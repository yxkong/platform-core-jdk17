package com.github.platform.core.gateway.admin.infra.util;

import com.github.platform.core.common.utils.CollectionUtil;
import com.github.platform.core.common.utils.JsonUtils;
import com.github.platform.core.common.utils.StringUtils;
import com.github.platform.core.gateway.admin.domain.common.entity.GatewayRouteBase;
import com.github.platform.core.gateway.admin.domain.common.entity.GatewayRouteConditionBase;
import com.github.platform.core.gateway.admin.domain.constant.ConditionEnum;
import com.github.platform.core.gateway.admin.domain.constant.GatewayAuthEnum;
import com.github.platform.core.gateway.admin.domain.constant.GatewayConstant;
import com.github.platform.core.gateway.admin.infra.exception.GatewayConfigException;
import com.github.platform.core.standard.constant.ResultStatusEnum;
import com.github.platform.core.standard.constant.SymbolConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * 路由信息适配到网关
 * @Author: yxkong
 * @Date: 2024/8/21
 * @version: 1.0
 */
@Slf4j
public class RouteInfoUtil {
    /**
     * 获取适配信息
     * @param basic 路由基本信息
     * @param conditions 路由条件列表
     * @return 路由定义Map
     * @throws GatewayConfigException 当配置无效时抛出
     */
    public static Map<String, Object> getResult(GatewayRouteBase basic,
                                                List<? extends GatewayRouteConditionBase> conditions) {

        Objects.requireNonNull(basic, "GatewayRouteBase cannot be null");

        LinkedHashMap<String, Object> result = new LinkedHashMap<>(16);

        // 基本信息处理
        result.put("id", basic.getRouteId());
        result.put("order", basic.getSort());
        // 添加鉴权元数据
        Map<String, Object> metadata = new HashMap<>();
        // URI处理
        try {
            result.put("uri", createValidUri(basic.getUri()));
        } catch (URISyntaxException e) {
            throw new GatewayConfigException(ResultStatusEnum.PARAM_ERROR.getStatus(),"Invalid URI format: " + basic.getUri());
        }
        if (StringUtils.isNotEmpty(basic.getAuthType()) && !GatewayAuthEnum.NONE.getType().equals(basic.getAuthType())) {
            metadata.put(GatewayConstant.AUTH_TYPE, basic.getAuthType());
            if (StringUtils.isNotEmpty(basic.getAuthConfig())) {
                metadata.put(GatewayConstant.AUTH_CONFIG, JsonUtils.fromJson(basic.getAuthConfig(), Map.class));
            }
        }

        if (CollectionUtil.isNotEmpty(conditions)) {
            // 处理条件和过滤器
            ConditionProcessor processor = new ConditionProcessor(conditions);
            result.put("filters", processor.getFilters());
            result.put("predicates", processor.getPredicates());
            metadata.put(GatewayConstant.AUTH_EXCLUSION,processor.getAuthExclusions());
        }

        result.put("metadata", metadata);

        return result;
    }
    private static URI createValidUri(String uri) throws URISyntaxException {
        if (StringUtils.isEmpty(uri)) {
            throw new URISyntaxException("", "URI cannot be null or empty");
        }
        return new URI(uri);
    }
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class RouteCondition {
        private String name;
        @Builder.Default
        private Map<String, String> args = new LinkedHashMap<>();
    }
    /**
     * 条件处理器，封装条件和过滤器的处理逻辑
     */
    private static class ConditionProcessor {
        private final List<RouteCondition> filters = new ArrayList<>();
        private final List<RouteCondition> predicates = new ArrayList<>();
        //认证排除
        private final List<String> authExclusions = new ArrayList<>();
        public ConditionProcessor(List<? extends GatewayRouteConditionBase> conditions) {
            processConditions(conditions);
        }
        private void processConditions(List<? extends GatewayRouteConditionBase> conditions) {
            // 按sort排序
            List<? extends GatewayRouteConditionBase> sortedConditions = conditions.stream()
                    .sorted(Comparator.comparingInt(GatewayRouteConditionBase::getSort))
                    .toList();
            sortedConditions.forEach(this::processSingleCondition);
        }
        private void processSingleCondition(GatewayRouteConditionBase condition) {
            try {
                ConditionEnum conditionEnum = ConditionEnum.getByName(condition.getName());
                if (conditionEnum == null) {
                    log.warn("Unknown condition name: {}", condition.getName());
                    return;
                }
                // 针对认证排除配置，进行特殊处理
                if (conditionEnum.isCustomType() && Objects.equals(GatewayConstant.AUTH_EXCLUSION,condition.getName())){
                    authExclusions.addAll( Arrays.asList(condition.getArgs().split(conditionEnum.getSplit())));
                    return;
                }
                Map<String, String> argsMap = processArgs(condition, conditionEnum);
                RouteCondition routeCondition = new RouteCondition(condition.getName(), argsMap);
                if (conditionEnum.isFilter()) {
                    filters.add(routeCondition);
                } else if (conditionEnum.isPredicate()){
                    predicates.add(routeCondition);
                }
            } catch (Exception e) {
                log.error("Process condition failed, condition: {}", condition, e);
            }
        }
        private Map<String, String> processArgs(GatewayRouteConditionBase condition,
                                                ConditionEnum conditionEnum) {
            Map<String, String> argsMap = new LinkedHashMap<>();

            if (conditionEnum == ConditionEnum.CUSTOM_FILTER) {
                processCustomFilterArgs(condition, argsMap);
            } else if (conditionEnum.getLength() == 1) {
                argsMap.put(conditionEnum.getSplit0(), condition.getArgs());
            } else if (conditionEnum.getLength() >= 2) {
                processStandardArgs(condition, conditionEnum, argsMap);
            }

            return argsMap;
        }

        /**
         * 自定义filter特殊处理
         * @param condition
         * @param argsMap
         */
        private void processCustomFilterArgs(GatewayRouteConditionBase condition,
                                             Map<String, String> argsMap) {
            String[] splits = Optional.ofNullable(condition.getArgs())
                    .orElseThrow(() -> new IllegalArgumentException("Args cannot be null for custom filter"))
                    .split(ConditionEnum.CUSTOM_FILTER.getSplit());

            if (splits.length == 0) {
                throw new IllegalArgumentException("Invalid custom filter args format");
            }

            argsMap.put(ConditionEnum.CUSTOM_FILTER.getSplit0(), splits[0]);

            for (int i = 1; i < splits.length; i++) {
                if (splits[i].contains(SymbolConstant.COLON)) {
                    String[] argParts = splits[i].split(SymbolConstant.COLON, 2);
                    argsMap.put(argParts[0], argParts[1]);
                }
            }
        }
        private void processStandardArgs(GatewayRouteConditionBase condition,
                                         ConditionEnum conditionEnum,
                                         Map<String, String> argsMap) {
            String[] splits = Optional.ofNullable(condition.getArgs())
                    .orElseThrow(() -> new IllegalArgumentException("Args cannot be null"))
                    .split(conditionEnum.getSplit());

            if (splits.length < conditionEnum.getLength()) {
                throw new IllegalArgumentException(
                        String.format("Expected %d args but got %d",
                                conditionEnum.getLength(), splits.length));
            }

            argsMap.put(conditionEnum.getSplit0(), splits[0].trim());
            if (conditionEnum.getLength() >= 2) {
                argsMap.put(conditionEnum.getSplit1(), splits[1].trim());
            }
        }
        public List<String> getAuthExclusions(){
            return Collections.unmodifiableList(authExclusions);
        }
        public List<RouteCondition> getFilters() {
            return Collections.unmodifiableList(filters);
        }
        public List<RouteCondition> getPredicates() {
            return Collections.unmodifiableList(predicates);
        }
    }
}

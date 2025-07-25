package com.github.platform.core.gateway.infra.filter;

import com.github.platform.core.common.constant.SpringBeanOrderConstant;
import com.github.platform.core.common.utils.CollectionUtil;
import com.github.platform.core.common.utils.JsonUtils;
import com.github.platform.core.common.utils.StringUtils;
import com.github.platform.core.loadbalancer.GrayLoadBalancer;
import com.github.platform.core.loadbalancer.domain.entity.GrayRuleEntity;
import com.github.platform.core.loadbalancer.gateway.IGrayRuleQueryGateway;
import com.github.platform.core.loadbalancer.holder.RequestHeaderHolder;
import com.github.platform.core.standard.constant.HeaderConstant;
import com.github.platform.core.standard.entity.common.LoginInfo;
import com.googlecode.aviator.AviatorEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultRequest;
import org.springframework.cloud.client.loadbalancer.LoadBalancerUriTools;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.gateway.support.DelegatingServiceInstance;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 灰度过滤器
 * @author: yxkong
 * @date: 2023/2/18 2:39 PM
 * @version: 1.0
 */
@Slf4j
public class GrayReactiveLoadBalancerFilter implements GlobalFilter, Ordered {
    private static final String LB = "lb";
    private  LoadBalancerClientFactory clientFactory;
    private IGrayRuleQueryGateway grayRuleService;
    private Environment environment;

    public GrayReactiveLoadBalancerFilter(LoadBalancerClientFactory clientFactory, IGrayRuleQueryGateway grayRuleService, Environment environment) {
        this.clientFactory = clientFactory;
        this.grayRuleService = grayRuleService;
        this.environment = environment;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        /**
         * 1，根据url判断，如果不是lb开头的，不走灰度路由
         */
        URI url = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        String schemePrefix = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR);
        if (url == null || (!LB.equals(url.getScheme()) && !LB.equals(schemePrefix))) {
            return chain.filter(exchange);
        }
        /**
         * 2，获取label标签
         */
        String label = getLabel(exchange);

        /**
         * 3，将label标签延续下去
         */
        ServerHttpRequest request;
        if (StringUtils.isNotEmpty(label)){
            request = exchange.getRequest().mutate()
                    .header(RequestHeaderHolder.LABEL_KEY,label)
                    .build();
        } else {
            request = exchange.getRequest().mutate().build();
        }
        if (log.isTraceEnabled()) {
            log.trace(ReactiveLoadBalancerClientFilter.class.getSimpleName() + " url before: " + url);
        }
        ServerWebExchangeUtils.addOriginalRequestUrl(exchange, url);
        return this.choose(exchange).doOnNext((response) -> {
            if (!response.hasServer()) {
                throw NotFoundException.create(true, "Unable to find instance for " + url.getHost());
            } else {
                URI uri = exchange.getRequest().getURI();
                String overrideScheme = null;
                if (schemePrefix != null) {
                    overrideScheme = url.getScheme();
                }

                DelegatingServiceInstance serviceInstance = new DelegatingServiceInstance(response.getServer(), overrideScheme);
                URI requestUrl = this.reconstructUri(serviceInstance, uri);
                if (log.isDebugEnabled()) {
                    log.debug("GrayReactiveLoadBalancerFilter url choose: " + requestUrl);
                }

                exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, requestUrl);
            }
        }).then(chain.filter(exchange.mutate().request(request).build()));

    }
    private String getLabel(ServerWebExchange exchange){
        String label = getLabelFromRule(exchange);
        if (Objects.nonNull(label)){
            return label;
        }
        return  getLabelFromHeader(exchange);
    }

    /**
     * 根据规则获取label
     * @param exchange
     * @return
     */
    private String getLabelFromRule(ServerWebExchange exchange){
        String info = exchange.getRequest().getHeaders().getFirst(HeaderConstant.LOGIN_INFO);
        if (Objects.isNull(info)){
            return null;
        }
        info = URLDecoder.decode(info,StandardCharsets.UTF_8);
        final LoginInfo loginInfo = JsonUtils.fromJson(info, LoginInfo.class);
        if (Objects.isNull(loginInfo)){
            return null;
        }
        final GrayRuleEntity grayRule = grayRuleService.findOne();
        if (Objects.isNull(grayRule)){
            return null;
        }
        final Map<String, Object> map = buildRuleMeta(loginInfo);
        /**
         * fixed  每次执行通过ASMCodeGenerator 动态生成类文件，导致metaspace不断增大，最终OOM，
         *    如果未设置最大值，会把系统内存占完
         */
        Boolean flag = (Boolean) AviatorEvaluator.execute(grayRule.getRule(), map,true);
        if (log.isWarnEnabled()){
            log.warn("用户{} 规则判定走label为：{} 的服务",loginInfo.getLoginName(),flag);
        }
        if(!flag){
           return null;
        }
        return grayRule.getLable();

    }
    /**
     * 初始化规则包的元数据，配置时从这里取值
     * @param loginInfo
     * @return
     */
    private Map<String,Object> buildRuleMeta(LoginInfo loginInfo){
        Map<String,Object> map = new HashMap<>();
        map.put("userId",loginInfo.getId());
        map.put("age",loginInfo.getAge());
        map.put("loginName",loginInfo.getLoginName());
        map.put("loginWay",loginInfo.getLoginWay());
        map.put("mobile",loginInfo.getMobile());
        map.put("loginTime",loginInfo.getLoginTime());
        map.put("registerTime",loginInfo.getRegisterTime());
        if (Objects.nonNull(loginInfo.getMobile())){
            long mod = Long.parseLong(loginInfo.getMobile())%10L;
            map.put("mobileMod",String.valueOf(mod));
        }
        if (Objects.nonNull(loginInfo.getId())){
            map.put("userIdMod",String.valueOf(loginInfo.getId()%10L));
        }
        return map;
    }
    /**
     * 从header中获取label
     * @param exchange
     * @return
     */
    private String getLabelFromHeader(ServerWebExchange exchange){
        List<String> labelArr = exchange.getRequest().getHeaders().get(RequestHeaderHolder.LABEL_KEY);
        if(!CollectionUtil.isEmpty(labelArr)){
            return labelArr.get(0);
        }
        return null;
    }
    protected URI reconstructUri(ServiceInstance serviceInstance, URI original) {
        return LoadBalancerUriTools.reconstructURI(serviceInstance, original);
    }

    private Mono<Response<ServiceInstance>> choose(ServerWebExchange exchange) {
        URI uri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        GrayLoadBalancer loadBalancer = new GrayLoadBalancer(clientFactory.getLazyProvider(uri.getHost(), ServiceInstanceListSupplier.class), uri.getHost(),environment);
        return loadBalancer.choose(this.createRequest(exchange));
    }

    private Request createRequest(ServerWebExchange exchange) {
        return new DefaultRequest<>( exchange.getRequest().getHeaders());
    }

    @Override
    public int getOrder() {
        /**
         * RouteToRequestUrlFilter 是10000
         *  系统默认的ReactiveLoadBalancerClientFilter order是10150
         *  得在这两个之间 ,取：10100
         */
        return SpringBeanOrderConstant.LOAD_BALANCER_CLIENT_FILTER_ORDER;
    }
}

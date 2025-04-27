package com.github.platform.core.loadbalancer;

import com.github.platform.core.common.utils.CollectionUtil;
import com.github.platform.core.loadbalancer.holder.RequestHeaderHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: yxkong
 * @date: 2023/2/23 5:20 PM
 * @version: 1.0
 */
@Slf4j
public class GrayLoadBalancer implements ReactorServiceInstanceLoadBalancer {
    /**
     *  loadbalancer 提供的访问当前服务的名称
     */
    private final String serviceId;
    /**
     * loadbalancer提供的访问服务列表
     */
    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    //普通server轮训器
    private AtomicLong nextServerCyclicCounter;
    private final Environment environment;

    public GrayLoadBalancer( ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider
            , String serviceId, Environment environment) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.environment = environment;
        this.nextServerCyclicCounter = new AtomicLong(0);
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        // 1. 防御性检查serviceId
        if (StringUtils.isEmpty(this.serviceId)) {
            log.error("ServiceId cannot be empty in GrayLoadBalancer");
            return Mono.just(new EmptyResponse());
        }
        // 2. 安全获取headers
        HttpHeaders headers = null;
        try {
            if (request instanceof RequestDataContext) {
                headers = ((RequestDataContext) request).getClientRequest().getHeaders();
            } else if (request instanceof DefaultRequest) {
                Object context = ((DefaultRequest<?>) request).getContext();
                if (context instanceof HttpHeaders) {
                    headers = (HttpHeaders) context;
                } else if (context instanceof RetryableRequestContext) {
                    headers = ((RetryableRequestContext) context).getClientRequest().getHeaders();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse request headers", e);
        }

        // 3. 安全获取Supplier
        ServiceInstanceListSupplier supplier;
        try {
            supplier = Optional.of(serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new))
                    .orElseGet(NoopServiceInstanceListSupplier::new);
        } catch (Exception e) {
            log.error("Failed to get ServiceInstanceListSupplier", e);
            return Mono.just(new EmptyResponse());
        }
        // 4. 添加trace日志
        if (log.isDebugEnabled()) {
            log.debug("Choosing instance for service: {}, label: {}",
                    serviceId,
                    headers != null ? headers.getFirst(RequestHeaderHolder.LABEL_KEY) : "null");
        }
        HttpHeaders finalHeaders = headers;
        return supplier.get().next().map(instances -> getInstanceResponse(instances, finalHeaders));
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> serviceInstances, HttpHeaders headers) {
        // 1. 空列表防御
        if (CollectionUtil.isEmpty(serviceInstances)) {
            log.warn("No servers available for service: " + this.serviceId);
            return new EmptyResponse();
        }

        // 2. 过滤掉无效实例（防御性编程）
        List<ServiceInstance> validInstances = serviceInstances.stream()
                .filter(instance -> instance != null && instance.getMetadata() != null)
                .toList();

        // 3. 获取灰度标签
        String label = headers != null ?
                headers.getFirst(RequestHeaderHolder.LABEL_KEY) :
                RequestHeaderHolder.getLabel();

        // 4. 无灰度标签或标签为空时直接走默认逻辑
        if (StringUtils.isEmpty(label)) {
            return getInstanceByDefault(validInstances);
        }

        // 5. 尝试匹配灰度实例
        List<ServiceInstance> grayInstances = validInstances.stream()
                .filter(instance -> label.equals(instance.getMetadata().get(RequestHeaderHolder.LABEL_KEY)))
                .toList();

        // 6. 灰度实例不存在时降级到默认逻辑
        if (grayInstances.isEmpty()) {
            log.warn("No gray servers available for label [{}], fallback to default instances", label);
            return getInstanceByDefault(validInstances);
        }

        // 7. 执行灰度权重选择
        return getInstanceByWeight(grayInstances);
    }


    /**
     * 根据权重选择
     * see com.alibaba.nacos.client.naming.core.Balancer#getHostByRandomWeight
     * @param instances
     * @return
     */
    private Response<ServiceInstance> getInstanceByWeight(List<ServiceInstance> instances){
        double[] weights = instances.stream()
                .mapToDouble(instance -> {
                    String weightStr = instance.getMetadata().getOrDefault(
                            "nacos.weight",
                            instance.getMetadata().getOrDefault("eureka.weight", "1.0")
                    );
                    return Math.max(Double.parseDouble(weightStr), 0);
                })
                .toArray();
        // 权重总和
        double totalWeight = Arrays.stream(weights).sum();
        if (totalWeight <= 0) {
            // 降级随机选择
            return getInstanceByRandom(instances);
        }
        // 加权随机算法
        double random = ThreadLocalRandom.current().nextDouble() * totalWeight;
        double sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += weights[i];
            if (random <= sum) {
                return new DefaultResponse(instances.get(i));
            }
        }
        //兜底
        return new DefaultResponse(instances.get(0));
    }
    /**
     * 根据配置选择默认负载均衡策略
     */
    private Response<ServiceInstance> getInstanceByDefault(List<ServiceInstance> instances) {
        String strategy = environment.getProperty("loadbalancer.default-strategy", "random");
        switch (strategy.toLowerCase()) {
            case "round-robin":
                return getInstanceByRoundRobin(instances);
            case "random":
            default:
                return getInstanceByRandom(instances);
        }
    }

    /**
     * 轮训的方式获取（不均匀，）
     * 参考 org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer#getInstanceResponse
     * @param instances
     * @return
     */
    private Response<ServiceInstance> getInstanceByRoundRobin(List<ServiceInstance> instances){
        long current = nextServerCyclicCounter.updateAndGet(prev ->
                prev >= Long.MAX_VALUE - 1 ? 0 : prev + 1
        );
        ServiceInstance instance = instances.get((int)(current % instances.size()));
        return new DefaultResponse(instance);
    }

    /**
     * 随机方式获取
     * @param instances
     * @return
     */
    private Response<ServiceInstance> getInstanceByRandom(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("No servers available for service: " + serviceId);
            }
            return new EmptyResponse();
        }
        int index = ThreadLocalRandom.current().nextInt(instances.size());
        ServiceInstance instance = instances.get(index);
        return new DefaultResponse(instance);
    }
}

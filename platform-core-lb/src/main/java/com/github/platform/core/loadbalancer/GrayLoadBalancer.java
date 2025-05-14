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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 灰度发布负载均衡器实现
 *
 * @author: yxkong
 * @date: 2023/2/23 5:20 PM
 * @version: 1.1 (优化版)
 */
@Slf4j
public class GrayLoadBalancer implements ReactorServiceInstanceLoadBalancer {
    private static final String DEFAULT_STRATEGY = "random";
    private static final String LABEL_KEY = RequestHeaderHolder.LABEL_KEY;
    private static final String WEIGHT_KEY_NACOS = "nacos.weight";
    private static final String WEIGHT_KEY_EUREKA = "eureka.weight";
    private static final double DEFAULT_WEIGHT = 1.0;

    private final String serviceId;
    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final AtomicInteger nextServerCyclicCounter;
    private final Environment environment;

    public GrayLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
                            String serviceId, Environment environment) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.environment = environment;
        this.nextServerCyclicCounter = new AtomicInteger(0);
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        if (StringUtils.isEmpty(this.serviceId)) {
            log.error("ServiceId cannot be empty in GrayLoadBalancer");
            return Mono.just(new EmptyResponse());
        }

        HttpHeaders headers = extractHeaders(request);

        return getServiceInstanceSupplier()
                .flatMapMany(ServiceInstanceListSupplier::get)
                .next()
                .map(instances -> getInstanceResponse(instances, headers))
                .onErrorResume(e -> {
                    log.error("Error occurred while choosing instance for service: {}", serviceId, e);
                    return Mono.just(new EmptyResponse());
                });
    }

    private HttpHeaders extractHeaders(Request request) {
        try {
            if (request instanceof RequestDataContext) {
                return ((RequestDataContext) request).getClientRequest().getHeaders();
            } else if (request instanceof DefaultRequest) {
                Object context = ((DefaultRequest<?>) request).getContext();
                if (context instanceof HttpHeaders) {
                    return (HttpHeaders) context;
                } else if (context instanceof RetryableRequestContext) {
                    return ((RetryableRequestContext) context).getClientRequest().getHeaders();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse request headers", e);
        }
        return null;
    }

    private Mono<ServiceInstanceListSupplier> getServiceInstanceSupplier() {
        return Mono.fromSupplier(() ->
                Optional.ofNullable(serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new))
                        .orElseGet(NoopServiceInstanceListSupplier::new)
        );
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> serviceInstances, HttpHeaders headers) {
        if (CollectionUtil.isEmpty(serviceInstances)) {
            log.warn("No servers available for service: {}", serviceId);
            return new EmptyResponse();
        }

        List<ServiceInstance> validInstances = filterValidInstances(serviceInstances);
        if (validInstances.isEmpty()) {
            log.warn("No valid instances found for service: {}", serviceId);
            return new EmptyResponse();
        }

        String label = getLabelFromHeadersOrHolder(headers);
        if (StringUtils.isEmpty(label)) {
            return getInstanceByDefault(validInstances);
        }

        List<ServiceInstance> grayInstances = filterGrayInstances(validInstances, label);
        if (grayInstances.isEmpty()) {
            log.warn("No gray servers available for label [{}], fallback to default instances", label);
            return getInstanceByDefault(validInstances);
        }

        return getInstanceByWeight(grayInstances);
    }

    private List<ServiceInstance> filterValidInstances(List<ServiceInstance> instances) {
        return instances.stream()
                .filter(instance -> instance != null && instance.getMetadata() != null)
                .toList();
    }

    private String getLabelFromHeadersOrHolder(HttpHeaders headers) {
        return headers != null ? headers.getFirst(LABEL_KEY) : RequestHeaderHolder.getLabel();
    }

    private List<ServiceInstance> filterGrayInstances(List<ServiceInstance> instances, String label) {
        return instances.stream()
                .filter(instance -> label.equals(instance.getMetadata().get(LABEL_KEY)))
                .toList();
    }

    /**
     * 根据权重选择实例
     */
    private Response<ServiceInstance> getInstanceByWeight(List<ServiceInstance> instances) {
        double[] weights = instances.stream()
                .mapToDouble(this::extractInstanceWeight)
                .toArray();

        double totalWeight = sumWeights(weights);
        if (totalWeight <= 0) {
            return getInstanceByRandom(instances);
        }

        double randomWeight = ThreadLocalRandom.current().nextDouble() * totalWeight;
        double sum = 0;

        for (int i = 0; i < weights.length; i++) {
            sum += weights[i];
            if (randomWeight <= sum) {
                return new DefaultResponse(instances.get(i));
            }
        }

        return new DefaultResponse(instances.get(0));
    }

    private double extractInstanceWeight(ServiceInstance instance) {
        try {
            String weightStr = instance.getMetadata().getOrDefault(WEIGHT_KEY_NACOS,
                    instance.getMetadata().getOrDefault(WEIGHT_KEY_EUREKA, String.valueOf(DEFAULT_WEIGHT)));
            return Math.max(Double.parseDouble(weightStr), 0);
        } catch (NumberFormatException e) {
            log.warn("Invalid weight value for instance: {}, using default weight", instance.getInstanceId(), e);
            return DEFAULT_WEIGHT;
        }
    }

    private double sumWeights(double[] weights) {
        double sum = 0;
        for (double weight : weights) {
            sum += weight;
        }
        return sum;
    }

    /**
     * 根据配置选择默认负载均衡策略
     */
    private Response<ServiceInstance> getInstanceByDefault(List<ServiceInstance> instances) {
        String strategy = environment.getProperty("loadbalancer.default-strategy", DEFAULT_STRATEGY);
        switch (strategy.toLowerCase()) {
            case "round-robin":
                return getInstanceByRoundRobin(instances);
            case "random":
            default:
                return getInstanceByRandom(instances);
        }
    }

    /**
     * 轮询方式选择实例
     */
    private Response<ServiceInstance> getInstanceByRoundRobin(List<ServiceInstance> instances) {
        int current = nextServerCyclicCounter.updateAndGet(prev ->
                prev >= Integer.MAX_VALUE - 1 ? 0 : prev + 1
        );
        int index = current % instances.size();
        return new DefaultResponse(instances.get(index));
    }

    /**
     * 随机方式选择实例
     */
    private Response<ServiceInstance> getInstanceByRandom(List<ServiceInstance> instances) {
        int index = ThreadLocalRandom.current().nextInt(instances.size());
        return new DefaultResponse(instances.get(index));
    }
}

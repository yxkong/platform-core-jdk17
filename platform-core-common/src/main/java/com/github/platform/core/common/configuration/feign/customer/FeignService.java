package com.github.platform.core.common.configuration.feign.customer;

import com.github.platform.core.common.constant.RequestTypeEnum;
import com.github.platform.core.standard.entity.dto.ResultBean;
import com.github.platform.core.standard.exception.CommonException;
import com.github.platform.core.standard.util.ResultBeanUtil;
import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.cloud.openfeign.FeignLoggerFactory;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author: yxkong
 * @date: 2023/5/24 8:31 PM
 * @version: 1.0
 */
@Component
@Import(FeignClientsConfiguration.class)
@Slf4j
public class FeignService {

    private PlatformFeignClient feignClient;
    private String url;
    private RequestTypeEnum requestType;
    private Map<String, Object> headers = new LinkedHashMap<>();
    private Map<String, Object> params = new LinkedHashMap<>();
    private Object requestBody;

    private final Logger logger;
    private final Encoder encoder;
    private final Decoder decoder;

    private final List<RequestInterceptor> interceptors = new ArrayList<>();

    private boolean streaming = false;
    private Consumer<InputStream> streamConsumer;
    private int streamingBufferSize = 8192;

    @Autowired
    public FeignService(FeignLoggerFactory loggerFactory, Encoder encoder, Decoder decoder) {
        this.logger = loggerFactory.create(PlatformFeignClient.class);
        this.encoder = encoder;
        this.decoder = decoder;
    }

    /** 设置请求 URL */
    public FeignService url(String url) {
        if (url == null) throw new IllegalArgumentException("URL不能为空");
        this.url = url;
        return this;
    }

    /** 添加请求参数（非 request body） */
    public FeignService params(Map<String, Object> params) {
        if (params != null) this.params.putAll(params);
        return this;
    }

    public FeignService addParam(String key, Object value) {
        this.params.put(key, value);
        return this;
    }

    /** 添加 Header */
    public FeignService header(Map<String, Object> headers) {
        if (headers != null) this.headers.putAll(headers);
        return this;
    }

    public FeignService addHeader(String key, Object value) {
        this.headers.put(key, value);
        return this;
    }

    /** 设置请求类型 */
    public FeignService requestType(RequestTypeEnum requestType) {
        this.requestType = requestType;
        return this;
    }

    public FeignService get() {
        return requestType(RequestTypeEnum.GET);
    }

    public FeignService post() {
        return requestType(RequestTypeEnum.POST);
    }
    /**body 必须存在，如果是没有body请使用post*/
    public FeignService restful() {
        return requestType(RequestTypeEnum.RESTFUL);
    }

    /** 设置请求体，仅 RESTFUL 使用 */
    public FeignService body(Object requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    /** 添加 Feign 拦截器，仅当前请求有效 */
    public FeignService interceptor(RequestInterceptor interceptor) {
        this.interceptors.add(interceptor);
        return this;
    }

    /** 启用流式响应处理 */
    public FeignService streaming() {
        this.streaming = true;
        return this;
    }

    public FeignService streaming(Consumer<InputStream> streamConsumer) {
        this.streaming = true;
        this.streamConsumer = streamConsumer;
        return this;
    }

    public FeignService streamingBufferSize(int bufferSize) {
        this.streamingBufferSize = bufferSize;
        return this;
    }

    /** 构造动态 FeignClient */
    private void buildClient() {
        Feign.Builder builder = Feign.builder()
                .logger(logger)
                .encoder(encoder)
                .decoder(decoder);

        for (RequestInterceptor interceptor : interceptors) {
            builder.requestInterceptor(interceptor);
        }

        this.feignClient = builder.target(Target.EmptyTarget.create(PlatformFeignClient.class));
    }

    public ResultBean resultBean() {
        validateRequest();
        try {
            return switch (requestType) {
                case GET -> feignClient.getResultBean(getUri(), headers, params);
                case POST -> feignClient.postResultBean(getUri(), headers, params);
                case RESTFUL -> feignClient.restfulResultBean(getUri(), headers, requestBody);
            };
        } catch (Exception e) {
            log.error("请求失败", e);
            return ResultBeanUtil.fail(e.getMessage());
        }

    }

    public Response execute() {
        validateRequest();
        return switch (requestType) {
            case GET -> feignClient.get(getUri(), headers, params);
            case POST -> feignClient.post(getUri(), headers, params);
            case RESTFUL -> feignClient.restful(getUri(), headers, requestBody);
        };
    }

    public void executeAndStream() {
        validateRequest();
        try (Response response = execute()) {
            if (!streaming) throw new IllegalStateException("当前请求未启用流式模式");
            processStreamingResponse(response);
        } catch (Exception e) {
            log.error("流式请求失败", e);
            throw new FeignRequestException("ERROR", "流式请求失败", e);
        }
    }

    public <T> T executeAndStream(Function<InputStream, T> streamProcessor) {
        validateRequest();
        try (Response response = execute()) {
            if (!streaming) throw new IllegalStateException("当前请求未启用流式模式");
            return processStreamingResponse(response, streamProcessor);
        } catch (Exception e) {
            log.error("流式请求失败", e);
            throw new FeignRequestException("ERROR", "流式请求失败", e);
        }
    }

    /** 处理 InputStream 消费 */
    private void processStreamingResponse(Response response) throws IOException {
        if (response.body() == null) throw new IOException("响应体为空");
        try (InputStream is = new BufferedInputStream(response.body().asInputStream(), streamingBufferSize)) {
            streamConsumer.accept(is);
        }
    }

    private <T> T processStreamingResponse(Response response, Function<InputStream, T> processor) throws IOException {
        if (response.body() == null) throw new IOException("响应体为空");
        try (InputStream is = new BufferedInputStream(response.body().asInputStream(), streamingBufferSize)) {
            return processor.apply(is);
        }
    }

    /** 请求参数校验 + 构建 Feign Client */
    private void validateRequest() {
        if (this.requestType == null) this.requestType = RequestTypeEnum.RESTFUL;
        buildClient();
    }

    private URI getUri() {
        try {
            return new URI(this.url);
        } catch (URISyntaxException e) {
            throw new RuntimeException("URL格式错误：" + url, e);
        }
    }

    /** 自定义异常 */
    public static class FeignRequestException extends CommonException {

        public FeignRequestException(String status, String message) {
            super(status,message);
        }

        public FeignRequestException(String status, String message, Throwable cause) {
            super(status,message, cause);
        }

    }
}



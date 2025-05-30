package com.github.platform.core.common.configuration.feign.customer;

import com.github.platform.core.common.configuration.feign.FeignClientTimeoutProperties;
import com.github.platform.core.common.constant.RequestTypeEnum;
import com.github.platform.core.standard.entity.dto.ResultBean;
import com.github.platform.core.standard.exception.CommonException;
import com.github.platform.core.standard.util.ResultBeanUtil;
import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.slf4j.Slf4jLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
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

    private final FeignClientTimeoutProperties feignClientTimeoutProperties;
    private final Decoder decoder;
    private final Encoder encoder;
    private final Retryer retryer;
    private final Logger.Level logLevel;
    private final QueryMapEncoder queryMapEncoder;
    private final List<RequestInterceptor> interceptors;

    private boolean streaming = false;
    private boolean sseEnabled = false;
    private Consumer<InputStream> streamConsumer;
    private int streamingBufferSize = 8192;
    private static final Object CACHE_LOCK = new Object();

    @Autowired
    public FeignService(FeignClientTimeoutProperties feignClientTimeoutProperties,
                        Decoder decoder,
                        Encoder encoder,
                        Retryer retryer,
                        Logger.Level logLevel,
                        QueryMapEncoder queryMapEncoder,
                        List<RequestInterceptor> interceptors) {
        this.feignClientTimeoutProperties = feignClientTimeoutProperties;
        this.decoder = decoder;
        this.encoder = encoder;
        this.retryer = retryer;
        this.logLevel = logLevel;
        this.queryMapEncoder = queryMapEncoder;
        this.interceptors = interceptors;
    }

    /** 设置请求 URL */
    public FeignService url(String url) {
        if (url == null) throw new IllegalArgumentException("URL不能为空");
        this.url = url;
        return this;
    }

    /** 添加请求参数 */
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

    /** body 必须存在，如果是没有body请使用post */
    public FeignService restful() {
        return requestType(RequestTypeEnum.RESTFUL);
    }

    /** 设置为SSE请求类型 */
    public FeignService sse() {
        this.sseEnabled = true;
        return requestType(RequestTypeEnum.SSE);
    }

    /** 设置为异步请求类型 */
    public FeignService async() {
        return requestType(RequestTypeEnum.ASYNC);
    }

    /** 设置为流式请求类型 */
    public FeignService stream() {
        this.streaming = true;
        return requestType(RequestTypeEnum.STREAM);
    }

    /** 设置请求体 */
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
        if (Objects.nonNull(this.feignClient)) {
            return;
        }
        synchronized (CACHE_LOCK) {
            FeignClientTimeoutProperties.ClientConfig clientConfig =
                    feignClientTimeoutProperties.getConfigForClient("platformFeignClient");

            // 创建新的 Options 对象
            Request.Options options = new Request.Options(
                    clientConfig.getConnectTimeout(),
                    TimeUnit.MILLISECONDS,
                    clientConfig.getReadTimeout(),
                    TimeUnit.MILLISECONDS,
                    true
            );

            // 使用注入的组件创建Builder
            Feign.Builder builder = Feign.builder()
                    .contract(new Contract.Default())
                    .options(options)
                    .logger(new Slf4jLogger(FeignService.class))
                    .encoder(encoder)
                    .decoder(decoder)
                    .retryer(retryer)
                    .logLevel(logLevel)
                    .queryMapEncoder(queryMapEncoder);
            // 添加请求特定拦截器
            for (RequestInterceptor interceptor : interceptors) {
                builder.requestInterceptor(interceptor);
            }
            this.feignClient = builder.target(Target.EmptyTarget.create(PlatformFeignClient.class));
        }
    }

    public ResultBean resultBean() {
        validateRequest();
        try {
            return switch (requestType) {
                case GET -> feignClient.getResultBean(getUri(), headers, params);
                case POST -> feignClient.postResultBean(getUri(), headers, params);
                case RESTFUL -> feignClient.restfulResultBean(getUri(), headers, requestBody);
                case SSE, STREAM -> {
                    log.warn("流式请求不应使用resultBean方法");
                    yield ResultBeanUtil.fail("不支持的请求类型");
                }
                default -> throw new IllegalStateException("不支持的请求类型: " + requestType);
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
            case RESTFUL,SSE -> feignClient.restful(getUri(), headers, requestBody);
            case STREAM -> feignClient.streamRequest(getUri(), headers, requestBody);
            case ASYNC -> throw new IllegalStateException("异步请求请使用executeAsync方法");
            default -> throw new IllegalStateException("不支持的请求类型: " + requestType);
        };
    }
    /** 处理 InputStream 消费 */
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

    /** 执行SSE请求 */
    public void executeSse(Consumer<String> eventConsumer) {
        validateRequest();
        if (!sseEnabled) {
            throw new IllegalStateException("当前请求未启用SSE模式");
        }
        try (Response response = execute()) {
            processSseResponse(response, eventConsumer);
        } catch (Exception e) {
            log.error("SSE请求失败", e);
            throw new FeignRequestException("ERROR", "SSE请求失败", e);
        }
    }

    /** 执行大模型流式请求 */
    public void executeModelStream(Consumer<String> chunkConsumer) {
        validateRequest();
        if (!streaming) {
            throw new IllegalStateException("当前请求未启用流式模式");
        }
        try (Response response = execute()) {
            processModelStreamResponse(response, chunkConsumer);
        } catch (Exception e) {
            log.error("大模型流式请求失败", e);
            throw new FeignRequestException("ERROR", "大模型流式请求失败", e);
        }
    }

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

    private void processSseResponse(Response response, Consumer<String> eventConsumer) throws IOException {
        if (response.body() == null) throw new IOException("响应体为空");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.body().asInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String event = line.substring(6).trim();
                    if (!event.isEmpty() && !"[DONE]".equals(event)) {
                        eventConsumer.accept(event);
                    }
                }
            }
        }
    }

    private void processModelStreamResponse(Response response, Consumer<String> chunkConsumer) throws IOException {
        if (response.body() == null) throw new IOException("响应体为空");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.body().asInputStream(), StandardCharsets.UTF_8))) {

            String chunk;
            while ((chunk = reader.readLine()) != null) {
                if (!chunk.isEmpty()) {
                    chunkConsumer.accept(chunk);
                }
            }
        }
    }

    /** 请求参数校验 + 构建 Feign Client */
    private void validateRequest() {
        if (this.requestType == null) this.requestType = RequestTypeEnum.RESTFUL;
        // 设置默认内容类型
        if (!headers.containsKey("Content-Type")) {
            if (requestType == RequestTypeEnum.RESTFUL || requestType == RequestTypeEnum.SSE) {
                headers.put("Content-Type", "application/json");
            } else if (requestType == RequestTypeEnum.POST) {
                headers.put("Content-Type", "application/x-www-form-urlencoded");
            } else if(requestType == RequestTypeEnum.STREAM){
                headers.put("Content-Type", "application/octet-stream");
            }
        }
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
            super(status, message);
        }

        public FeignRequestException(String status, String message, Throwable cause) {
            super(status, message, cause);
        }
    }
}



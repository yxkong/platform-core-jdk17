package com.github.platform.core.common.configuration.feign.customer;

import com.github.platform.core.common.constant.RequestTypeEnum;
import com.github.platform.core.standard.constant.ResultStatusEnum;
import com.github.platform.core.standard.entity.dto.ResultBean;
import com.github.platform.core.standard.exception.CommonException;
import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.cloud.openfeign.FeignLoggerFactory;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
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
    private Map<String, Object> headers;
    private Map<String, Object> params;
    private Object requestBody;
    private final Logger logger;
    private final Encoder encoder;
    private final Decoder decoder;
    private final Target.EmptyTarget<PlatformFeignClient> platformFeignClientEmptyTarget = Target.EmptyTarget.create(PlatformFeignClient.class);
    //支持拦截器
    private final List<RequestInterceptor> interceptors = new ArrayList<>();
    //流式输出
    private boolean streaming = false;
    private Consumer<InputStream> streamConsumer;
    private int streamingBufferSize = 8192;
    /**
     * 在构造函数中用 @Autowired 引入自定义参数和编解码器
     * @param encoder
     * @param decoder
     */
    @Autowired
    public FeignService(FeignLoggerFactory loggerFactory, Encoder encoder, Decoder decoder) {
        this.logger = loggerFactory.create(PlatformFeignClient.class);
        this.encoder = encoder;
        this.decoder = decoder;
        feignClient = Feign.builder()
                .logger(this.logger)
                .encoder(this.encoder).decoder(this.decoder)
                .target(this.platformFeignClientEmptyTarget);
    }
    public FeignService url(String url) {
        if (url == null) {
            throw new IllegalArgumentException("URL cannot be null");
        } else {
            this.url = url;
        }
        return this;
    }
    @Deprecated
    public FeignService request(Map<String, Object> request) {
        this.params = request;
        return this;
    }
    public FeignService params(Map<String, Object> params) {
        this.params = params;
        return this;
    }
    public FeignService addParam(String key, Object value) {
        if (this.params == null) {
            this.params = new LinkedHashMap<>();
        }
        this.params.put(key, value);
        return this;
    }
    public FeignService header(Map<String, Object> header) {
        this.headers = header;
        return this;
    }
    public FeignService addHeader(String key, Object value) {
        if (this.headers == null) {
            this.headers = new LinkedHashMap<>();
        }
        this.headers.put(key, value);
        return this;
    }
    public FeignService requestType(RequestTypeEnum requestType) {
        this.requestType = requestType;
        return this;
    }
    public FeignService get() {
        this.requestType = RequestTypeEnum.GET;
        return this;
    }
    public FeignService post() {
        this.requestType = RequestTypeEnum.POST;
        return this;
    }
    public FeignService restful() {
        this.requestType = RequestTypeEnum.RESTFUL;
        return this;
    }
    public FeignService body(Object requestBody) {
        this.requestBody = requestBody;
        return this;
    }
    public FeignService interceptor(RequestInterceptor interceptor) {
        this.interceptors.add(interceptor);
        return this;
    }
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
    private void validateRequest(){
        if (Objects.isNull(this.headers)){
            this.headers = new LinkedHashMap<>();
        }
        if (Objects.isNull(this.params)){
            this.params = new LinkedHashMap<>();
        }
        if (Objects.isNull(requestType)){
            this.requestType = RequestTypeEnum.RESTFUL;
        }
        // 应用所有拦截器
        if (!interceptors.isEmpty()) {
            this.feignClient = Feign.builder()
                    .logger(this.logger)
                    .encoder(this.encoder)
                    .decoder(this.decoder)
                    .target(this.platformFeignClientEmptyTarget)
            ;
        }
    }
    /**
     * 执行远程调用，返回ResultBean
     * @return
     */
    public ResultBean resultBean(){
        this.validateRequest();
        return switch (requestType) {
            case GET -> feignClient.getResultBean(getUri(), this.headers, this.params);
            case POST, RESTFUL -> {
                if (requestBody != null) {
                    yield feignClient.postResultBean(getUri(), headers, requestBody);
                }
                yield feignClient.postResultBean(getUri(), headers, params);
            }
        };
    }

    /**
     * 执行调用方法返回Response
     * @return
     */
    public Response execute() {
        this.validateRequest();
        return switch (requestType) {
            case GET -> feignClient.get(getUri(),this.headers,this.params);
            case POST, RESTFUL ->{
                if (requestBody != null) {
                    yield feignClient.post(getUri(), headers, requestBody);
                }
                yield feignClient.post(getUri(),this.headers,this.params);
            }
        };
    }
    /**
     * 新增流式响应处理方法
     */
    public void executeAndStream() {
        validateRequest();
        try (Response response = execute()) {
            if (!streaming) {
                throw new IllegalStateException("Not in streaming mode");
            }
            processStreamingResponse(response);
        } catch (Exception e) {
            log.error("Feign streaming request failed", e);
            throw new FeignRequestException(ResultStatusEnum.ERROR.getStatus(),"Feign streaming request failed", e);
        }
    }

    /**
     * 新增流式响应处理方法（带返回值）
     */
    public <T> T executeAndStream(Function<InputStream, T> streamProcessor) {
        validateRequest();
        try (Response response = execute()) {
            if (!streaming) {
                throw new IllegalStateException("Not in streaming mode");
            }
            return processStreamingResponse(response, streamProcessor);
        } catch (Exception e) {
            log.error("Feign streaming request failed", e);
            throw new FeignRequestException(ResultStatusEnum.ERROR.getStatus(),"Feign streaming request failed", e);
        }
    }
    private void processStreamingResponse(Response response) throws IOException {
        if (response.body() == null) {
            throw new IOException("Response body is null");
        }

        try (InputStream inputStream = response.body().asInputStream()) {
            streamConsumer.accept(inputStream);
        }
    }

    private <T> T processStreamingResponse(Response response,
                                           Function<InputStream, T> streamProcessor) throws IOException {
        if (response.body() == null) {
            throw new IOException("Response body is null");
        }

        try (InputStream inputStream = response.body().asInputStream()) {
            return streamProcessor.apply(inputStream);
        }
    }
    private URI getUri(){
        try {
            return new URI(this.url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
 // ========== 自定义异常 ==========

    public static class FeignRequestException extends CommonException {
        public FeignRequestException(String status,String message) {
            super(status,message);
        }

        public FeignRequestException(String status,String message, Throwable cause) {
            super(status,message, cause);
        }
    }
}

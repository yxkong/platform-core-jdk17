package com.github.platform.core.web.configuration;

import com.github.platform.core.web.configuration.properties.SwaggerProperties;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 自定义api
 *
 * @Author: yxkong
 * @Date: 2024/12/25
 * @version: 1.0
 */

//@OpenAPIDefinition(
//        // 用于配置多环境
//        servers = {
//                @Server(description = "开发环境服务器", url = "http://localhost:8080"),
//                @Server(description = "测试环境服务器", url = "https://testapi.5ycode.com")
//        },
//        // 用于配置外部文档信息
//        externalDocs = @ExternalDocumentation(
//                description = "项目编译部署说明",
//                url = "http://localhost:8080/readme.md"
//        )
//)
@Configuration
public class SpringDocConfiguration {
    @Resource
    private SwaggerProperties swaggerProperties;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        //文档标题
                        .title(swaggerProperties.getTitle())
                        //文档描述
                        .description(swaggerProperties.getDescription())
                        //文档版本
                        .version(swaggerProperties.getVersion())
                        //文档授权
                        .termsOfService(swaggerProperties.getServiceUrl())
                        //文档联系人
                        .contact(new Contact()
                                .name(swaggerProperties.getContactName())
                                .url(swaggerProperties.getContactUrl())
                                .email(swaggerProperties.getContactEmail()))
                        // 文档授权
                        .license(new License()
                                .name("Apache 2.0")
                                .url(swaggerProperties.getContactUrl())));
    }

    @Bean
    public List<Parameter> globalParameters() {
        return List.of(new Parameter()
                .in("header")
                .name("token")
                .description("用户 token")
                .required(false));
    }

    @Bean
    public ApiResponses globalResponses() {
        return new ApiResponses()
                .addApiResponse("404", new ApiResponse().description("未找到"));
    }
}

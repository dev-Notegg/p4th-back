package com.p4th.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /**
     * API 기본 정보 설정 (제목, 버전, 설명 등)
     */
    @Bean
    public OpenAPI customOpenAPI() {
        Info info = new Info()
                .version("v1.0.0")
                .title("P4TH API")
                .description("");

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER).name("Authorization");
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("Authorization"); // "Authorization"를 사용합니다.

        Components components = new Components().addSecuritySchemes("Authorization", securityScheme);


        return new OpenAPI()
                .components(components)
                .addSecurityItem(securityRequirement)
                .info(info);
    }
    /**
     * 모든 API 경로를 포함한 그룹을 정의한다.
     */
    @Bean
    public GroupedOpenApi all() {
        String[] pathsToMatch = {"/api/**"};
        return GroupedOpenApi.builder()
                .group("전체")
                .pathsToMatch(pathsToMatch)
                .build();
    }

    /**
     * 관리자 API 그룹을 정의한다.
     */
    @Bean
    public GroupedOpenApi adminManager() {
        String[] pathsToMatch = {"/api/admin/**"};
        return GroupedOpenApi.builder()
                .group("CMS")
                .pathsToMatch(pathsToMatch)
                .build();
    }

    /**
     * 채팅 API 그룹을 정의한다.
     */
    @Bean
    public GroupedOpenApi chatApi() {
        String[] pathsToMatch = {"/api/chat/**"};
        return GroupedOpenApi.builder()
                .group("채팅")
                .pathsToMatch(pathsToMatch)
                .build();
    }
}

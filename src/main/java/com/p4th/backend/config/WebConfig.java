package com.p4th.backend.config;

import com.p4th.backend.interceptor.AdminAuthorizationInterceptor;
import com.p4th.backend.mapper.AdminUserMapper;
import com.p4th.backend.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtProvider jwtProvider;
    private final AdminUserMapper adminUserMapper;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:3000",
                        "http://p4th.kr",
                        "https://p4th.kr",
                        "https://www.p4th.kr"
                )
                .allowedMethods(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.PATCH.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.OPTIONS.name()
                )
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/WEB-INF/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 관리자 전용 API에 대해 관리자 인증 인터셉터 적용
        registry.addInterceptor(new AdminAuthorizationInterceptor(jwtProvider, adminUserMapper))
                .addPathPatterns("/api/admin/**");
    }

    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }
}

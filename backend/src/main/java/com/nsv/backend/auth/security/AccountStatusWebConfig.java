package com.nsv.backend.auth.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AccountStatusWebConfig implements WebMvcConfigurer {

    private final ObjectProvider<AccountStatusInterceptor> interceptor;

    public AccountStatusWebConfig(ObjectProvider<AccountStatusInterceptor> interceptor) {
        this.interceptor = interceptor;
    }

    /** Dùng ObjectProvider để các test @WebMvcTest không có interceptor vẫn chạy được. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        interceptor.ifAvailable(i -> registry.addInterceptor(i).addPathPatterns("/api/**"));
    }
}

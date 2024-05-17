package org.tinycloud.tinyid.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.tinycloud.tinyid.config.session.AuthenticeInterceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-05-15 10:35
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthenticeInterceptor authenticeInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> excludePath = new ArrayList<>();
        // 开放登录接口
        excludePath.add("/");
        excludePath.add("/auth/login");
        excludePath.add("/auth/getCode");
        // 开放前端静态资源和静态页面
        excludePath.add("/static/**");
        excludePath.add("/api/**");
        excludePath.add("/css/**");
        excludePath.add("/images/**");
        excludePath.add("/js/**");
        excludePath.add("/lib/**");
        excludePath.add("/page/**");
        excludePath.add("/index.html");


        // 注册会话拦截器
        registry.addInterceptor(authenticeInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(excludePath);
    }

    /**
     * 配置静态资源映射
     *
     * @param registry ResourceHandlerRegistry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/lib/**").addResourceLocations("classpath:/static/lib/");
        registry.addResourceHandler("/page/**").addResourceLocations("classpath:/static/page/");
        registry.addResourceHandler("/index.html").addResourceLocations("classpath:/static/");

        // 配置swagger-ui资源映射(knife4j)
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * 这里加这个配置是为了解决Jackson2ObjectMapperBuilderCustomizer自定义配置不生效的问题
     * 参考自：https://www.jianshu.com/p/09169bd31f72
     */
    @Autowired(required = false)
    private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);
        if (Objects.isNull(mappingJackson2HttpMessageConverter)) {
            converters.add(0, new MappingJackson2HttpMessageConverter());
        } else {
            converters.add(0, mappingJackson2HttpMessageConverter);
        }
    }
}

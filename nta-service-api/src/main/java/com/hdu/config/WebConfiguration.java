package com.hdu.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hdu.json.JacksonObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebConfiguration extends WebMvcConfigurationSupport {
    private final ObjectMapper jacksonObjectMapper;
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // 添加拦截器
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {

    }

    /**
     * 生成接口文档
     *
     * @return
     */
    @Bean
    public OpenAPI springShopOpenAPI() {
        log.info("开始生成接口文档...");
        Info info = new Info().title("Traffic-Analysis接口文档").version("1.0.0");
        return new OpenAPI().info(info);
    }

    /**
     * 设置静态资源映射
     *
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
//        log.info("开始设置静态资源映射...");
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

        log.info("扩展消息转换器...");
        // 创建一个消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        // 为消息转换器设置一个对象转换器 将java对象序列化为json数据
        converter.setObjectMapper(jacksonObjectMapper);
        // 将自己的消息转换器加入到容器中
        converters.add(0,converter);
    }

}

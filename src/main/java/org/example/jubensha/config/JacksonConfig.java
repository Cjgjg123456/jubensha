package org.example.jubensha.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.nio.charset.StandardCharsets;

/**
 * Jackson JSON 配置类
 * 确保所有 JSON 序列化都使用 UTF-8 编码，并支持驼峰式命名
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        
        // 禁用空对象序列化失败
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        // 配置驼峰式命名策略（将Java字段名转换为JSON时保持驼峰式）
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        
        return objectMapper;
    }
}
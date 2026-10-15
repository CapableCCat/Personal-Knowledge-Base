package com.pkb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置。
 *
 * <p>key 用 String 序列化（可读性好），value 用 Jackson JSON 序列化。
 * <p>注意：这里显式声明名为 {@code redisTemplate}、泛型为 {@code RedisTemplate<String, Object>} 的 Bean ——
 * Sa-Token 的 {@code sa-token-redis-jackson} 按此泛型注入，Spring Boot 自带的
 * {@code RedisTemplate<Object, Object>} 不匹配该泛型，因此必须由本配置提供。
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        // 用库默认的 GenericJackson2JsonRedisSerializer（带 @class 类型信息），
        // 与 Sa-Token 的 Redis 存储实现保持一致；若后续缓存含 LocalDateTime 的对象，
        // 再改为自定义 ObjectMapper 并注册 JavaTimeModule。
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }
}

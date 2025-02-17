package com.sinohealth.framework.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * redis配置
 */
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {

    @Autowired
    private RedisProperties clusterProperties;


    @Bean
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        FastJson2JsonRedisSerializer serializer = new FastJson2JsonRedisSerializer(Object.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        serializer.setObjectMapper(mapper);

        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());

        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    public RedisClusterConfiguration redisClusterConfiguration() {
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
        Set<RedisNode> hp = new HashSet<>();
        for (String node : clusterProperties.getCluster().getNodes()) {
            String[] split = node.split(":");
            hp.add(new RedisNode(split[0].trim(), Integer.parseInt(split[1])));
        }
        redisClusterConfiguration.setPassword(clusterProperties.getPassword());
        redisClusterConfiguration.setClusterNodes(hp);
        return redisClusterConfiguration;
    }

    public JedisPoolConfig poolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
//        jedisPoolConfig.setMinIdle(0);
//        jedisPoolConfig.setMaxWaitMillis(-1);
//        jedisPoolConfig.setMaxTotal(maxActive);
        jedisPoolConfig.setMaxIdle(8);
        return jedisPoolConfig;
    }

    @Bean
    public JedisConnectionFactory jedisFactory() {
        return new JedisConnectionFactory(redisClusterConfiguration(), poolConfig());
    }


    @Bean(name = "cacheManager")
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration difConf = getDefConf().entryTtl(Duration.ofHours(1));

        //自定义的缓存过期时间配置
        Map<String, RedisCacheConfiguration> redisCacheConfigurationMap = new HashMap<>();
        // key对应 Cacheable 的 cacheNames
        redisCacheConfigurationMap.put(RedisKeys.Cache.ASSET_TREE, getDefConf().entryTtl(Duration.ofSeconds(180)));
        redisCacheConfigurationMap.put(RedisKeys.Cache.STATISTICS, getDefConf().entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(difConf)
                .withInitialCacheConfigurations(redisCacheConfigurationMap)
                .build();
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return (target, method, objects) -> {
            StringBuilder sb = new StringBuilder();
            // 区分不同用户的缓存
            Optional.ofNullable(SecurityUtils.getUserIdIgnoreError()).map(v -> v + ":").ifPresent(sb::append);
            sb.append(target.getClass().getName());
            sb.append(":").append(method.getName()).append(":");
            for (Object obj : objects) {
                sb.append(Optional.ofNullable(obj).map(Object::toString).orElse("null"));
            }
            return sb.toString();
        };
    }

    private RedisCacheConfiguration getDefConf() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> RedisKeys.CACHE_PREFIX + cacheName + ":")
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                ;
    }
}

package com.example.redisstudydemo.redisson;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.*;

/**
 * @author scq
 */
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redisson) {
        return new RedissonConnectionFactory(redisson);
    }

    @Bean
    public Map<Integer , RedissonClient> redissonClientMap(@Value("classpath:/redisson.json") Resource configFile) throws IOException {
        Config config = Config.fromYAML(configFile.getInputStream());
        config.useSingleServer().setDatabase(0);
        Map<Integer , RedissonClient> redissonClientMap = new HashMap();
        //按照需要配置Database和Redissonclient数量
        for(int i = 0;i < 3 ;i++ ) {
            config.useSingleServer().setDatabase(i);
            redissonClientMap.put(i , Redisson.create(config));
        }
        config.useSingleServer().setDatabase(0);
        redissonClientMap.put(999 , Redisson.create(config));
        return redissonClientMap;
    }
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(Map<Integer , RedissonClient> redissonClientMap ,@Value("0") Integer database) throws IOException {
        //默认返回database0的Redissonclient
        return redissonClientMap.get(database);
    }
}

package org.pucar.dristi.caselifecycle.cases.internal.service;

import lombok.extern.slf4j.Slf4j;
import org.pucar.dristi.caselifecycle.cases.internal.config.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service("casesCacheService")
@Slf4j
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private Configuration config;

    public void save(String id, Object value) {
        log.info("Sending data to redis :: {}",value);
        redisTemplate.opsForValue().set(id, value, config.getRedisTimeout(), TimeUnit.MINUTES);
    }

    public Object findById(String id) {
        return redisTemplate.opsForValue().get(id);
    }

    public void delete(String id) {
        redisTemplate.delete(id);
    }
}

package org.tinygame.herostory.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Redis 实用工具类
 */
public final class RedisUtil {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisUtil.class);

    /**
     * Redis 连接池
     */
    private static JedisPool _jedisPool = null;

    /**
     * 私有化类默认构造器
     */
    private RedisUtil() {}

    /**
     * 初始化
     */
    public static void init() {
        try {
            _jedisPool = new JedisPool("192.168.144.200", 6379);
            LOGGER.info("Redis 连接成功");
        } catch (Exception e) {
            // 记录错误日志
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 获取 Redis 实例
     *
     * @return Redis 实例
     */
    public static Jedis getJedis() {
        if (null == _jedisPool) {
            throw new RuntimeException("_jedisPool 尚未初始化");
        }

        Jedis jedis = _jedisPool.getResource();

        return jedis;
    }
}

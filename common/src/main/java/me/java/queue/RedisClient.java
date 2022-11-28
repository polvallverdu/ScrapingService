package me.java.queue;

import io.github.cdimascio.dotenv.Dotenv;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URI;
import java.net.URISyntaxException;

public class RedisClient {

    private final JedisPool jedisPool;
    public static final RedisClient INSTANCE;

    static {
        try {
            INSTANCE = new RedisClient(Dotenv.load().get("REDIS_URI"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(-1);
            throw new RuntimeException(e);
        }
    }

    private RedisClient(String uri) throws URISyntaxException {
        this.jedisPool = new JedisPool(new JedisPoolConfig(), new URI(uri));
    }

    public void add(String key, String value) {
        this.add(key, value, null);
    }

    /**
     *
     * @param key the key
     * @param value the value
     * @param seconds the seconds. Nullable
     */
    public void add(String key, String value, Long seconds) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            jedis.set(key, value);
            if (seconds != null && seconds > 0) {
                jedis.expire(key, seconds);
            }
        }
    }

    public String get(String key, boolean remove) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String data = jedis.get(key);
            if (remove) {
                jedis.del(key);
            }
            return data;
        }
    }


}

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisDB {
    private static JedisPool jedisPool;

    // Initialize Redis connection pool
    static {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10); // Maximum number of connections in the pool
        poolConfig.setMaxIdle(5);  // Maximum number of idle connections
        poolConfig.setMinIdle(1);  // Minimum number of idle connections

        String redisHost = "localhost"; // Replace with your Redis server host
        int redisPort = 6379;           // Default Redis port
        jedisPool = new JedisPool(poolConfig, redisHost, redisPort);
    }

    // Get a Jedis connection
    public static Jedis getJedis() {
        return jedisPool.getResource();
    }

    // Close the Jedis connection pool
    public static void closePool() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}

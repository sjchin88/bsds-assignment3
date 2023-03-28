import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import java.util.concurrent.ExecutionException;

public class RedisDB {
  private static final String REDIS_HOST = "redis://127.0.0.1:6379";
  public static void main(String[] args) throws ExecutionException, InterruptedException {
    RedisClient redisClient = RedisClient.create(REDIS_HOST);
    StatefulRedisConnection<String, String> connection = redisClient.connect();
    //Create asynchronous API
    RedisAsyncCommands<String, String> async = connection.async();
    RedisFuture<String> set = async.set("key", "value");
    RedisFuture<Long> test1 = async.incr("LikesDB:player1");
    RedisFuture<Long> test2 = async.incr("LikesDB:player2");

    RedisFuture<String> get = async.get("key");
    System.out.println(get.get());
    System.out.println(set.get());
    System.out.println(test1.get());
    System.out.println(test2.get());
  }
}

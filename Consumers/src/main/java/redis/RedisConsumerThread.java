package redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;

public abstract class RedisConsumerThread implements Runnable {
  //private static final String REDIS_HOST = "redis://127.0.0.1:6379";
  private static final String REDIS_HOST = "redis://foobared2@54.218.18.155:6379";

  protected StatefulRedisConnection<String, String> redConnection;
  protected RedisAsyncCommands<String, String> redCommand;

  public RedisConsumerThread() {
    RedisClient redisClient = RedisClient.create(REDIS_HOST);
    this.redConnection = redisClient.connect();
    this.redConnection.setAutoFlushCommands(false);
    //Create asynchronous API
    this.redCommand = this.redConnection.async();
  }

  @Override
  public abstract void run();
}

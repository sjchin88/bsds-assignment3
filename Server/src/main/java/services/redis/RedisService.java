package services.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Represent an RedisService class to perform required redis operation
 */
public class RedisService {
  private static final String REDIS_HOST = "redis://127.0.0.1:6379";
  //private static final String REDIS_HOST = "redis://foobared2@54.218.18.155:6379";
  private static final String PREFIX_LIKES_CNT = "Likes:";
  private static final String PREFIX_DISLIKES_CNT = "Dislikes:";
  private static final String PREFIX_SWIPE_REC = "Swiper:";
  private StatefulRedisConnection<String, String> redisConnection;
  private RedisAsyncCommands<String, String> redisCommand;
  private RedisClient redisClient;

  /**
   * Create a new RedisService object with default connection setting
   */
  public RedisService() {
    //Initialized the Redis connection
    this.redisClient = RedisClient.create(REDIS_HOST);
    this.redisConnection = this.redisClient.connect();
    //Create asynchronous API
    this.redisCommand = this.redisConnection.async();
  }

  /**
   * Return the list of potential matches for given swiperID
   * @param swiperID
   * @return List<String> for matches
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public List<String> getMatches(String swiperID) throws ExecutionException, InterruptedException {
    RedisFuture<List<String>> swipeeSet = this.redisCommand.zrange(PREFIX_SWIPE_REC+swiperID, 0, 100);
    return swipeeSet.get();
  }

  /**
   * Return the likes count by the swiper
   * @param swiperID
   * @return int of the likes count
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public int getLikesCount(String swiperID) throws ExecutionException, InterruptedException {
    RedisFuture<String> likeCount = this.redisCommand.get(PREFIX_LIKES_CNT+swiperID);
    int likes = likeCount.get() != null ? Integer.valueOf(likeCount.get()) : 0;
    return likes;
  }

  /**
   * Return the dislikes count by the swiper
   * @param swiperID
   * @return int of the likes count
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public int getDislikesCount(String swiperID) throws ExecutionException, InterruptedException {
    RedisFuture<String> dislikeCount = this.redisCommand.get(PREFIX_DISLIKES_CNT+swiperID);
    int dislikes = dislikeCount.get() != null ? Integer.valueOf(dislikeCount.get()) : 0;
    return dislikes;
  }
}

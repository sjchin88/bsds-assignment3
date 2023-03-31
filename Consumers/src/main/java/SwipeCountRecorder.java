import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import rabbitmq.SwipeCountThread;
import redis.RedisSwipeCountThread;

/**
 * Driver to record the likes and dislikes count from the swiper
 */
public class SwipeCountRecorder extends Recorder{
  /**
   * Default queue name
   */
  protected static final String QUEUE_NAME = "swipesrecord";
  private static final String[] BINDING_KEYS = new String[] {"right", "left"};

  /**
   * Main method, take in the number of thread as first argument and server address as second argument
   * if there is problem parsing any of this the default will be used
   * @param argv CLI arguments
   * @throws Exception
   */
  public static void main(String[] argv) throws Exception {
    int numThread;
    try{
      numThread = Integer.valueOf(argv[0]);
    } catch (Exception e){
      numThread = NUM_THREAD;
    }
    int numReddisThread;
    try{
      numReddisThread = Integer.valueOf(argv[1]);
    } catch (Exception e){
      numReddisThread = NUM_REDDIS_THREAD;
    }
    int prefetchCount;
    try{
      prefetchCount = Integer.valueOf(argv[2]);
    } catch (Exception e){
      prefetchCount = PREFETCH_COUNT;
    }

    Connection rabbitConnection = createConnection();
    BlockingQueue<String> buffer = new LinkedBlockingDeque<>(BUFFER_SIZE);

    for(int i = 0; i < numThread; i++){
      Thread thread = new Thread(new SwipeCountThread(rabbitConnection, EXCHANGE_NAME, EXCHANGE_TYPE, QUEUE_NAME, BINDING_KEYS, buffer, prefetchCount));
      thread.start();
    }
    for(int i = 0; i < numReddisThread; i++){
      Thread redisThread = new Thread(new RedisSwipeCountThread(buffer));
      redisThread.start();
    }
  }
}

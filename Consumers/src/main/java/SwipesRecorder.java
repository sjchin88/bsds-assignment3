import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Driver to record the likes and dislikes count from the swiper
 */
public class SwipesRecorder extends Recorder{
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
    String serverAddr;
    try{
      serverAddr = argv[1];
    } catch (Exception e){
      serverAddr = SERVER_ADDR;
    }
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(serverAddr);
    factory.setUsername(ADMIN_NAME);
    factory.setPassword(ADMIN_PASS);
    Connection connection = factory.newConnection();
    ConcurrentHashMap<Integer, Integer> likeDB = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, Integer> dislikeDB = new ConcurrentHashMap<>();
    for(int i = 0; i < numThread; i++){
      Thread thread = new Thread(new SwipeThread(connection, EXCHANGE_NAME, EXCHANGE_TYPE, QUEUE_NAME, BINDING_KEYS, likeDB, dislikeDB));
      thread.start();
    }

    // Code for checking and debug
    /*System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
    while (true){
      Thread.sleep(100_000);
      printDb(likeDB);
      printDb(dislikeDB);
    }*/
  }

  /**
   * Static method to print the content of the concurrent hashmap, use for checking and debug
   * @param db ConcurrentHashMap<Integer, Integer>
   */
  public static void printDb(ConcurrentHashMap<Integer, Integer> db){
    int total = 0;
    for(Entry<Integer, Integer> entry: db.entrySet()) {
      System.out.println(entry);
      total += entry.getValue();
    }
    System.out.println("Total equals:" + total);
  }
}

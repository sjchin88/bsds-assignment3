import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Recorder class provide basic function to listen to the channels
 */
public class Recorder {
  /**
   * Default exchange name
   */
  protected static final String EXCHANGE_NAME = "swipes";
  /**
   * Default exchange type
   */
  protected static final String EXCHANGE_TYPE = "direct";

  /**
   * Default number of thread
   */
  protected static final int NUM_THREAD = 20;
  protected static final int NUM_REDDIS_THREAD = 3;
  protected static final int PREFETCH_COUNT = 20;
  protected static final int BUFFER_SIZE = 5_000_000;
  /**
   * Default server address
   */
  protected static String RABBIT_HOST = "localhost";
  //protected static String RABBIT_HOST = "35.165.32.0";
  //protected static String ADMIN_NAME = "csj";
  //protected static String ADMIN_PASS = "Gu33ssm3";

  public static Connection createConnection() throws IOException, TimeoutException {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(RABBIT_HOST);
    //factory.setUsername(ADMIN_NAME);
    //factory.setPassword(ADMIN_PASS);
    return factory.newConnection();
  }
}

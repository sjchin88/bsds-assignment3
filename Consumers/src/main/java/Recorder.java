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
  protected static final int BUFFER_SIZE = 500_000;
  /**
   * Default server address
   */
  protected static String RABBIT_HOST = "localhost";
  //protected static String SERVER_ADDR = "35.165.32.0";
  //protected static String ADMIN_NAME = "csj";
  //protected static String ADMIN_PASS = "Gu33ssm3";

  protected static final String REDIS_HOST = "redis://127.0.0.1:6379";
}

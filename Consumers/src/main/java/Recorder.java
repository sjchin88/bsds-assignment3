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
  protected static final int NUM_THREAD = 10;
  /**
   * Default server address
   */
  protected static String SERVER_ADDR = "localhost";
  //protected static String ADMIN_NAME = "csj";
  //protected static String ADMIN_PASS = "Gu33ssm3";

  protected static final String REDIS_HOST = "redis://127.0.0.1:6379";
}

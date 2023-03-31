package apache;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;

public abstract class ApacheAsyncClient implements Runnable{
  protected CloseableHttpAsyncClient httpClient;
  protected String url;
  protected Integer numRequest;
  protected static final int LOWER_BOUND = 0;
  protected static final int SWIPER_UPPER = 5_001;
  protected static final int SWIPEE_UPPER = 1_000_001;
  protected Random random;
  protected BlockingQueue<long[]> memoryBuffer;

  /**
   * Get a random number in string based on limit
   *
   * @param limit  int limit (exclusive)
   * @return random number in string
   */
  public String getRandomNum(int limit){
    return String.valueOf(random.nextInt(limit));
  }
}

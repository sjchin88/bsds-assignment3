import java.io.IOException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class PoolClient implements Runnable{
  protected CloseableHttpClient httpClient;
  protected String url;
  protected Integer numRequest;
  protected CountDownLatch countDownLatch;
  // protected String[] comments;
  protected static final String URL_LEFT = "/left";
  protected static final String URL_RIGHT = "/right";
  protected static final int LOWER_BOUND = 0;
  protected static final int SWIPER_UPPER = 5_001;
  protected static final int SWIPEE_UPPER = 1_000_001;
  protected static final int VALID_RESPONSECODE = 201;
  protected static final int SEND_LIMIT = 5;
  protected int countSuccess;
  protected int countFailure;
  protected Counter counter;
  protected Random random;
  protected RandomStringUtils randomStringUtils;
  protected BlockingQueue<long[]> memoryBuffer;

  public PoolClient(String url, CloseableHttpClient httpClient, Integer numRequest,
      CountDownLatch countDownLatch,
      Counter counter, BlockingQueue<long[]> memoryBuffer) {
    this.url = url;
    this.numRequest = numRequest;
    this.countDownLatch = countDownLatch;
    this.counter = counter;
    this.memoryBuffer = memoryBuffer;
    this.countSuccess = 0;
    this.countFailure = 0;
    this.random = new Random();
    this.randomStringUtils = new RandomStringUtils();

    //Build the CloseableHttpClient object using the build() method.
    this.httpClient = httpClient;
  }

  /**
   * Build a randomly generated JSON containing the
   * swiper id, swipee id and comments
   * @return JSON object in String
   */
  public String buildJson(){
    StringBuilder sb = new StringBuilder();
    sb.append("{ \"swiper\":");
    sb.append(this.getRandomNum(SWIPER_UPPER));
    sb.append(", \"swipee\":");
    sb.append(this.getRandomNum(SWIPEE_UPPER));
    sb.append(", \"comment\":\"");
    sb.append(this.randomStringUtils.randomAlphabetic(256));
    sb.append("\"}");
    return sb.toString();
  }

  /**
   * Get a random number in string based on limit
   *
   * @param limit  int limit (exclusive)
   * @return random number in string
   */
  public String getRandomNum(int limit){
    return String.valueOf(random.nextInt(limit));
  }

  /**
   * Build a http request
   * @return http request built
   */
  public HttpPost buildRequest(){
    int ranNum = this.random.nextInt(2);
    String swipeDirection = URL_LEFT;
    if (ranNum == 0){
      swipeDirection = URL_RIGHT;
    }
    String url_new = url + swipeDirection;
    HttpPost post = new HttpPost(url_new);
    StringEntity entity = new StringEntity(this.buildJson());
    post.setEntity(entity);
    post.setHeader("Accept", "application/json");
    post.setHeader("Content-type", "application/json");
    return post;
  }

  @Override
  public void run() {
    for(int i = 0; i < this.numRequest; i++){
      this.sendRequest(-1, this.buildRequest());
    }
    this.counter.addCount(this.countSuccess, this.countFailure);
    this.countDownLatch.countDown();
  }

  /**
   * Send the http request as long as count is within SEND_LIMIT
   * @param count count of how many time this request have been resend
   */
  public void sendRequest(int count, HttpPost httpPost){
    int sendCount = count;
    try {
      long start = System.currentTimeMillis();
      CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
      long end = System.currentTimeMillis();
      int statusCode = httpResponse.getCode();
      EntityUtils.consume(httpResponse.getEntity());

      if(httpResponse==null){
        sendCount ++;
        this.countFailure ++;
        if(sendCount < SEND_LIMIT){
          this.sendRequest(sendCount, httpPost);
        }
      } else {
        long[] record = new long[]{start, end, statusCode};
        this.memoryBuffer.put(record);
        if(statusCode != VALID_RESPONSECODE){
          sendCount ++;
          this.countFailure ++;
          if(sendCount < SEND_LIMIT){
            this.sendRequest(sendCount, httpPost);
          }
        }else {
          this.countSuccess ++;
        }
      }
    } catch (IOException | InterruptedException e) {
      sendCount ++;
      this.countFailure ++;
      if(sendCount < SEND_LIMIT){
        this.sendRequest(sendCount, httpPost);
      }
    }
  }
}

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.core5.http.ContentType;

public class RequestProducer implements Runnable{
  protected String url;
  protected Integer numRequest;
  protected static final String URL_LEFT = "/left";
  protected static final String URL_RIGHT = "/right";
  protected static final int LOWER_BOUND = 0;
  protected static final int SWIPER_UPPER = 51;
  protected static final int SWIPEE_UPPER = 1_01;
  protected Random random;
  protected RandomStringUtils randomStringUtils;
  protected BlockingQueue<SimpleHttpRequest> requestBuffer;

  public RequestProducer(String url, Integer numRequest,
      BlockingQueue<SimpleHttpRequest> requestBuffer) {
    this.url = url;
    this.numRequest = numRequest;
    this.requestBuffer = requestBuffer;
    this.random = new Random();
    this.randomStringUtils = new RandomStringUtils();
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
  public SimpleHttpRequest buildRequest(){
    int ranNum = this.random.nextInt(2);
    String swipeDirection = URL_LEFT;
    if (ranNum == 0){
      swipeDirection = URL_RIGHT;
    }
    String url_new = url + swipeDirection;
    SimpleHttpRequest post = SimpleRequestBuilder.post(url_new)
        .setBody(this.buildJson(), ContentType.APPLICATION_JSON).build();
    return post;
  }

  @Override
  public void run() {
    for(int i = 0; i < this.numRequest; i++){
      SimpleHttpRequest request = this.buildRequest();
      boolean success = false;
      while(!success){
        success = this.requestBuffer.offer(request);
        if(!success){
          try {
            Thread.sleep(1);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
  }
}

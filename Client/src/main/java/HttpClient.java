import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.lang3.RandomStringUtils;
import utility.Counter;

/**
 * HttpClient class implements Runnable for part II,
 * To create a http Post request with randomly generated direction (left/right)
 * randomly generated swiper id and swipee id, and randomly chosen string comments
 */
public class HttpClient implements Runnable{
  protected String url;
  protected static final java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder().version(Version.HTTP_2)
      .connectTimeout(Duration.ofSeconds(1000))
      .build();
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

  /**
   * Create new HttpClient based on given parameters
   * @param url url destination of the server
   * @param numRequest  number of request to be send
   * @param countDownLatch  countDownLatch to manage concurrency
   * @param counter   utility.Counter object used to sum the count of success and unsuccess requests across threads
   */
  public HttpClient(String url, Integer numRequest, CountDownLatch countDownLatch, Counter counter) {
    this.url = url;
    this.numRequest = numRequest;
    this.countDownLatch = countDownLatch;
    // this.comments = comments;
    this.countSuccess = 0;
    this.countFailure = 0;
    this.counter = counter;
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
   * Override run method for thread run
   */
  @Override
  public void run() {
    for(int i = 0; i < this.numRequest; i++){
      this.sendRequest(-1, this.buildRequest());
    }
    this.counter.addCount(this.countSuccess, this.countFailure);
    this.countDownLatch.countDown();
  }

  /**
   * Build a http request
   * @return http request built
   */
  public HttpRequest buildRequest(){
    int ranNum = this.random.nextInt(2);
    String swipeDirection = URL_LEFT;
    if (ranNum == 0){
      swipeDirection = URL_RIGHT;
    }
    String url_new = url + swipeDirection;
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url_new))
        .header("Content-Type", "application/json")
        .timeout(Duration.ofSeconds(120))
        .POST(HttpRequest.BodyPublishers.ofString(this.buildJson()))
        .build();
    return request;
  }

  /**
   * Send the http request as long as count is within SEND_LIMIT
   * @param count count of how many time this request have been resend
   * @param request http request
   */
  public void sendRequest(int count, HttpRequest request){
    int sendCount = count;
    try {
      //CompletableFuture<HttpResponse<String>> future = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
      //HttpResponse<String> responseAsync = future.get();
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if(response==null || response.statusCode()!=VALID_RESPONSECODE){
        sendCount ++;
        this.countFailure ++;
        if(sendCount < SEND_LIMIT){
          this.sendRequest(sendCount, request);
        }
      }else {
        this.countSuccess ++;
      }
    } catch (IOException | InterruptedException e) {
      sendCount ++;
      this.countFailure ++;
      if(sendCount < SEND_LIMIT){
        this.sendRequest(sendCount, request);
      }
    }
  }
}

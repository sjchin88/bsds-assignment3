package apache;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.http.HttpResponse;
import utility.Counter;

public class GetClient extends ApacheAsyncClient {
  protected static final String URL_MATCHES = "/matches/";
  protected static final String URL_STATS = "/stats/";

  public GetClient(String url,
      CloseableHttpAsyncClient httpClient,
      Integer numRequest,
      BlockingQueue<long[]> memoryBuffer) {
    this.url = url;
    this.numRequest = numRequest;
    this.memoryBuffer = memoryBuffer;
    this.random = new Random();
    this.httpClient = httpClient;
  }

  /**
   * Build a http get request
   * @return http request built
   */
  public SimpleHttpRequest buildGetRequest(){
    int ranNum = this.random.nextInt(2);
    String getPrefix = URL_MATCHES;
    if (ranNum == 0){
      getPrefix = URL_STATS;
    }
    //Construct the url, using base url + prefix + the random swiper id (integer represent in String)
    String url_new = url + getPrefix + this.getRandomNum(SWIPER_UPPER);
    SimpleHttpRequest get = SimpleRequestBuilder.get(url_new).build();
    return get;
  }

  @Override
  public void run() {
    try{
      while(true){
        for(int i = 0; i < numRequest; i++){
          this.sendRequest(this.buildGetRequest());
        }
        Thread.sleep(1000);
      }
    } catch (InterruptedException e) {
      System.out.println("get thread exiting");
    }
  }

  /**
   * Send the getRequest and record the latency into the memory buffer
   * @param getRequest
   */
  public void sendRequest(SimpleHttpRequest getRequest){
    try {
      long start = System.currentTimeMillis();
      Future<SimpleHttpResponse> future = httpClient.execute(getRequest, null);
      // and wait until response is received
      HttpResponse response = future.get();
      long end = System.currentTimeMillis();
      int statusCode = response.getCode();
      long[] record = new long[]{start, end, statusCode};
      this.memoryBuffer.put(record);
    } catch (InterruptedException | ExecutionException e) {
      //Do nothing
    }
  }
}

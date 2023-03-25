import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.http.HttpResponse;

public class RequestSender implements Runnable{
  protected CloseableHttpAsyncClient httpClient;
  protected CountDownLatch sendCount;
  protected CountDownLatch threadCount;
  protected static final int VALID_RESPONSECODE = 201;
  protected static final int SEND_LIMIT = 5;
  protected int countSuccess;
  protected int countFailure;
  protected Counter counter;
  protected BlockingQueue<SimpleHttpRequest> requestBuffer;
  protected BlockingQueue<long[]> memoryBuffer;

  public RequestSender(CloseableHttpAsyncClient httpClient, CountDownLatch sendCount, CountDownLatch threadCount,
       Counter counter, BlockingQueue<SimpleHttpRequest> requestBuffer, BlockingQueue<long[]> memoryBuffer) {
    this.httpClient = httpClient;
    this.sendCount = sendCount;
    this.threadCount = threadCount;
    this.countSuccess = 0;
    this.countFailure = 0;
    this.counter = counter;
    this.requestBuffer = requestBuffer;
    this.memoryBuffer = memoryBuffer;
  }

  @Override
  public void run() {
    while(this.sendCount.getCount() > 0){
      SimpleHttpRequest request = requestBuffer.poll();
      if(request!=null){
        this.sendRequest(-1, request);
        //this.counter.addSend();
        this.sendCount.countDown();
        System.out.println(this.sendCount.getCount());
      } else {
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
    this.counter.addCount(this.countSuccess, this.countFailure);
    this.threadCount.countDown();
  }

  /**
   * Send the http request as long as count is within SEND_LIMIT
   * @param count count of how many time this request have been resend
   */
  public void sendRequest(int count, SimpleHttpRequest httpPost){
    int sendCount = count;
    try {

      long start = System.currentTimeMillis();
      // CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
      Future<SimpleHttpResponse> future = httpClient.execute(httpPost, null);
      // and wait until response is received
      HttpResponse response = future.get();
      long end = System.currentTimeMillis();
      int statusCode = response.getCode();
      // EntityUtils.consume(httpResponse.getEntity());
      if(response==null){
        sendCount ++;
        this.counter.addFail();
        if(sendCount < SEND_LIMIT){
          this.sendRequest(sendCount, httpPost);
        }
      } else {
        long[] record = new long[]{start, end, statusCode};
        this.memoryBuffer.put(record);
        if(statusCode != VALID_RESPONSECODE){
          sendCount ++;
          this.counter.addFail();
          if(sendCount < SEND_LIMIT){
            this.sendRequest(sendCount, httpPost);
          }
        }else {
          this.counter.addSuccess();
        }
      }
    } catch (InterruptedException | ExecutionException e) {
      sendCount ++;
      this.counter.addFail();
      if(sendCount < SEND_LIMIT){
        this.sendRequest(sendCount, httpPost);
      }
    }
  }
}

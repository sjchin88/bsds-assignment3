import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class HttpAsyncClient extends HttpClient{
  private BlockingQueue<long[]> memoryBuffer;

  /**
   * Create a new Http Async Client based on given parameters
   * @param url Url of server
   * @param numRequest  number of request per thread
   * @param countDownLatch countDownLatch for each thread
   * @param memoryBuffer memory buffer of BlockingQueue to store the record of start time, latency (start - end) and response code
   * @param counter Counter object used to sum the count of success and unsuccess requests across threads
   */
  public HttpAsyncClient(String url, Integer numRequest, CountDownLatch countDownLatch,
      Counter counter, BlockingQueue<long[]> memoryBuffer) {
    super(url, numRequest, countDownLatch, counter);
    this.memoryBuffer = memoryBuffer;
  }

  /**
   * Send the http request as long as count is within SEND_LIMIT
   * further processing include recording the response time of each request,
   * and store the start, end and response.statusCode into the blockingQueue memoryBuffer
   * @param count count of how many time this request have been resend
   * @param request http request
   */
  public void sendRequest(int count, HttpRequest request){
    int sendCount = count;
    try {
      long start = System.currentTimeMillis();
      // System.out.println("Sending response");
      CompletableFuture<HttpResponse<String>> future = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
      // HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      String body = future.thenApply(HttpResponse::body).get();
      int statusCode = future.thenApply(HttpResponse::statusCode).get();
      // System.out.println("response get");
      long end = System.currentTimeMillis();
      if(body==null){
        sendCount ++;
        this.countFailure ++;
        if(sendCount < SEND_LIMIT){
          this.sendRequest(sendCount, request);
        }
      } else {
        long[] record = new long[]{start, end, statusCode};
        this.memoryBuffer.put(record);
        if(statusCode != VALID_RESPONSECODE){
          sendCount ++;
          this.countFailure ++;
          if(sendCount < SEND_LIMIT){
            this.sendRequest(sendCount, request);
          }
        }else {
          this.countSuccess ++;
        }
      }

    } catch (InterruptedException e) {
      sendCount ++;
      if(sendCount < SEND_LIMIT){
        this.sendRequest(sendCount, request);
      }
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
}

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * HttpClient class implements Runnable for part III,
 * To create a http Post request with randomly generated direction (left/right)
 * randomly generated swiper id and swipee id, and randomly chosen string comments
 * and record the start time, latency (end - start time) and response code for each request
 * and store it into the blocking queue
 */
public class HttpRecordingClient extends HttpClient{
  private BlockingQueue<long[]> memoryBuffer;
  /**
   * Create a new Http Recording Client based on given parameters
   * @param url Url of server
   * @param numRequest  number of request per thread
   * @param countDownLatch countDownLatch for each thread
   * @param memoryBuffer memory buffer of BlockingQueue to store the record of start time, latency (start - end) and response code
   * @param counter Counter object used to sum the count of success and unsuccess requests across threads
   */
  public HttpRecordingClient(String url, Integer numRequest, CountDownLatch countDownLatch,
       BlockingQueue<long[]> memoryBuffer, Counter counter) {
    super(url,numRequest,countDownLatch,counter);
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
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      long end = System.currentTimeMillis();
      if(response==null){
        sendCount ++;
        this.countFailure ++;
        if(sendCount < SEND_LIMIT){
          this.sendRequest(sendCount, request);
        }
      } else {
        long[] record = new long[]{start, end, response.statusCode()};
        this.memoryBuffer.put(record);
        if(response.statusCode() != VALID_RESPONSECODE){
          sendCount ++;
          this.countFailure ++;
          if(sendCount < SEND_LIMIT){
            this.sendRequest(sendCount, request);
          }
        }else {
          this.countSuccess ++;
        }
      }

    } catch (IOException e) {
      sendCount ++;
      if(sendCount < SEND_LIMIT){
        this.sendRequest(sendCount, request);
      }
    } catch (InterruptedException e) {
      sendCount ++;
      if(sendCount < SEND_LIMIT){
        this.sendRequest(sendCount, request);
      }
    }
  }
}

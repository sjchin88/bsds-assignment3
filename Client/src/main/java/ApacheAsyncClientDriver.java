import apache.GetClient;
import apache.PostClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import utility.Counter;
import utility.RecordProcessor;

/**
 * Apache Client with Pool Connection management,
 * Asynchronous response
 * One client for all threads
 */
public class ApacheAsyncClientDriver extends ClientDriver{
  /**
   * Main method to initiate the threads
   * @param args from CLI, not required at here
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException, IOException {
    System.out.println("Apache Client Start Running");
    List<String[]> runRecords = new ArrayList<>();
    final String[] headers = new String[]{"Number of Threads", "Number of requests", "Time taken", "Throughput per second"};
    runRecords.add(headers);
    RecordProcessor recordProcessor = new RecordProcessor();
    for (int idx = 0; idx < 2; idx++){
      //Initialize common variables and objects required
      int numthreads = NUMTHREADS_LIST[idx];
      int totalRequests = REQUEST_LIST[idx];
      int requestPerThread = totalRequests / numthreads;
      CountDownLatch countDownLatch = new CountDownLatch(numthreads);
      Counter counter = new Counter();
      BlockingQueue<long[]> postRecBuffer = new LinkedBlockingDeque<>(totalRequests);
      BlockingQueue<long[]> getRecBuffer = new LinkedBlockingDeque<>(totalRequests/100);

      //Build the HTTP Client
      CloseableHttpAsyncClient httpClient = buildClient(numthreads);
      httpClient.start();

      Long start = System.currentTimeMillis();
      for (int i = 0; i < numthreads; i++){
        Thread thread = new Thread(new PostClient(URL, httpClient, requestPerThread, countDownLatch, counter, postRecBuffer));
        thread.start();
      }
      Thread getThread = new Thread(new GetClient(URL,httpClient,5,getRecBuffer));
      getThread.start();
      countDownLatch.await();
      Long end = System.currentTimeMillis();
      getThread.interrupt();
      Long timeTaken = end - start;
      String outputname = "apacheasyncclient-"+numthreads+"-request-"+totalRequests;
      recordProcessor.processData(counter,timeTaken,idx,numthreads,totalRequests,postRecBuffer,getRecBuffer,outputname,runRecords);
    }
    recordProcessor.storeResult(runRecords, "RecordingSummary.csv");
  }

  /**
   * Static method to build the Apache Async Client
   * @param numthreads number of threads in total
   * @return the CloseableHttpAsyncClient object
   */
  public static CloseableHttpAsyncClient buildClient(int numthreads){
    //Creating the Client Connection Pool Manager by instantiating the PoolingHttpClientConnectionManager class.
    PoolingAsyncClientConnectionManager connManager = PoolingAsyncClientConnectionManagerBuilder.create().build();

    //Set the maximum number of connections in the pool
    int max = numthreads;
    if(numthreads > 200){
      max = (int) Math.min(numthreads*0.7, 200);
    }
    connManager.setMaxTotal(max);
    connManager.setDefaultMaxPerRoute(max);

    //Create a ClientBuilder Object by setting the connection manager
    HttpAsyncClientBuilder clientbuilder = HttpAsyncClients.custom().setConnectionManager(connManager);

    //Build the CloseableHttpClient object using the build() method.
    CloseableHttpAsyncClient httpClient = clientbuilder.build();
    return httpClient;
  }
}

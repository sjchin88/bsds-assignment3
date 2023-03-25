import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;

/**
 * Apache Client with Pool Connection management,
 * Synchronous response
 * Single Client for all threads
 */
public class ClientPoolDriver extends ClientDriver{
  /**
   * Main method to initiate the threads
   * @param args from CLI, not required at here
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException, IOException {
    System.out.println("Pool run with recording to Server");
    List<String[]> runRecords = new ArrayList<>();
    final String[] headers = new String[]{"Number of Threads", "Number of requests", "Time taken", "Throughput per second"};
    runRecords.add(headers);
    for (int idx = 0; idx < NUMTHREADS_LIST.length; idx++){
      int numthreads = NUMTHREADS_LIST[idx];
      int totalRequests = REQUEST_LIST[idx];
      CountDownLatch countDownLatch = new CountDownLatch(numthreads);
      Counter counter = new Counter();
      int requestPerThread = totalRequests / numthreads;
      BlockingQueue<long[]> memoryBuffer = new LinkedBlockingDeque<>(totalRequests);
      Long start = System.currentTimeMillis();
      //Creating the Client Connection Pool Manager by instantiating the PoolingHttpClientConnectionManager class.
      PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();

      //Set the maximum number of connections in the pool
      int max = Math.min(numthreads, 200);
      connManager.setMaxTotal(max);
      connManager.setDefaultMaxPerRoute(max);

      //Create a ClientBuilder Object by setting the connection manager
      HttpClientBuilder clientbuilder = HttpClients.custom().setConnectionManager(connManager);

      //Build the CloseableHttpClient object using the build() method.
      CloseableHttpClient httpClient = clientbuilder.build();

      for (int i = 0; i < numthreads; i++){
        Thread thread = new Thread(new PoolClient(URL, httpClient, requestPerThread, countDownLatch, counter, memoryBuffer));
        thread.start();
      }
      countDownLatch.await();
      Long end = System.currentTimeMillis();
      Long timeTaken = end - start;
      printOutput(counter, timeTaken, idx, runRecords);
      String outputname = "pool-numthread-"+numthreads+"request-"+totalRequests;
      RecordProcessor recordProcessor = new RecordProcessor(memoryBuffer, outputname);
      recordProcessor.processData();
    }
    RecordProcessor recordProcessor = new RecordProcessor(null, null);
    recordProcessor.storeResult(runRecords, "PoolRunsWithRecordingSummary.csv");
    // ClientDriver.main(new String[] {});
  }
}

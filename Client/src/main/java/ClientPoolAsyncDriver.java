import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.nio.AsyncClientConnectionManager;

/**
 * Apache Client with Pool Connection management,
 * Asynchronous response
 * One client per thread
 */
public class ClientPoolAsyncDriver extends ClientDriver {
  /**
   * Main method to initiate the threads
   * @param args from CLI, not required at here
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException, IOException {
    System.out.println("Pool async run with recording to Server");
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
      PoolingAsyncClientConnectionManager connManager = PoolingAsyncClientConnectionManagerBuilder.create().build();

      //Set the maximum number of connections in the pool
      int max = Math.min(numthreads, 200);
      connManager.setMaxTotal(max);
      connManager.setDefaultMaxPerRoute(max);


      for (int i = 0; i < numthreads; i++){
        Thread thread = new Thread(new PoolAsyncClient(URL, connManager, requestPerThread, countDownLatch, counter, memoryBuffer));
        thread.start();
      }
      countDownLatch.await();
      Long end = System.currentTimeMillis();
      Long timeTaken = end - start;
      printOutput(counter, timeTaken, idx, runRecords);
      String outputname = "pool-async-numthread-"+numthreads+"request-"+totalRequests;
      RecordProcessor recordProcessor = new RecordProcessor(memoryBuffer, outputname);
      recordProcessor.processData();
    }
    RecordProcessor recordProcessor = new RecordProcessor(null, null);
    recordProcessor.storeResult(runRecords, "PoolAsyncRunsWithRecordingSummary.csv");
    // ClientDriver.main(new String[] {});
  }
}

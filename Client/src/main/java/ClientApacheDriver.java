import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

/**
 * Driver to test for Apache Http Client using synchronous method
 *
 */
public class ClientApacheDriver extends ClientDriver{
  /**
   * Main method to initiate the threads
   * @param args from CLI, not required at here
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException, IOException {
    System.out.println("Apache run with recording to Server");
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

      //Build the CloseableHttpClient object using the build() method.
      CloseableHttpClient httpClient = HttpClients.createDefault();

      for (int i = 0; i < numthreads; i++){
        Thread thread = new Thread(new PoolClient(URL, httpClient, requestPerThread, countDownLatch, counter, memoryBuffer));
        thread.start();
      }
      countDownLatch.await();
      Long end = System.currentTimeMillis();
      Long timeTaken = end - start;
      printOutput(counter, timeTaken, idx, runRecords);
      String outputname = "apache-numthread-"+numthreads+"request-"+totalRequests;
      RecordProcessor recordProcessor = new RecordProcessor(memoryBuffer, outputname);
      recordProcessor.processData();
    }
    RecordProcessor recordProcessor = new RecordProcessor(null, null);
    recordProcessor.storeResult(runRecords, "ApacheRunsWithRecordingSummary.csv");
  }
}

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;

public class ProducerConsumerClientDriver extends ClientDriver{
  /**
   * Main method to initiate the threads
   * @param args from CLI, not required at here
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException, IOException {
    System.out.println("Producer Consumer with recording to Server");
    List<String[]> runRecords = new ArrayList<>();
    final String[] headers = new String[]{"Number of Threads", "Number of requests", "Time taken", "Throughput per second"};
    runRecords.add(headers);
    for (int idx = 0; idx < 3; idx++){
      int numthreads = NUMTHREADS_LIST[idx];
      int totalRequests = REQUEST_LIST[idx];
      CountDownLatch sendCount = new CountDownLatch(totalRequests);
      CountDownLatch threadCount = new CountDownLatch(numthreads);
      Counter counter = new Counter(totalRequests);

      BlockingQueue<long[]> memoryBuffer = new LinkedBlockingDeque<>(totalRequests);
      BlockingQueue<SimpleHttpRequest> requestBuffer = new LinkedBlockingDeque<>(totalRequests);

      //Creating the Client Connection Pool Manager by instantiating the PoolingHttpClientConnectionManager class.
      PoolingAsyncClientConnectionManager connManager = PoolingAsyncClientConnectionManagerBuilder.create().build();

      //Set the maximum number of connections in the pool
      int max = Math.min(numthreads, 200);
      connManager.setMaxTotal(max);
      connManager.setDefaultMaxPerRoute(max);
      //Create a ClientBuilder Object by setting the connection manager
      HttpAsyncClientBuilder clientbuilder = HttpAsyncClients.custom().setConnectionManager(connManager);

      //Build the CloseableHttpClient object using the build() method.
      CloseableHttpAsyncClient httpClient = clientbuilder.build();
      httpClient.start();
      Long start = System.currentTimeMillis();
      int requestPerThread = totalRequests / numthreads;
      for (int i = 0; i < numthreads; i++){
        Thread thread = new Thread(new RequestProducer(URL, requestPerThread,requestBuffer));
        thread.start();
        Thread thread2 = new Thread(new RequestSender(httpClient,sendCount, threadCount ,counter, requestBuffer, memoryBuffer));
        thread2.start();
      }
      threadCount.await();
      Long end = System.currentTimeMillis();
      Long timeTaken = end - start;
      printOutput(counter, timeTaken, idx, runRecords);
      String outputname = "prod-consum-numthread-"+numthreads+"request-"+totalRequests;
      RecordProcessor recordProcessor = new RecordProcessor(memoryBuffer, outputname);
      recordProcessor.processData();
    }
    RecordProcessor recordProcessor = new RecordProcessor(null, null);
    recordProcessor.storeResult(runRecords, "ProdConsumerRecordingSummary.csv");
    // ClientDriver.main(new String[] {});
  }
}

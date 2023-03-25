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

public class ApachePoolTestDriver extends ClientDriver{

  /**
   * Main method to initiate the threads
   * @param args from CLI, not required at here
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException, IOException {
    System.out.println("Find Best combination of connection pool size and number of thread to server");
    List<String[]> runRecords = new ArrayList<>();
    final String[] headers = new String[]{"Number of Threads", "Number of Connections", "Number of requests", "Time taken", "Throughput per second"};
    runRecords.add(headers);
    for (int idx = 0; idx < NUMTHREADS_LIST.length; idx++){
      int numthreads = NUMTHREADS_LIST[idx];
      int totalRequests = REQUEST_LIST[idx];
      for(int maxConnections = 100; maxConnections <= Math.max(100 , numthreads); maxConnections += 100){
        CountDownLatch countDownLatch = new CountDownLatch(numthreads);
        Counter counter = new Counter();
        int requestPerThread = totalRequests / numthreads;
        BlockingQueue<long[]> memoryBuffer = new LinkedBlockingDeque<>(totalRequests);

        //Creating the Client Connection Pool Manager by instantiating the PoolingHttpClientConnectionManager class.
        PoolingAsyncClientConnectionManager connManager = PoolingAsyncClientConnectionManagerBuilder.create().build();

        //Set the maximum number of connections in the pool
        int max = Math.min(numthreads, maxConnections);
        connManager.setMaxTotal(max);
        connManager.setDefaultMaxPerRoute(max);
        //Create a ClientBuilder Object by setting the connection manager
        HttpAsyncClientBuilder clientbuilder = HttpAsyncClients.custom().setConnectionManager(connManager);

        //Build the CloseableHttpClient object using the build() method.
        CloseableHttpAsyncClient httpClient = clientbuilder.build();
        httpClient.start();

        Long start = System.currentTimeMillis();
        for (int i = 0; i < numthreads; i++){
          Thread thread = new Thread(new PoolAsyncSingleClient(URL, httpClient, requestPerThread, countDownLatch, counter, memoryBuffer));
          thread.start();
        }
        countDownLatch.await();
        Long end = System.currentTimeMillis();
        Long timeTaken = end - start;
        String outputname = "Combo-numthread-"+numthreads+"numConn"+max+"request-"+totalRequests;
        RecordProcessor recordProcessor = new RecordProcessor(memoryBuffer, outputname);
        List<String> stats = recordProcessor.processAndStoreData();
        printOutput(counter, timeTaken, idx, max, runRecords, stats);
        recordProcessor.printStatistic();
      }
    }
    RecordProcessor recordProcessor = new RecordProcessor(null, null);
    recordProcessor.storeResult(runRecords, "ApachePoolComboSummary.csv");
    // ClientDriver.main(new String[] {});
  }

  /**
   * Method to print the output after finished sending the request
   *
   * @param counter    Counter object storing count success and failure
   * @param timeTaken  time taken (wall time)
   * @param idx        idx of current run
   * @param runRecords List of String to store record of each run
   */
  public static void printOutput(Counter counter, Long timeTaken, Integer idx, int max,
      List<String[]> runRecords, List<String> stats){

    System.out.println("Test " + (idx + 1));
    System.out.println("Number of threads used: " + NUMTHREADS_LIST[idx]);
    System.out.println("Number of connections used: " + max);
    System.out.println("Number of successful requests: " + counter.getCountSuccess());
    System.out.println("Number of unsuccessful requests: " + counter.getCountFailure());
    System.out.println("Total run time (wall time) taken = " + timeTaken + "ms");
    double throughput = REQUEST_LIST[idx]/(timeTaken/1000.0);
    System.out.println("Total Throughput in requests per second = " + throughput);
    List<String> record = new ArrayList<>();
    record.add(String.valueOf(NUMTHREADS_LIST[idx]));
    record.add(String.valueOf(max));
    record.add(String.valueOf(REQUEST_LIST[idx]));
    record.add(String.valueOf(timeTaken));
    record.add(String.valueOf(throughput));
    record.addAll(stats);
    String[] records = new String[record.size()];
    records = record.toArray(records);
    runRecords.add(records);
  }
}

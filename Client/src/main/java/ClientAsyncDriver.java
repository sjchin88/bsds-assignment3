import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;

public class ClientAsyncDriver extends ClientDriver{
  /**
   * Main method to initiate the threads
   * @param args from CLI, not required at here
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException, IOException {
    System.out.println("Async Run with recording to Server");
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
      for (int i = 0; i < numthreads; i++){
        Thread thread = new Thread(new HttpAsyncClient(URL, requestPerThread, countDownLatch, counter, memoryBuffer));
        thread.start();
      }
      countDownLatch.await();
      Long end = System.currentTimeMillis();
      Long timeTaken = end - start;
      printOutput(counter, timeTaken, idx, runRecords);
      String outputname = "async-numthread-"+numthreads+"request-"+totalRequests;
      RecordProcessor recordProcessor = new RecordProcessor(memoryBuffer, outputname);
      recordProcessor.processData();
    }
    RecordProcessor recordProcessor = new RecordProcessor(null, null);
    recordProcessor.storeResult(runRecords, "AsyncRunsWithRecordingSummary.csv");
    // ClientDriver.main(new String[] {});
  }
}

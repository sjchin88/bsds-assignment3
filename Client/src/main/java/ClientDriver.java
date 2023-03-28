import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Represent a ClientDriver class for generating HttpClient threads and number of requests
 * without recording the results
 */
public class ClientDriver {

  //protected final static int[] NUMTHREADS_LIST = new int[] { 1, 200, 300, 400, 500, 600};
  //protected final static int[] REQUEST_LIST = new int[] { 10, 500_000,  500_000, 500_000, 500_000, 500_000};
  protected final static int[] NUMTHREADS_LIST = new int[] { 1 , 200};
  protected final static int[] REQUEST_LIST = new int[] { 100 , 500_000};
  /**
   * Address where the tomcat webapp is being hosted, change it to remote IP address when hosting on AWS
   */
  protected final static String URL = "http://localhost:8091/Server_war_exploded/swipe";
  //protected final static String URL = "http://34.215.190.242:8080/Twinder/swipe";

  /**
   * Main method to initiate the threads
   * @param args from CLI, not required at here
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException, IOException {
    List<String[]> runRecords = new ArrayList<>();
    final String[] headers = new String[]{"Number of Threads", "Number of requests", "Time taken", "Throughput per second"};
    runRecords.add(headers);
    for (int idx = 0; idx < NUMTHREADS_LIST.length; idx++) {
      int numthreads = NUMTHREADS_LIST[idx];
      int totalRequests = REQUEST_LIST[idx];
      CountDownLatch countDownLatch = new CountDownLatch(numthreads);
      Counter counter = new Counter();
      int requestPerThread = totalRequests / numthreads;
      Long start = System.currentTimeMillis();
      for (int i = 0; i < numthreads; i++){
        Thread thread = new Thread(new HttpClient(URL, requestPerThread, countDownLatch, counter));
        thread.start();
      }
      countDownLatch.await();
      Long end = System.currentTimeMillis();
      Long timeTaken = end - start;
      printOutput(counter, timeTaken, idx, runRecords);
    }
    RecordProcessor recordProcessor = new RecordProcessor(null, null);
    recordProcessor.storeResult(runRecords, "RunsSummary.csv");
    // ClientRecordingDriver.main(new String[]{});
  }

  /**
   * Method to print the output after finished sending the request
   *
   * @param counter    Counter object storing count success and failure
   * @param timeTaken  time taken (wall time)
   * @param idx        idx of current run
   * @param runRecords List of String to store record of each run
   */
  public static void printOutput(Counter counter, Long timeTaken, Integer idx,
      List<String[]> runRecords){

    System.out.println("Test " + (idx + 1));
    System.out.println("Number of threads used: " + NUMTHREADS_LIST[idx]);
    System.out.println("Number of successful requests: " + counter.getCountSuccess());
    System.out.println("Number of unsuccessful requests: " + counter.getCountFailure());
    System.out.println("Total run time (wall time) taken = " + timeTaken + "ms");
    double throughput = REQUEST_LIST[idx]/(timeTaken/1000.0);
    System.out.println("Total Throughput in requests per second = " + throughput);
    String[] record = new String[] {String.valueOf(NUMTHREADS_LIST[idx]),
        String.valueOf(REQUEST_LIST[idx]), String.valueOf(timeTaken), String.valueOf(throughput)};
    runRecords.add(record);
  }
}

package utility;

import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Represent a record processor class to process given record of http post request result
 * Calculate the mean response time, median response time, throughput,
 * P99 response time, min and max, and convert the record to type to be written into the CSV
 */
public class RecordProcessor {
  private DescriptiveStatistics stats;
  private HashMap<Long, Integer> recordMap;

  /**
   * Create the record processor with given memoryBuffer
   */
  public RecordProcessor() {
    this.stats = new DescriptiveStatistics();
    this.recordMap = new HashMap<>();
  }

  /**
   * Print output for each run
   * @param counter   Counter object used for each run
   * @param timeTaken time taken for each run
   * @param idx       index of the run
   * @param numThread total number of thread used
   * @param totalRequest total number of request
   * @param runRecords
   */
  public void processData(Counter counter, Long timeTaken, int idx, Integer numThread, Integer totalRequest,
      BlockingQueue<long[]> postRecBuffer, BlockingQueue<long[]> getRecBuffer, String name,
      List<String[]> runRecords) throws IOException, InterruptedException {
    //Print main summary
    System.out.println("Test " + (idx + 1));
    System.out.println("Number of threads used: " + numThread);
    System.out.println("Number of successful requests: " + counter.getCountSuccess());
    System.out.println("Number of unsuccessful requests: " + counter.getCountFailure());
    System.out.println("Total run time (wall time) taken = " + timeTaken + "ms");
    double throughput = totalRequest/(timeTaken/1000.0);
    System.out.println("Total Throughput in requests per second = " + throughput);

    //Process post request
    this.computeStat(postRecBuffer);
    this.printStatistic();
    this.storeFreqSummary(name+"post");
    double postMean = stats.getMean();
    double postMedian = stats.getPercentile(50.0);
    double post99 = stats.getPercentile(99.0);
    double postMin = stats.getMin();
    double postMax = stats.getMax();

    //Process get request
    System.out.println("Get Request results:");
    this.computeStat(getRecBuffer);
    this.printStatistic();
    this.storeFreqSummary(name+"get");
    double getMean = stats.getMean();
    double getMedian = stats.getPercentile(50.0);
    double get99 = stats.getPercentile(99.0);
    double getMin = stats.getMin();
    double getMax = stats.getMax();

    String[] record = new String[] {
        String.valueOf(numThread),
        String.valueOf(totalRequest),
        String.valueOf(timeTaken),
        String.valueOf(throughput),
        String.valueOf(postMean),
        String.valueOf(postMedian),
        String.valueOf(post99),
        String.valueOf(postMin),
        String.valueOf(postMax),
        String.valueOf(getMean),
        String.valueOf(getMedian),
        String.valueOf(get99),
        String.valueOf(getMin),
        String.valueOf(getMax),
    };
    runRecords.add(record);
  }
  /**
   * Process the data, extract the record from the memory buffer and write it into the csv file,
   * while also adding the latency(response time) to the stats, and counting the completion of request per second in
   * the hashmap
   * @param memoryBuffer buffer for the record
   * @throws InterruptedException
   * @throws IOException
   */
  public void computeStat(BlockingQueue<long[]> memoryBuffer) throws InterruptedException, IOException {
    this.recordMap.clear();
    this.stats.clear();

    while(!memoryBuffer.isEmpty()){
      long[] record = memoryBuffer.take();
      int responseTime = (int) (record[1] - record[0]);
      stats.addValue(responseTime);
      this.recordMap.put(record[1]/1000, recordMap.getOrDefault(record[1]/1000, 0) + 1);
    }
  }

  /**
   * Method to print the statistic of response time
   */
  public void printStatistic() {
    System.out.println("mean response time: " + stats.getMean());
    System.out.println("median response time: " + stats.getPercentile(50.0));
    System.out.println("99th percentile: " + stats.getPercentile(99.0));
    System.out.println("min response time: " + stats.getMin());
    System.out.println("max response time: " + stats.getMax());
  }

  /**
   * Method to convert the hashmap into sorted treeMap, and store the count of request completed per second to the
   * csv file for further plotting
   * @param outputName name of the output file
   * @throws IOException
   */
  public void storeFreqSummary(String outputName) throws IOException {
    TreeMap<Long, Integer> treeMap = new TreeMap<>(this.recordMap);
    final CSVWriter csvWriter = this.createWriter(outputName + "summary.csv");
    final String[] headers = new String[]{"End", "count"};
    csvWriter.writeNext(headers);
    for(Long key: treeMap.keySet()){
      Timestamp timestamp = new Timestamp(key * 1000);
      String[] nextLine = new String[] {timestamp.toString(), String.valueOf(treeMap.get(key))};
      csvWriter.writeNext(nextLine);
    }
    csvWriter.close();
  }

  /**
   * Method to store the records from the List<String> into given fileName
   * @param records record of List<String>
   * @param fileName fileName of target record file
   */
  public void storeResult(List<String[]> records, String fileName) throws IOException {
    final CSVWriter csvWriter = this.createWriter(fileName);
    csvWriter.writeAll(records);
    csvWriter.close();
  }

  /**
   * Create a new CSV Writer from given String file
   * @param fullName name of the file in String
   * @return CSV Writer file
   */
  public CSVWriter createWriter(final String fullName) {
    try{
      final File file = new File(fullName);
      // create FileWriter object with file as parameter
      final FileWriter outputfile = new FileWriter(file);

      // create CSVWriter object filewriter object as parameter
      return new CSVWriter(outputfile);
    }catch (IOException e){
      throw new RuntimeException(e);
    }
  }
}

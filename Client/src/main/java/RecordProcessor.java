import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
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
  private BlockingQueue<long[]> memoryBuffer;
  private String outputName;
  private DescriptiveStatistics stats;
  private HashMap<Long, Integer> recordMap;

  /**
   * Create the record processor with given memoryBuffer
   * @param memoryBuffer memoryBuffer of BlockingQueue storing long[] object
   * @param name Name of the output file
   */
  public RecordProcessor(BlockingQueue<long[]> memoryBuffer, String name) {
    this.memoryBuffer = memoryBuffer;
    this.outputName = name;
    this.stats = new DescriptiveStatistics();
    this.recordMap = new HashMap<>();
  }

  /**
   * Process the data, extract the record from the memory buffer and write it into the csv file,
   * while also adding the latency(response time) to the stats, and counting the completion of request per second in
   * the hashmap
   * @throws InterruptedException
   * @throws IOException
   */
  public void processData() throws InterruptedException, IOException {
    // final CSVWriter csvWriter = this.createWriter(outputName + ".csv");
    // final String[] headers = new String[]{"Start", "Method", "Latency", "End", "Response Code"};
    // csvWriter.writeNext(headers);
    while(!this.memoryBuffer.isEmpty()){
      // String[] processedRecord = new String[5];
      long[] record = this.memoryBuffer.take();
      //Timestamp startTime = new Timestamp(record[0]);
      //processedRecord[0] = startTime.toString();
      //processedRecord[1] = "Post";
      int responseTime = (int) (record[1] - record[0]);
      stats.addValue(responseTime);
      // processedRecord[2] = String.valueOf(responseTime);
      this.recordMap.put(record[1]/1000, recordMap.getOrDefault(record[1]/1000, 0) + 1);
      // processedRecord[3] = new Timestamp(record[1]).toString();
      // processedRecord[4] = String.valueOf(record[2]);
      // csvWriter.writeNext(processedRecord);
    }
    // csvWriter.close();
    this.printStatistic();
    this.storeSummary();
  }

  public List<String> processAndStoreData() throws InterruptedException, IOException {
    while(!this.memoryBuffer.isEmpty()){
      long[] record = this.memoryBuffer.take();
      int responseTime = (int) (record[1] - record[0]);
      stats.addValue(responseTime);
      this.recordMap.put(record[1]/1000, recordMap.getOrDefault(record[1]/1000, 0) + 1);
    }
    List<String> results = new ArrayList<>();
    results.add(String.valueOf(stats.getMean()));
    results.add(String.valueOf(stats.getPercentile(50.0)));
    results.add(String.valueOf(stats.getPercentile(99.0)));
    results.add(String.valueOf(stats.getMin()));
    results.add(String.valueOf(stats.getMax()));
    this.storeSummary();
    return results;
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
   * @throws IOException
   */
  public void storeSummary() throws IOException {
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

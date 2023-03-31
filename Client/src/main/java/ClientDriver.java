import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import utility.Counter;
import utility.RecordProcessor;

/**
 * Represent a ClientDriver class for generating HttpClient threads and number of requests
 * without recording the results
 */
public class ClientDriver {

  //protected final static int[] NUMTHREADS_LIST = new int[] { 1, 200, 300, 400, 500, 600};
  //protected final static int[] REQUEST_LIST = new int[] { 10, 500_000,  500_000, 500_000, 500_000, 500_000};
  protected final static int[] NUMTHREADS_LIST = new int[] { 1 ,100, 200, 300, 400};
  protected final static int[] REQUEST_LIST = new int[] { 1000 , 500_000, 500_000, 500_000, 500_000};
  /**
   * Address where the tomcat webapp is being hosted, change it to remote IP address when hosting on AWS
   */
  //protected final static String URL = "http://localhost:8091/Server_war_exploded";
  protected final static String URL = "http://34.217.108.173:8080/TwinderA3V1";


}

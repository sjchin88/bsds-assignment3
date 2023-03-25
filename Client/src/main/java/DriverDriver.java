import java.io.IOException;

/**
 * Driver to test all the different settings in one go
 */
public class DriverDriver {
  public static void main(String[] args) throws IOException, InterruptedException {
    //ClientRecordingDriver.main(new String[]{});
    //ClientAsyncDriver.main(new String[]{});
    //ClientApacheDriver.main(new String[]{});
    //ClientPoolDriver.main(new String[]{});
    //ClientPoolAsyncDriver.main(new String[]{});
    ClientPoolAsyncSingleDriver.main(new String[]{});
  }
}

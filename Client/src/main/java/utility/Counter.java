package utility;

/**
 * Represent a counter storing the countSuccess and countFailure,
 * and control its incrementation in synchronized manner
 */
public class Counter {
  private int countSuccess;
  private int countFailure;
  private int sendCount;

  /**
   * Initialize a new utility.Counter object
   */
  public Counter() {
    this.countSuccess = 0;
    this.countFailure = 0;
  }

  public Counter( int sendCount) {
    this.countSuccess = countSuccess;
    this.countFailure = countFailure;
    this.sendCount = sendCount;
  }

  /**
   * Synchronized method to add new count of success and failure from each thread to the
   * countSuccess and countFailure stored
   * @param success new count of success from the thread
   * @param failure new count of failure from the thread
   */
  public synchronized void addCount(int success, int failure) {
    this.countSuccess += success;
    this.countFailure += failure;
  }

  public synchronized void addSend(){
    this.sendCount--;
  }

  public synchronized void addSuccess(){
    this.countSuccess++;
  }

  public synchronized void addFail(){
    this.countFailure++;
  }

  public synchronized int getSendCount() {
    return sendCount;
  }

  /**
   * Getter for the countSuccess variable
   * @return countSuccess in int
   */
  public synchronized int getCountSuccess() {
    return countSuccess;
  }

  /**
   * Getter for the countFailure variable
   * @return countFailure in int
   */
  public synchronized int getCountFailure() {
    return countFailure;
  }
}

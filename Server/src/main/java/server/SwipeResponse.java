package server;

/**
 * Class for SwipeResponse
 * Allow conversion directly to json object to include in Http Response
 */
public class SwipeResponse {
  private String message;

  /**
   * Create new SwipeResponse using given message
   * @param message String message
   */
  public SwipeResponse(String message) {
    this.message = message;
  }

  /**
   * Getter for the response message
   * @return messge in string
   */
  public String getMessage() {
    return message;
  }

  /**
   * Setter for the response message
   * @param message
   */
  public void setMessage(String message) {
    this.message = message;
  }
}

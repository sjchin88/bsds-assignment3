package server;

import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import shared.utilities.SwipeResponse;

public interface Server {
  /**
   * Valid URL
   */
  public static final String URL_LEFT = "/left";
  public static final String URL_RIGHT = "/right";
  public static final String URL_MATCH = "^/matches/\\d+";
  public static final String URL_STAT = "^/stats/\\d+";

  /**
   * Response Message for valid swipe
   */
  public static final String MSG_SWIPE_OK = "Swipe Ok";

  /**
   * Error Message for invalid url
   */
  public static final String MSG_ERROR_URL = "invalid url";

  /**
   * Error Message for invalid inputs
   */
  public static final String MSG_ERROR_INPUT = "Invalid Input";

  /**
   * Error Message for invalid swiper
   */
  public static final String MSG_ERROR_USER = "User not found";

  /**
   * Lower bound limit for swiper and swipee id
   */
  public static final int SWIPE_LOWER= 0;

  /**
   * Upper bound limit for swiper and swipee id
   */
  public static final int SWIPE_UPPER = 50_000;

  /**
   * Exchange name to be used on RabbitMQ server
   */
  public static final String RABBIT_EXCH_NAME = "swipes";

  public Gson gson = new Gson();

  /**
   * method to check if the swipeId is valid
   * @param swipeId swiperId in int
   * @return boolean indicate if the swiperId is valid
   */
  default boolean validateSwipeID(int swipeId){
    if(swipeId < Server.SWIPE_LOWER || swipeId > Server.SWIPE_UPPER) {
      return false;
    }
    return true;
  }


  /**
   * method to check if the comment is valid
   * @param comment comment in String
   * @return boolean indicate if the swiperId is valid
   */
  default boolean validateComment(String comment){
    if(comment == null){
      return false;
    }
    return true;
  }

  /**
   * Helper method to send HTTP response using given message and responseCode
   * @param message response message in string
   * @param responseCode HTTP response code in int
   * @param response  HttpServlet Response object
   * @throws IOException IO exception when writing into the getOutputStream
   */
  default void replyMsg(String message, int responseCode, HttpServletResponse response)
      throws IOException {
    SwipeResponse swipeResponse = new SwipeResponse(message);
    response.setStatus(responseCode);
    response.getOutputStream().print(this.gson.toJson(swipeResponse));
    response.getOutputStream().flush();
  }
}

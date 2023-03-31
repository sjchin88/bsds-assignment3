package server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "GetServlet", value = "/*")
public class GetServer extends HttpServlet {
  private static final String URL_MATCH = "^/matches/\\d+";
  private static final String URL_STAT = "^/stats/\\d+";

  /**
   * Error Message for invalid url
   */
  private static final String ERROR_URL = "invalid url";

  /**
   * Error Message for invalid inputs
   */
  private static final String ERROR_INPUT = "Invalid Input";

  /**
   * Error Message for invalid swiper
   */
  private static final String ERROR_USER = "User not found";

  /**
   * Lower bound limit for swiper and swipee id
   */
  private static final int LOWER_BOUND = 0;

  /**
   * Upper bound limit for swiper id
   */
  private static final int SWIPER_UPPER = 5000;

  private Gson gson;
  private static final String REDIS_HOST = "redis://127.0.0.1:6379";
  //private static final String REDIS_HOST = "redis://foobared2@54.218.18.155:6379";
  private static final String PREFIX_LIKES_CNT = "Likes:";
  private static final String PREFIX_DISLIKES_CNT = "Dislikes:";
  private static final String PREFIX_SWIPE_REC = "Swiper:";
  private StatefulRedisConnection<String, String> redisConnection;
  private RedisAsyncCommands<String, String> redisCommand;
  private RedisClient redisClient;

  /**
   * Set up the server class , creating the RabbitMQ channel pool
   * @throws ServletException
   */
  @Override
  public void init() throws ServletException {
    super.init();
    // Initialized other instance variable
    this.gson = new Gson();

    //Initialized the Redis connection
    this.redisClient = RedisClient.create(REDIS_HOST);
    this.redisConnection = this.redisClient.connect();
    //Create asynchronous API
    this.redisCommand = this.redisConnection.async();
  }

  /**
   * doGet method, currently return one line statement only as the assignment request to implement
   * post method only
   * @param request valid http request
   * @param response http response
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("application/json");
    String urlPath = request.getPathInfo();

    // check for valid url
    if (isGetUrlValid(urlPath, response)) {
      if(urlPath.matches(URL_MATCH)){
        this.getPotentialMatches(response, urlPath.substring(9));
      } else if (urlPath.matches(URL_STAT)){
        this.getStat(response, urlPath.substring(9));
      }
    }
  }


  /**
   * Retrieve
   * @param response
   * @param swiperId
   */
  private void getPotentialMatches(HttpServletResponse response, String swiperId) {
    try{
      RedisFuture<List<String>> swipeeSet = this.redisCommand.srandmember(PREFIX_SWIPE_REC+swiperId, 100);
      JsonObject jsonObject = new JsonObject();
      JsonArray jsonArray = this.gson.toJsonTree(swipeeSet.get()).getAsJsonArray();
      jsonObject.add("matchList", jsonArray);
      response.setStatus(HttpServletResponse.SC_OK);
      response.getOutputStream().print(this.gson.toJson(jsonObject));
      response.getOutputStream().flush();
    } catch (IOException | ExecutionException | InterruptedException e) {
      Logger.getLogger("Get From Redis").log(Level.WARNING, "Error processing request", e);
    }
  }

  private void getStat(HttpServletResponse response, String swiperId) throws IOException {
    try{
      //System.out.println(swiperId);
      RedisFuture<String> likeCount = this.redisCommand.get(PREFIX_LIKES_CNT+swiperId);
      RedisFuture<String> dislikeCount = this.redisCommand.get(PREFIX_DISLIKES_CNT+swiperId);
      //System.out.println(dislikeCount.get());
      JsonObject jsonObject = new JsonObject();
      int likes = likeCount.get() != null ? Integer.valueOf(likeCount.get()) : 0;
      int dislikes = dislikeCount.get() != null ? Integer.valueOf(dislikeCount.get()) : 0;
      jsonObject.addProperty("numLikes", likes );
      jsonObject.addProperty("numDislikes",dislikes);
      response.setStatus(HttpServletResponse.SC_OK);
      response.getOutputStream().print(this.gson.toJson(jsonObject));
      response.getOutputStream().flush();
    } catch (IOException | ExecutionException | InterruptedException e) {
      Logger.getLogger("Get From Redis").log(Level.WARNING, "Error processing request", e);
    }
  }

  /**
   * Check if the urlPath is valid for Get call
   *
   * @param urlPath
   * @param response
   * @return boolean
   */
  private boolean isGetUrlValid(String urlPath, HttpServletResponse response) throws IOException {
    if (urlPath == null || urlPath.isEmpty()) {
      this.replyMsg(ERROR_INPUT, HttpServletResponse.SC_BAD_REQUEST, response);
      return false;
    }
    int swiperId = -1;
    try{
      if(urlPath.matches(URL_MATCH)){
        swiperId = Integer.valueOf(urlPath.substring(9));
      } else if(urlPath.matches(URL_STAT)){
        swiperId = Integer.valueOf(urlPath.substring(7));
      }
    } catch (Exception e){
      this.replyMsg(ERROR_INPUT, HttpServletResponse.SC_BAD_REQUEST, response);
      return false;
    }
    if(!validateSwiper(swiperId)) {
      this.replyMsg(ERROR_USER, HttpServletResponse.SC_NOT_FOUND, response);
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
  protected void replyMsg(String message, int responseCode, HttpServletResponse response)
      throws IOException {
    SwipeResponse swipeResponse = new SwipeResponse(message);
    response.setStatus(responseCode);
    response.getOutputStream().print(this.gson.toJson(swipeResponse));
    response.getOutputStream().flush();
  }

  /**
   * Static method to check if the swiperId is valid
   * @param swiperId swiperId in int
   * @return boolean indicate if the swiperId is valid
   */
  public static boolean validateSwiper(int swiperId){
    if(swiperId < LOWER_BOUND || swiperId > SWIPER_UPPER) {
      return false;
    } else {
      return true;
    }
  }
}

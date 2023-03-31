package server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Server class to handle traffic to /swipe path
 */
@WebServlet(name = "SwipeServlet", value = "/swipe")
public class SwipeServer extends HttpServlet {

  /**
   * Valid URL
   */
  private static final String URL_LEFT = "/left";
  /**
   * Valid URL
   */
  private static final String URL_RIGHT = "/right";
  private static final String URL_MATCH = "^/matches/\\d+";
  private static final String URL_STAT = "^/stats/\\d+";

  /**
   * Response Message for valid swipe
   */
  private static final String SWIPE_OK = "Swipe Ok";

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
  /**
   * Upper bound limit for swipee id
   */
  private static final int SWIPEE_UPPER = 1_000_000;

  /**
   * number of channels to connect to RabbitMQ server
   */
  private static final int NUM_CHANNELS = 200;

  /**
   * Exchange name to be used on RabbitMQ server
   */
  private static final String EXCHANGE_NAME = "swipes";

  /**
   * Address of the RabbitMQ server, change it to IP address when hosting on EC-2
   */
  private static String RABBIT_HOST = "localhost";
  //private static String RABBIT_HOST = "35.165.32.0";
  //private static String RABBIT_USER = "csj";
  //private static String RABBIT_PASS = "Gu33ssm3";
  private ConnectionFactory rabbitFactory;
  private RabbitMQChannelPool channelPool;
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
    // Create new connection to the rabbit MQ
    this.rabbitFactory = new ConnectionFactory();
    this.rabbitFactory.setHost(RABBIT_HOST);
    //this.rabbitFactory.setUsername(RABBIT_USER);
    //this.rabbitFactory.setPassword(RABBIT_PASS);
    Connection rabbitMQConn;
    try {
      rabbitMQConn = this.rabbitFactory.newConnection();
      System.out.println("INFO: RabbitMQ connection established");
    } catch (IOException | TimeoutException e) {
      throw new RuntimeException(e);
    }

    // Create the required RabbitMQ Channels pool
    RabbitMQChannelFactory factory = new RabbitMQChannelFactory(rabbitMQConn);
    this.channelPool = new RabbitMQChannelPool(NUM_CHANNELS, factory);

    // Initialized other instance variable
    this.gson = new Gson();
  }

  /**
   * Check if the urlPath is valid
   * @param urlPath path of the url in string
   * @return boolean indicator
   */
  private boolean isPostUrlValid(String urlPath) {
    // validate the request url path according to the API spec
    if (urlPath.equals(URL_LEFT) || urlPath.equals(URL_RIGHT)){
      return true;
    }
    return false;
  }


  /**
   * doPost method, convert the request body from json object to SwipeDetail class object
   * check if input is valid, and call the method processRequest for further processing
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("application/json");
    String urlPath = request.getPathInfo();
    // check for valid url
    if (urlPath == null || urlPath.isEmpty() || !isPostUrlValid(urlPath)) {
      this.replyMsg(ERROR_URL, HttpServletResponse.SC_NOT_FOUND, response);
      return;
    }
    this.processRequest(request, response);
  }

  /**
   * Helper method to process the HTTP request
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
    try {
      //Get the swipe details from json and stored it in SwipeDetail object
      StringBuilder sb = new StringBuilder();
      String s;
      while((s = request.getReader().readLine()) != null) {
        sb.append(s);
      }
      SwipeDetail swipeDetail = this.gson.fromJson(sb.toString(), SwipeDetail.class);

      // validate Swiper ID
      int swiperId  = Integer.parseInt(swipeDetail.getSwiper());
      if(!validateSwiper(swiperId)){
        this.replyMsg(ERROR_USER, HttpServletResponse.SC_NOT_FOUND, response);
        return;
      }

      // validate Swipee ID
      int swipeeId = Integer.parseInt(swipeDetail.getSwipee());
      if(!validateSwipee(swipeeId)){
        this.replyMsg(ERROR_INPUT, HttpServletResponse.SC_BAD_REQUEST, response);
        return;
      }

      // validate comment
      if(!validateComment(swipeDetail.getComment())){
        this.replyMsg(ERROR_INPUT, HttpServletResponse.SC_BAD_REQUEST, response);
        return;
      }

      // prepare the message to RabbitMQ and required info
      String messages = swipeDetail.getSwiper() + ":"+swipeDetail.getSwipee();
      String swipeDirection = request.getPathInfo().substring(1);

      // borrow channel and publish the message
      Channel channel = this.channelPool.borrowObject();
      // publish the message with MessageProperties set to persistent type
      channel.basicPublish(EXCHANGE_NAME, swipeDirection, true, MessageProperties.PERSISTENT_TEXT_PLAIN, messages.getBytes());
      this.channelPool.returnObject(channel);

      // Send the response back to client
      this.replyMsg(SWIPE_OK,HttpServletResponse.SC_CREATED,response);
    } catch (IOException e) {
      Logger.getLogger(RabbitMQChannelPool.class.getName()).log(Level.WARNING, "Error processing request", e);
    }
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

  /**
   * Static method to check if the swipeeId is valid
   * @param swipeeId swipeeId in int
   * @return boolean indicate if the swiperId is valid
   */
  public static boolean validateSwipee(int swipeeId){
    if(swipeeId < LOWER_BOUND || swipeeId > SWIPEE_UPPER) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Static method to check if the comment is valid
   * @param comment comment in String
   * @return boolean indicate if the swiperId is valid
   */
  public static boolean validateComment(String comment){
    if(comment == null){
      return false;
    } else {
      return true;
    }
  }
}

package server;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import services.rabbitmq.RabbitMQChannelPool;
import shared.utilities.SwipeDetail;


/**
 * Server class to handle traffic to /swipe path
 */
@WebServlet(name = "SwipeServlet", value = "/swipe")
public class SwipeServer extends HttpServlet implements Server {
  private RabbitMQChannelPool channelPool;

  /**
   * Set up the server class , creating the RabbitMQ channel pool
   * @throws ServletException
   */
  @Override
  public void init() throws ServletException {
    super.init();
    this.channelPool = new RabbitMQChannelPool();
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
      this.replyMsg(MSG_ERROR_URL, HttpServletResponse.SC_NOT_FOUND, response);
      return;
    }
    this.processRequest(request, response);
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

      if(validateSwipe(swipeDetail, response)){
        // prepare the message to RabbitMQ and required info
        String messages = swipeDetail.getSwiper() + ":"+swipeDetail.getSwipee();
        String swipeDirection = request.getPathInfo().substring(1);

        // borrow channel and publish the message
        Channel channel = this.channelPool.borrowObject();

        // publish the message with MessageProperties set to persistent type
        channel.basicPublish(RABBIT_EXCH_NAME, swipeDirection, true, MessageProperties.PERSISTENT_TEXT_PLAIN, messages.getBytes());
        this.channelPool.returnObject(channel);

        // Send the response back to client
        this.replyMsg(MSG_SWIPE_OK,HttpServletResponse.SC_CREATED,response);
      }
    } catch (IOException e) {
      Logger.getLogger(RabbitMQChannelPool.class.getName()).log(Level.WARNING, "Error processing request", e);
    }
  }

  /**
   * method to validate the swipe details
   * @param swipeDetail swipe details object contain the swipe information
   * @param response HttpServlet Response object
   * @return boolean indicator
   */
  public boolean validateSwipe(SwipeDetail swipeDetail, HttpServletResponse response)
      throws IOException {
    // validate Swiper ID
    int swiperId  = Integer.parseInt(swipeDetail.getSwiper());
    if(!validateSwipeID(swiperId)){
      this.replyMsg(MSG_ERROR_USER, HttpServletResponse.SC_NOT_FOUND, response);
      return false;
    }

    // validate Swipee ID
    int swipeeId = Integer.parseInt(swipeDetail.getSwipee());
    if(!validateSwipeID(swipeeId)){
      this.replyMsg(MSG_ERROR_INPUT, HttpServletResponse.SC_BAD_REQUEST, response);
      return false;
    }

    // validate comment
    if(!validateComment(swipeDetail.getComment())){
      this.replyMsg(MSG_ERROR_INPUT, HttpServletResponse.SC_BAD_REQUEST, response);
      return false;
    }

    return true;
  }


}

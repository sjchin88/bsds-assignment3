package server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import services.redis.RedisService;

@WebServlet(name = "GetServlet", value = "/*")
public class GetServer extends HttpServlet implements Server {

  private RedisService redisService;

  /**
   * Set up the server class , creating the RabbitMQ channel pool
   * @throws ServletException
   */
  @Override
  public void init() throws ServletException {
    super.init();
    // Initialized other instance variable
    this.redisService = new RedisService();
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
      List<String> matches = this.redisService.getMatches(swiperId);
      JsonObject jsonObject = new JsonObject();
      JsonArray jsonArray = this.gson.toJsonTree(matches).getAsJsonArray();
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
      JsonObject jsonObject = new JsonObject();
      int likes = this.redisService.getLikesCount(swiperId);
      int dislikes = this.redisService.getDislikesCount(swiperId);
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
      this.replyMsg(MSG_ERROR_URL, HttpServletResponse.SC_BAD_REQUEST, response);
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
      this.replyMsg(MSG_ERROR_INPUT, HttpServletResponse.SC_BAD_REQUEST, response);
      return false;
    }
    if(!validateSwipeID(swiperId)) {
      this.replyMsg(MSG_ERROR_USER, HttpServletResponse.SC_NOT_FOUND, response);
    }
    return true;
  }



}

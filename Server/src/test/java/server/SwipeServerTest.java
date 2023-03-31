package server;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


class SwipeServerTest {

  HttpServletRequest request;

  HttpServletResponse response;
  SwipeServer swipeServer;
  private StringWriter writer;

  @BeforeEach
  void setUp() {
    this.request = mock(HttpServletRequest.class);
    this.response = mock(HttpServletResponse.class);
    this.swipeServer = new SwipeServer();
    this.writer = new StringWriter();
  }


  @Test
  void doPost_invalidUrl() throws IOException, ServletException {
    MockHttpServletRequest requestMock = new MockHttpServletRequest("Post","");
    requestMock.setPathInfo("");
    MockHttpServletResponse responseMock = new MockHttpServletResponse();
    JsonObject object = new JsonObject();
    object.addProperty("swiper", 11);
    object.addProperty("swipee", 11);
    object.addProperty("comment", "testing");
    byte[] content = object.toString().getBytes(StandardCharsets.UTF_8);
    requestMock.setContent(content);
    swipeServer.doPost(requestMock, responseMock);
    assertEquals(404, responseMock.getStatus());
    assertEquals("{\"message\":\"invalid url\"}", responseMock.getContentAsString());
  }

  @Test
  void doPost_invalidUrl2() throws IOException, ServletException {
    MockHttpServletRequest requestMock = new MockHttpServletRequest("Post","");
    MockHttpServletResponse responseMock = new MockHttpServletResponse();
    JsonObject object = new JsonObject();
    object.addProperty("swiper", 11);
    object.addProperty("swipee", 11);
    object.addProperty("comment", "testing");
    byte[] content = object.toString().getBytes(StandardCharsets.UTF_8);
    requestMock.setContent(content);
    swipeServer.doPost(requestMock, responseMock);
    assertEquals(404, responseMock.getStatus());
    assertEquals("{\"message\":\"invalid url\"}", responseMock.getContentAsString());
  }

  @Test
  void doPost_invalidUrl3() throws IOException, ServletException {
    MockHttpServletRequest requestMock = new MockHttpServletRequest("Post","");
    requestMock.setPathInfo("/test");
    MockHttpServletResponse responseMock = new MockHttpServletResponse();
    JsonObject object = new JsonObject();
    object.addProperty("swiper", 11);
    object.addProperty("swipee", 11);
    object.addProperty("comment", "testing");
    byte[] content = object.toString().getBytes(StandardCharsets.UTF_8);
    requestMock.setContent(content);
    swipeServer.doPost(requestMock, responseMock);
    assertEquals(404, responseMock.getStatus());
    assertEquals("{\"message\":\"invalid url\"}", responseMock.getContentAsString());
  }

  @Test
  void doPost_validUrl() throws IOException, ServletException {
    MockHttpServletRequest requestMock = new MockHttpServletRequest("Post","");
    requestMock.setPathInfo("/left");
    MockHttpServletResponse responseMock = new MockHttpServletResponse();
    JsonObject object = new JsonObject();
    object.addProperty("swiper", 11);
    object.addProperty("swipee", 11);
    object.addProperty("comment", "testing");
    byte[] content = object.toString().getBytes(StandardCharsets.UTF_8);
    requestMock.setContent(content);
    swipeServer.doPost(requestMock, responseMock);
    assertEquals(201, responseMock.getStatus());
    assertEquals( "{\"message\":\"Swipe Ok\"}", responseMock.getContentAsString());
  }

  @Test
  void doPost_validUrl2() throws IOException, ServletException {
    MockHttpServletRequest requestMock = new MockHttpServletRequest("Post","");
    requestMock.setPathInfo("/right");
    MockHttpServletResponse responseMock = new MockHttpServletResponse();
    JsonObject object = new JsonObject();
    object.addProperty("swiper", 11);
    object.addProperty("swipee", 11);
    object.addProperty("comment", "testing");
    byte[] content = object.toString().getBytes(StandardCharsets.UTF_8);
    requestMock.setContent(content);
    swipeServer.doPost(requestMock, responseMock);
    assertEquals(201, responseMock.getStatus());
    assertEquals( "{\"message\":\"Swipe Ok\"}", responseMock.getContentAsString());
  }

  @Test
  void processRequest() throws UnsupportedEncodingException {
    MockHttpServletRequest requestMock = new MockHttpServletRequest("Post","");
    MockHttpServletResponse responseMock = new MockHttpServletResponse();
    JsonObject object = new JsonObject();
    object.addProperty("swiper", 11);
    object.addProperty("swipee", 11);
    object.addProperty("comment", "testing");
    byte[] content = object.toString().getBytes(StandardCharsets.UTF_8);
    requestMock.setContent(content);
    swipeServer.processRequest(requestMock, responseMock);
    assertEquals(201, responseMock.getStatus());
    assertEquals( "{\"message\":\"Swipe Ok\"}", responseMock.getContentAsString());
  }

  @Test
  void processRequest_InvalidSwiper() throws UnsupportedEncodingException {
    MockHttpServletRequest requestMock = new MockHttpServletRequest("Post","");
    MockHttpServletResponse responseMock = new MockHttpServletResponse();
    JsonObject object = new JsonObject();
    object.addProperty("swiper", -1);
    object.addProperty("swipee", 11);
    object.addProperty("comment", "testing");
    byte[] content = object.toString().getBytes(StandardCharsets.UTF_8);
    requestMock.setContent(content);
    swipeServer.processRequest(requestMock, responseMock);
    assertEquals( 404, responseMock.getStatus()
    );
    assertEquals( "{\"message\":\"User not found\"}", responseMock.getContentAsString());
  }

  @Test
  void processRequest_InvalidSwiper2() throws UnsupportedEncodingException {
    MockHttpServletRequest requestMock = new MockHttpServletRequest("Post","");
    MockHttpServletResponse responseMock = new MockHttpServletResponse();
    JsonObject object = new JsonObject();
    object.addProperty("swiper", 5001);
    object.addProperty("swipee", 11);
    object.addProperty("comment", "testing");
    byte[] content = object.toString().getBytes(StandardCharsets.UTF_8);
    requestMock.setContent(content);
    swipeServer.processRequest(requestMock, responseMock);
    assertEquals(404, responseMock.getStatus());
    assertEquals( "{\"message\":\"User not found\"}", responseMock.getContentAsString());
  }

  @Test
  void processRequest_InvalidSwipee() throws UnsupportedEncodingException {
    MockHttpServletRequest requestMock = new MockHttpServletRequest("Post","");
    MockHttpServletResponse responseMock = new MockHttpServletResponse();
    JsonObject object = new JsonObject();
    object.addProperty("swiper", 11);
    object.addProperty("swipee", -1);
    object.addProperty("comment", "testing");
    byte[] content = object.toString().getBytes(StandardCharsets.UTF_8);
    requestMock.setContent(content);
    swipeServer.processRequest(requestMock, responseMock);
    assertEquals(400, responseMock.getStatus());
    assertEquals( "{\"message\":\"Invalid Input\"}", responseMock.getContentAsString());
  }

  @Test
  void processRequest_InvalidSwipee2() throws UnsupportedEncodingException {
    MockHttpServletRequest requestMock = new MockHttpServletRequest("Post","");
    MockHttpServletResponse responseMock = new MockHttpServletResponse();
    JsonObject object = new JsonObject();
    object.addProperty("swiper", 11);
    object.addProperty("swipee", 1_000_001);
    object.addProperty("comment", "testing");
    byte[] content = object.toString().getBytes(StandardCharsets.UTF_8);
    requestMock.setContent(content);
    swipeServer.processRequest(requestMock, responseMock);
    assertEquals(400, responseMock.getStatus());
    assertEquals("{\"message\":\"Invalid Input\"}", responseMock.getContentAsString());
  }

  @Test
  void processRequest_InvalidComment() throws UnsupportedEncodingException {
    MockHttpServletRequest requestMock = new MockHttpServletRequest("Post","");
    MockHttpServletResponse responseMock = new MockHttpServletResponse();
    JsonObject object = new JsonObject();
    object.addProperty("swiper", 11);
    object.addProperty("swipee", 999_999);
    object.addProperty("comment", (String) null);
    byte[] content = object.toString().getBytes(StandardCharsets.UTF_8);
    requestMock.setContent(content);
    swipeServer.processRequest(requestMock, responseMock);
    assertEquals( 400, responseMock.getStatus());
    assertEquals( "{\"message\":\"Invalid Input\"}", responseMock.getContentAsString());
  }
}
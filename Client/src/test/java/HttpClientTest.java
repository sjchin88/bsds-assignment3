import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HttpClientTest {

  private HttpClient httpClient;
  @BeforeEach
  void setUp() {
    this.httpClient = new HttpClient("test", 1, new CountDownLatch(1), new Counter());
  }

  @Test
  void buildJson() {
    String json = this.httpClient.buildJson();
    System.out.println(json);
  }
}
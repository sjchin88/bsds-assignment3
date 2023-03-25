import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessageTest {

  private Message testMessage;
  private int testSwiper;
  private int testSwipee;

  @BeforeEach
  void setUp() {
    this.testSwiper = 10;
    this.testSwipee = 100;
    ByteBuffer buffer = ByteBuffer.allocate(8);
    buffer.putInt(this.testSwiper);
    buffer.putInt(this.testSwipee);
    this.testMessage = new Message(buffer.array());
  }

  @Test
  void getSwiper() {
    assertEquals(this.testSwiper, this.testMessage.getSwiper());
  }

  @Test
  void getSwipee() {
    assertEquals(this.testSwipee, this.testMessage.getSwipee());
  }
}
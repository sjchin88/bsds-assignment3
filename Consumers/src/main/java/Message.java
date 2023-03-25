import java.nio.ByteBuffer;

public class Message {
  private int swiper;
  private int swipee;

  public Message(byte[] messagesByte) {
    ByteBuffer buffer = ByteBuffer.wrap(messagesByte);
    this.swiper = buffer.getInt();
    this.swipee = buffer.getInt();
  }

  public int getSwiper() {
    return swiper;
  }

  public int getSwipee() {
    return swipee;
  }
}

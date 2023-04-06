package shared.utilities;

/**
 * SwipeDetail class to store the swipe information,
 * allow conversion directly from the json object
 * which includes the id of swiper (in int), id of swipee (in int) and
 * comment (in string)
 */
public class SwipeDetail {
  private String swiper;
  private String swipee;
  private String comment;

  /**
   * Getter for swiper id
   * @return swiper id
   */
  public String getSwiper() {
    return swiper;
  }

  /**
   * Setter for swiper id
   * @param swiper new swiper id
   */
  public void setSwiper(String swiper) {
    this.swiper = swiper;
  }

  /**
   * Getter for swipee id
   * @return swipee id in int
   */
  public String getSwipee() {
    return swipee;
  }

  /**
   * Setter for swipee id
   * @param swipee swipee id in int
   */
  public void setSwipee(String swipee) {
    this.swipee = swipee;
  }

  /**
   * Getter for comment
   * @return comment in string
   */
  public String getComment() {
    return comment;
  }

  /**
   * Setter for comment
   * @param comment new comment
   */
  public void setComment(String comment) {
    this.comment = comment;
  }

  @Override
  public String toString() {
    return "swiper=" + swiper +
        "swipee=" + swipee;
  }
}

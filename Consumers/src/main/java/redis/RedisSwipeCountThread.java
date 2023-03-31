package redis;

import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class RedisSwipeCountThread extends RedisConsumerThread{
  private BlockingQueue<String> buffer;
  private static int BATCH_SIZE = 100;

  public RedisSwipeCountThread(BlockingQueue<String> buffer) {
    this.buffer = buffer;
  }

  @Override
  public void run() {
    try {
      List<RedisFuture<Long>> futureList = new ArrayList<>();
      while(true) {
        for(int i = 0; i < BATCH_SIZE; i++){
          String swiperId = this.buffer.poll(200, TimeUnit.MILLISECONDS);
          if(swiperId!=null){
            RedisFuture<Long> future = this.redCommand.incr(swiperId);
            futureList.add(future);
          } else {
            break;
          }
        }
        // If the list is not empty
        if(futureList.size()!=0){
          this.redConnection.flushCommands();
          LettuceFutures.awaitAll(200, TimeUnit.MILLISECONDS, futureList.toArray(new RedisFuture[futureList.size()]));
        }
        // Clear the list at the end
        futureList.clear();
      }
    } catch (InterruptedException e) {
      System.out.println("Reddis interrupt exception");
    }
  }
}

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
          String swiperId = this.buffer.poll(100, TimeUnit.MILLISECONDS);
          if(swiperId!=null){
            RedisFuture<Long> future = this.redCommand.incr(swiperId);
            futureList.add(future);
          } else {
            break;
          }
        }
        this.redConnection.flushCommands();
        LettuceFutures.awaitAll(100, TimeUnit.MILLISECONDS, futureList.toArray(new RedisFuture[futureList.size()]));
      }
    } catch (InterruptedException e) {
      System.out.println("Reddis interrupt exception");
    }
  }
}

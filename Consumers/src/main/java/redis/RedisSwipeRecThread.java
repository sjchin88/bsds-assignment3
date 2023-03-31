package redis;

import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class RedisSwipeRecThread extends RedisConsumerThread {
  private BlockingQueue<String[]> buffer;
  private static int BATCH_SIZE = 100;

  public RedisSwipeRecThread(BlockingQueue<String[]> buffer) {
    this.buffer = buffer;
  }

  @Override
  public void run() {
    try {
      List<RedisFuture<Long>> futureList = new ArrayList<>();
      while(true) {
        for(int i = 0; i < BATCH_SIZE; i++){
          String[] swipes = this.buffer.poll(200, TimeUnit.MILLISECONDS);
          if(swipes!=null){
            RedisFuture<Long> future = this.redCommand.sadd(swipes[0], swipes[1]);
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

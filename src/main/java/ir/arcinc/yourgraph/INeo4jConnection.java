package ir.arcinc.yourgraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by tahae on 3/17/2016.
 */
public abstract class INeo4jConnection implements Runnable, AutoCloseable{

    protected Logger logger = LoggerFactory.getLogger(Neo4jEmbed.class.getName());
    protected LinkedBlockingQueue<String> queries = new LinkedBlockingQueue<>();
    protected Thread queryRunner = new Thread(this,"Query Runner");
    protected Thread queryNotifier =  new Thread(()->{
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                Thread.sleep(30 * 1000, 0);
                logger.info("Queries remaining: " + queries.size());
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }
    },"Query Notifier");

    protected INeo4jConnection(){
        queryNotifier.start();
    }

    public abstract List<Map<String, Object>> execute(String query);

    public void executeAsync(String query){
        try {
            queries.put(query);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }
}

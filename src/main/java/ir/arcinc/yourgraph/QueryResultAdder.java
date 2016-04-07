package ir.arcinc.yourgraph;

import org.neo4j.graphdb.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by tahae on 3/27/2016.
 */
public class QueryResultAdder {

    private BlockingQueue<String> queue;
    protected INeo4jConnection connection;
    protected Logger logger = LoggerFactory.getLogger(QueryResultAdder.class.getName());

    ExecutorService resultsPool = Executors.newSingleThreadExecutor(new ThreadFactory() {
        int count = 1;
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "QueryResultAdder - Result[" + count++ + "]");
        }
    });

    public QueryResultAdder(INeo4jConnection connection, BlockingQueue<String> queue) {
        this.queue = queue;
        this.connection = connection;
    }

    public void addQueryResults(String query){
        logger.trace(query);
        try {
            List<Map<String, Object>> rs = connection.execute(query);

            List<String> toBeShuffled = new ArrayList<>();
            for (Map<String, Object> row : rs) {
                toBeShuffled.add((String) row.get("username"));
            }

            if (rs.isEmpty())
                return;

            Collections.shuffle(toBeShuffled);
            toBeShuffled.removeAll(buildExclusions());

            resultsPool.submit(() -> {
                for (String s : toBeShuffled)
                    try {
                        queue.put(s);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                logger.info(toBeShuffled.size() + " usernames added.");
            });
        } catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private List<String> buildExclusions(){
        List<String> exclusions = new ArrayList<>();
        exclusions.add("dota_2_reborn_valve");
        exclusions.add("araghchi");
        exclusions.add("marcopolo_ir");
        exclusions.add("chelseafc");
        exclusions.add("_dota2official_");
        exclusions.add("peeyade");
        return exclusions;
    }
}

package ir.arcinc.yourgraph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.logging.slf4j.Slf4jLogProvider;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by tahae on 3/18/2016.
 */
public class Neo4jEmbed extends INeo4jConnection {

    final private GraphDatabaseService db;

    public Neo4jEmbed() {
        super();
        logger = LoggerFactory.getLogger(Neo4jEmbed.class.getName());
        try {
            db = new GraphDatabaseFactory()
                    .setUserLogProvider(new Slf4jLogProvider())
                    .newEmbeddedDatabaseBuilder(new File("graph"))
                    .newGraphDatabase();

            logger.info("Database started.");

            queryRunner.start();

            registerShutdownHook(db);
        } catch (Exception e){
            logger.error(e.getMessage());
            throw e;
        }
    }

    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread(graphDb::shutdown));
    }

    @Override
    public List<Map<String, Object>> execute(String query) {
        try {
            logger.trace(query);
            try(Transaction tx = db.beginTx()) {
                List<Map<String, Object>> table = new LinkedList<>();
                Result r = db.execute(query);
                while (r.hasNext()) {
                    Map<String, Object> next = r.next();
                    table.add(next);
                }
                tx.success();
                return table;
            }
        } catch (Exception e){
            logger.error(e.getMessage());
            logger.error(query);
        }
        return null;
    }

    @Override
    public void close(){
        db.shutdown();
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (true) {
            try (Transaction tx = db.beginTx()) {
                do{
                    String query = queries.take();
                    logger.trace("Async: " + query);
                    db.execute(query);
                } while (!queries.isEmpty());
                tx.success();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }
}
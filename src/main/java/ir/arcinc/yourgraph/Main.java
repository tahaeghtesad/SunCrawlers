package ir.arcinc.yourgraph;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jinstagram.exceptions.InstagramException;

import java.sql.SQLException;
import java.util.concurrent.*;

/**
 * Created by tahae on 3/15/2016.
 */
public class Main {

    public static void main(String[] args) throws InterruptedException, InstagramException, SQLException {
        Log logger = LogFactory.getLog(Main.class);
        ExecutorService crawlersPool = Executors.newCachedThreadPool(new ThreadFactory() {
            int count = 1;
            @Override
            public Thread newThread(@NotNull Runnable r) {
                return new Thread(r, "Crawler[" + count++ + "]");
            }
        });

//        INeo4jConnection connection = new Neo4jEmbed();
        INeo4jConnection connection = new Neo4jJDBC();

        BlockingQueue<String> queue = new LoggingLinkedBlockingQueue<>();

        PostHTMLExporter exporter = new PostHTMLExporter(connection);

        //starting feedcrawler
        Thread feedExplorer = new Thread(new FeedExplorer(connection), "Feed Explorer");
        feedExplorer.start();

        //starting crawlers
        int NUMBER_OF_EXPLORERS = 6;
        for (int i = 0; i < NUMBER_OF_EXPLORERS; i++) {
            crawlersPool.submit(new Explorer(connection, queue));
        }

        Thread consoleAdder = new Thread(new ConsoleAdder(connection, queue), "Console Adder");
        consoleAdder.start();

        crawlersPool.shutdown();
    }
}
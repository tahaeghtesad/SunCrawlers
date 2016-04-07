package ir.arcinc.yourgraph;

import org.neo4j.graphdb.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by tahae on 4/1/2016.
 */
public class PostHTMLExporter implements Runnable{

    private INeo4jConnection connection;
    private LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private Logger logger = LoggerFactory.getLogger(PostHTMLExporter.class.getName());

    public PostHTMLExporter(INeo4jConnection connection) {
        this.connection = connection;
        Thread thisThread = new Thread(this,"HTML Exporter");
        thisThread.start();
    }

    public void saveUserPosts(String username){
        try {
            queue.put(username);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run(){
        while (true) {
            try {
                String username = queue.take();
                List<Map<String, Object>> urls = connection.execute("match ({username:'" + username + "'})-[r:Posted]->(p:Post{type:'image'}) return p.url;");

                try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File("user_posts\\" + username + ".html")), "UTF-8"))) {
                    out.println(
                            "<!DOCTYPE HTML>" +
                                    "<html>" +
                                    "<head><meta charset=\"UTF-8\"/></head>" +
                                    "<body>");

                    for (Map<String, Object> row : urls) {
                        out.println("<img src=\"" + row.get("p.url") + "\" />");
                    }

                    out.println("</body></html>");
                } catch (FileNotFoundException | UnsupportedEncodingException e) {
                    logger.error(e.getMessage());
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }
    }
}
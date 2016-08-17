package ir.arcinc.yourgraph;

import org.jinstagram.entity.users.feed.MediaFeedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by tahae on 4/1/2016.
 */
public class PostHTMLExporter implements Runnable{

    private INeo4jConnection connection;
    private BlockingQueue<String> queue = new LoggingLinkedBlockingQueue<>();
    private Logger logger = LoggerFactory.getLogger(PostHTMLExporter.class.getName());

    public PostHTMLExporter(INeo4jConnection connection) {
        this.connection = connection;
        Thread thisThread = new Thread(this,"HTML Exporter");
//        thisThread.start();
    }

    public static void main(String[] args) throws SQLException {
        PostHTMLExporter exporter = new PostHTMLExporter(new Neo4jEmbed());

        exporter.saveUserPosts("___az_za___");
        System.out.println("done");
    }

    public void saveUserPostsAsync(String username){
        try {
            queue.put(username);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }

    public void saveUserPostsFromMap(List<MediaFeedData> posts){
        if (posts.isEmpty())
            return;

        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File("user_posts\\" + posts.get(0).getUser().getUserName() + ".html")), "UTF-8"))) {
            out.println(
                    "<!DOCTYPE HTML>" +
                            "<html>" +
                            "<head><meta charset=\"UTF-8\"/></head>" +
                            "<body>");

            for (MediaFeedData post : posts){
                out.println("<img src=\"" + post.getImages().getStandardResolution() + "\"/>");
            }

            out.println("</body></html>");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run(){
        while (true) {
            try {
                String username = queue.take();
                saveUserPosts(username);
            } catch (InterruptedException e){
                logger.error(e.getMessage());
            }
        }
    }

    public void saveUserPosts(String username){
            try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File("user_posts\\" + username + ".html")), "UTF-8"))) {
                out.println(
                        "<!DOCTYPE HTML>" +
                                "<html>" +
                                "<head><meta charset=\"UTF-8\"/></head>" +
                                "<body>");

                for (Map<String, Object> post : connection.execute("match (u{username:'" + username + "'})-[:Posted]->(p{type:'image'}) return p.id,p.caption,p.url;")) {
                    System.out.println(post.get("p.url"));
                    out.println("<img src=\"" + post.get("p.url") + "\" />");
                    out.println("\t<p>" + post.get("p.caption") + "</p>");
                    out.println("\t<ul>");
                    for (Map<String, Object> tagged : connection.execute("match (p{id:'" + post.get("p.id") + "'})<-[:AppearedIn]-(u) return u.username")){
                        out.println("\t\t<li><a href=\"https://www.instagram.com/" + tagged.get("u.username") + "\">" + tagged.get("u.username") + "</a></li>");
                        System.out.println(tagged.get("u.username"));
                    }
                    out.println("\t</ul>");
                }

                out.println("</body></html>");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                logger.error(e.getMessage());
            }
    }
}
package ir.arcinc.yourgraph;

import org.jinstagram.auth.model.Token;
import org.jinstagram.entity.comments.CommentData;
import org.jinstagram.entity.common.User;
import org.jinstagram.entity.users.basicinfo.UserInfoData;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.entity.users.feed.UserFeedData;
import org.jinstagram.exceptions.InstagramRateLimitException;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.concurrent.BlockingQueue;

public class Explorer extends AbstractExplorer{
    private BlockingQueue<String> queue;
    private PostHTMLExporter exporter;

    public Explorer(INeo4jConnection connection, BlockingQueue<String> queue, PostHTMLExporter exporter) {
        super(connection);
        logger = LoggerFactory.getLogger(Explorer.class.getName());
        this.queue = queue;
        this.exporter = exporter;
    }

    private UserInfoData top;
    private List<UserFeedData> followings = new LinkedList<>();
    private List<UserFeedData> followers = new LinkedList<>();
    private List<MediaFeedData> posts = new LinkedList<>();
    private Map<MediaFeedData,List<User>> likes = new HashMap<>();
    private Map<MediaFeedData,List<CommentData>> comments = new HashMap<>();

    @Override
    protected void exploreAndSaveData() {
        try {
            String topUsername = queue.take();

            logger.info("Doing user: " + topUsername);

            String topId = findId(topUsername);

            String accessToken = getAccessTokenForUser(topUsername);
            instagram.setAccessToken(new Token(accessToken, Parameters.clientSecret));

            top = instagram.getUserInfo(topId).getData();
            if (getFollowRelationshipCount(top.getUsername()) > top.getCounts().getFollowedBy() + top.getCounts().getFollows() + 15)
                connection.executeAsync("MATCH ({username:'" + top.getUsername() +"'})-[r:Following]-() DELETE r");

            logger.trace("Getting user followings: " + top.getUsername());
            followings = getFollowings(topId);
            saveFollowings(top.getId(), followings);
            logger.trace("Getting user followers: " + top.getUsername());
            followers = getFollowers(topId);
            saveFollowers(top.getId(), followers);
            logger.trace("Getting user posts: " + top.getUsername());
            posts = getPosts(topId);
            savePosts(posts);
            exporter.saveUserPostsFromMap(posts);
            logger.trace("Getting user likes: " + top.getUsername());
            likes = getLikes(posts);
            saveLikes(likes);
            logger.trace("Getting user comments: " + top.getUsername());
            comments = getComments(posts);
            saveComments(comments);

        } catch (InstagramRateLimitException e){
            logger.error(e.getMessage());
            try {
                Thread.sleep(1800*1000);
            } catch (InterruptedException e1) {
                logger.error(e1.getMessage());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private String getAccessTokenForUser(String username) {
        List<Map<String, Object>> r = connection.execute("MATCH (p{username:'" + username + "'}) WHERE EXISTS(p.access_token) RETURN p.access_token AS access_token");
        if (!r.isEmpty()) {
            return (String) r.get(0).get("access_token");
        }

        r = connection.execute("MATCH (p:Person)-[:Following]->(target{username:'" + username + "'}) WHERE EXISTS(p.access_token) RETURN p.access_token AS access_token LIMIT 1;");
        System.out.println(r.get(0).get("access_token"));
        if (!r.isEmpty()) {
            return (String) r.get(0).get("access_token");
        } else {
            logger.info("No access_token found. Checking whether user is public: " + username);
            return Parameters.myToken;
        }
    }

    private long getFollowRelationshipCount(String user){
        List<Map<String, Object>> res = connection.execute("MATCH ({username:'" + user +"'})-[r:Following]-() RETURN COUNT(r) AS count");
        return Long.valueOf(res.get(0).get("count").toString());
    }
}

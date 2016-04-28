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

    public Explorer(INeo4jConnection connection, BlockingQueue<String> queue) {
        super(connection);
        logger = LoggerFactory.getLogger(Explorer.class.getName());
        this.queue = queue;
    }

    private UserInfoData top;
    private List<UserFeedData> followings = new LinkedList<>();
    private List<UserFeedData> followers = new LinkedList<>();
    private List<MediaFeedData> posts = new LinkedList<>();
    private Map<MediaFeedData,List<User>> likes = new HashMap<>();
    private Map<MediaFeedData,List<CommentData>> comments = new HashMap<>();

    @Override
    protected void getData() {
        try {
            String topUsername = queue.take();

            logger.info("Finding user: " + topUsername);

            String topId = findId(topUsername);

            String accessToken = getAccessTokenForUser(topUsername);
            instagram.setAccessToken(new Token(accessToken, Parameters.clientSecret));

            top = instagram.getUserInfo(topId).getData();

            logger.info("Getting user data: " + top.getUsername());

            followings = getFollowings(topId);
            followers = getFollowers(topId);
            posts = getPosts(topId);
            likes = getLikes(posts);
            comments = getComments(posts);

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

    @Override
    protected void saveData() {
        logger.info("Adding user data: " + top.getUsername());

        updateUser(top);
        if (getFollowRelationshipCount(top.getUsername()) > top.getCounts().getFollowedBy() + top.getCounts().getFollows() + 15)
            connection.executeAsync("MATCH ({username:'" + top.getUsername() +"'})-[r:Following]-() DELETE r");

        saveFollowings(top.getId(), followings);
        saveFollowers(top.getId(), followers);
        savePosts(posts);
        saveLikes(likes);
        saveComments(comments);
    }

    private String getAccessTokenForUser(String username) {
        List<Map<String, Object>> r = connection.execute("MATCH (p{username:'" + username + "'}) WHERE p.access_token IS NOT null RETURN p.access_token AS access_token");
        if (!r.isEmpty()) {
            return (String) r.get(0).get("access_token");
        }

        r = connection.execute("MATCH (p:Person)-[:Following]->(target{username:'" + username + "'}) WHERE p.access_token IS NOT null RETURN p.access_token AS access_token LIMIT 1;");
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

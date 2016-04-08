package ir.arcinc.yourgraph;

import org.jinstagram.Instagram;
import org.jinstagram.entity.comments.CommentData;
import org.jinstagram.entity.common.Pagination;
import org.jinstagram.entity.common.User;
import org.jinstagram.entity.users.basicinfo.UserInfoData;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.exceptions.InstagramException;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by tahae on 3/29/2016.
 */
public class FeedExplorer extends AbstractExplorer {

    protected List<MediaFeedData> posts = new LinkedList<>();
    protected Map<MediaFeedData,List<User>> likes = new HashMap<>();
    protected Map<MediaFeedData,List<CommentData>> comments = new HashMap<>();
    protected List<UserInfoData> users = new LinkedList<>();

    public FeedExplorer(INeo4jConnection connection) {
        super(connection);
        logger = LoggerFactory.getLogger(FeedExplorer.class.getName());
        instagram = new Instagram(Parameters.myToken,Parameters.clientSecret);
    }

    @Override
    protected void getData() {
        try {
            logger.info("Getting news feed...");
            posts = instagram.getUserFeeds().getData();
            likes = getLikes(posts);
            comments = getComments(posts);
            users = getPostingUsers(posts);
            logger.info("Done getting newsfeed.");
        } catch (InstagramException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    protected void saveData() {
        logger.info("Adding newsfeed data.");
        savePosts(posts);
        saveLikes(likes);
        saveComments(comments);
        updateUser(users);

        end();
    }

    protected void end() {
        try {
            logger.info("Sleeping for 10 minutes...");
            Thread.sleep(600*1000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }

    protected void updateUser(List<UserInfoData> users) {
        users.parallelStream().forEach(this::updateUser);
    }

    protected List<UserInfoData> getPostingUsers(List<MediaFeedData> posts) throws InstagramException {
        List<UserInfoData> ret = new LinkedList<>();
        for (MediaFeedData post : posts)
            ret.add(instagram.getUserInfo(post.getUser().getId()).getData());
        return ret;
    }

}
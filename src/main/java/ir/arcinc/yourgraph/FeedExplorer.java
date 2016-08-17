package ir.arcinc.yourgraph;

import org.jinstagram.Instagram;
import org.jinstagram.entity.comments.CommentData;
import org.jinstagram.entity.common.User;
import org.jinstagram.entity.users.basicinfo.UserInfoData;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.exceptions.InstagramException;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    protected void exploreAndSaveData() {
        try {
            logger.info("Getting news feed...");
            posts = instagram.getUserFeeds().getData();
            savePosts(posts);
            logger.trace("Getting likes...");
            likes = getLikes(posts);
            saveLikes(likes);
            logger.trace("Getting comments...");
            comments = getComments(posts);
            saveComments(comments);
            logger.trace("Updating users...");
            users = getPostingUsers(posts);
            updateUser(users);

            end();

        } catch (InstagramException e) {
            logger.error(e.getMessage());
        }
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
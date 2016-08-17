package ir.arcinc.yourgraph;

import org.apache.commons.lang.StringEscapeUtils;
import org.jinstagram.Instagram;
import org.jinstagram.entity.comments.CommentData;
import org.jinstagram.entity.common.FromTagData;
import org.jinstagram.entity.common.Pagination;
import org.jinstagram.entity.common.User;
import org.jinstagram.entity.common.UsersInPhoto;
import org.jinstagram.entity.users.basicinfo.UserInfoData;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.entity.users.feed.UserFeed;
import org.jinstagram.entity.users.feed.UserFeedData;
import org.jinstagram.exceptions.InstagramException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by tahae on 3/29/2016.
 */
public abstract class AbstractExplorer implements Runnable {

    protected Logger logger = LoggerFactory.getLogger(Explorer.class.getName());
    final protected INeo4jConnection connection;
    protected Instagram instagram = new Instagram(Parameters.myToken,Parameters.clientSecret);

    public AbstractExplorer(INeo4jConnection connection) {
        this.connection = connection;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (true){
//            {
            try {
                exploreAndSaveData();
            } catch (Exception e){
                logger.error(e.getMessage());
            }
        }
    }

    protected abstract void exploreAndSaveData();

    private static String toString(UserFeedData data) {
        return String.format(
                "{id:\'%s\', username:\'%s\', fullName:\"%s\", profile_picture:\"%s\"}",
                data.getId(),
                data.getUserName(),
                StringEscapeUtils.escapeJava(data.getFullName()),
                StringEscapeUtils.escapeJava(data.getProfilePictureUrl()));
    }

    private static String toString(UserInfoData user) {
        return String.format(
                "{id:\'%s\', username:\'%s\', fullName:\"%s\", bio:\"%s\", profile_picture:\"%s\", posts:%s, followings:%s, followers:%s}",
                user.getId(),
                user.getUsername(),
                StringEscapeUtils.escapeJava(user.getFullName()),
                StringEscapeUtils.escapeJava(user.getBio()),
                StringEscapeUtils.escapeJava(user.getProfilePicture()),
                user.getCounts().getMedia(),
                user.getCounts().getFollows(),
                user.getCounts().getFollowedBy());
    }

    private static String toString(User data) {
        return String.format(
                "{id:\'%s\', username:\'%s\', fullName:\"%s\", profile_picture:\"%s\"}",
                data.getId(),
                data.getUserName(),
                StringEscapeUtils.escapeJava(data.getFullName()),
                StringEscapeUtils.escapeJava(data.getProfilePictureUrl()));
    }

    private static String toString(FromTagData data) {
        return String.format(
                "{id:\'%s\', username:\'%s\', fullName:\"%s\", profile_picture:\"%s\"}",
                data.getId(),
                data.getUsername(),
                StringEscapeUtils.escapeJava(data.getFullName()),
                StringEscapeUtils.escapeJava(data.getProfilePicture()));
    }

    private static String toString(MediaFeedData data){

        String tags = "[";
        for(int i = 0; i < data.getTags().size(); i++) {
            tags += "\"" + data.getTags().get(i) + "\"";
            if (i != data.getTags().size()-1)
                tags += ",";
        }
        tags += "]";

        return String.format("{id:\'%s\', caption: \"%s\", created_time: \'%s\',tags:%s,type:\'%s\',url:\"%s\"}",
                data.getId(),
                data.getCaption() == null ? "" : StringEscapeUtils.escapeJava(data.getCaption().getText()),
                data.getCreatedTime(),
                tags,
                data.getType(),
                StringEscapeUtils.escapeJava(Objects.equals(data.getType(), "image") ? data.getImages().getStandardResolution().getImageUrl() : data.getVideos().getStandardResolution().getUrl())
        );
    }

    private String toString(CommentData data){
        return String.format("{id:\'%s\',created_time:\'%s\',text:\'%s\'}", data.getId(), data.getCreatedTime(), data.getText().replaceAll("[^a-zA-Z0-9 @#]", ""));
    }

    protected List<UserFeedData> getFollowings(String id){
        List<UserFeedData> followings = new ArrayList<>();
        Pagination p = null;
        do {
            try {
                UserFeed feed = p == null ? instagram.getUserFollowList(id) : instagram.getUserFollowListNextPage(p);
                p = feed.getPagination();
                followings.addAll(feed.getUserList());
            } catch (InstagramException e) {
                logger.error(e.getMessage());
            }
        } while (p==null || p.hasNextPage());
        return followings;
    }

    protected List<UserFeedData> getFollowers(String id){
        List<UserFeedData> followers = new ArrayList<>();
        Pagination p = null;
        do {
            try {
                UserFeed feed = p == null ? instagram.getUserFollowedByList(id) : instagram.getUserFollowedByListNextPage(p);
                p = feed.getPagination();
                followers.addAll(feed.getUserList());
            } catch (InstagramException e) {
                logger.error(e.getMessage());
            }
        } while (p==null || p.hasNextPage());


        return followers;
    }

    protected List<MediaFeedData> getPosts(String id){
        List<MediaFeedData> posts = new LinkedList<>();
        Pagination p = null;
        while (p == null || p.hasNextPage()){
            try {
                MediaFeed feed = p == null ? instagram.getRecentMediaFeed(id) : instagram.getRecentMediaNextPage(p);
                p = feed.getPagination();
                posts.addAll(feed.getData());
            } catch (InstagramException e) {
                logger.error(e.getMessage());
            }
        }

        return posts;
    }

    protected Map<MediaFeedData,List<User>> getLikes(List<MediaFeedData> posts) throws InstagramException {
        Map<MediaFeedData,List<User>> ret = new HashMap<>();
        for (MediaFeedData post : posts)
            ret.put(post, instagram.getUserLikes(post.getId()).getUserList());
        return ret;
    }

    protected Map<MediaFeedData,List<CommentData>> getComments(List<MediaFeedData> posts) throws InstagramException {
        Map<MediaFeedData, List<CommentData>> ret = new HashMap<>();
        for (MediaFeedData post : posts){
            ret.put(post,instagram.getMediaComments(post.getId()).getCommentDataList());
        }
        return ret;
    }

    protected void updateUser(UserInfoData user) {
        connection.executeAsync("MERGE (u:Person{id:'" + user.getId() + "'}) ON MATCH SET u+=" + toString(user) + " ON CREATE SET u=" + toString(user) + " RETURN u;");
    }

    protected void saveFollowings(String id, List<UserFeedData> followings){
        for (UserFeedData friend : followings) {
            connection.executeAsync("MERGE (n:Person{id:'" + friend.getId() + "'}) ON MATCH SET n+=" + toString(friend) + " ON CREATE SET n=" + toString(friend) + " RETURN n;");
            connection.executeAsync("MATCH (a:Person),(b:Person) WHERE a.id='" + id + "' AND b.id='" + friend.getId() + "' CREATE UNIQUE (a)-[r:Following]->(b) RETURN r;");
        }
    }

    protected void saveFollowers(String id, List<UserFeedData> followers){
        for (UserFeedData friend : followers) {
            connection.executeAsync("MERGE (n:Person{id:'" + friend.getId() + "'}) ON MATCH SET n+=" + toString(friend) + " ON CREATE SET n=" + toString(friend) + " RETURN n;");
            connection.executeAsync("MATCH (a:Person),(b:Person) WHERE a.id='" + friend.getId() + "' AND b.id='" + id + "' CREATE UNIQUE (a)-[r:Following]->(b) RETURN r;");
        }
    }

    protected void savePosts(List<MediaFeedData> posts) {
        for(MediaFeedData post : posts){
            connection.executeAsync("MERGE (n:Post{id:'" + post.getId() + "'}) ON MATCH SET n+=" + toString(post) + " ON CREATE SET n=" + toString(post) + " RETURN n;");
            connection.executeAsync("MATCH (n:Person),(p:Post) WHERE n.id='" + post.getUser().getId() + "' AND p.id='" + post.getId() + "' CREATE UNIQUE (n)-[r:Posted]->(p) RETURN r;");

            for (UsersInPhoto usersInPhoto : post.getUsersInPhotoList()){
                User user = usersInPhoto.getUser();
                connection.executeAsync("MERGE (n:Person{id:'" + user.getId() + "'}) ON MATCH SET n+=" + toString(user) + " ON CREATE SET n=" + toString(user) + " RETURN n;");
                connection.executeAsync("MATCH (u:Person),(p:Post) WHERE u.id='" + user.getId() + "' AND p.id='" + post.getId() + "' CREATE UNIQUE (u)-[r:AppearedIn]->(p) RETURN r;");
            }
        }
    }

    protected void saveLikes(Map<MediaFeedData,List<User>> posts){
        for (MediaFeedData post : posts.keySet())
            for (User user : posts.get(post)){
                connection.executeAsync("MERGE (n:Person{id:'" + user.getId() + "'}) ON MATCH SET n+=" + toString(user) + " ON CREATE SET n=" + toString(user) + " RETURN n;");
                connection.executeAsync("MATCH (u:Person),(p:Post) WHERE u.id='" + user.getId() + "' AND p.id='" + post.getId() + "' CREATE UNIQUE (u)-[r:Liked]->(p) RETURN r;");
            }
    }
    protected void saveComments(Map<MediaFeedData,List<CommentData>> posts){
        for (MediaFeedData post : posts.keySet())
            for (CommentData comment : posts.get(post)){
                connection.executeAsync("MERGE (n:Person{id:'" + comment.getCommentFrom().getId() + "'}) ON MATCH SET n+=" + toString(comment.getCommentFrom()) + " ON CREATE SET n=" + toString(comment.getCommentFrom()) + " RETURN n;");
                connection.executeAsync("MATCH (u:Person),(p:Post) WHERE u.id='" + comment.getCommentFrom().getId() + "' AND p.id='" + post.getId() + "' MERGE (u)-[r:CommentedOn{id:'" + comment.getId() + "'}]->(p) ON CREATE SET r=" + toString(comment) + " ON MATCH SET r+=" + toString(comment) + " RETURN r;");
            }
    }

    protected String findId(String username) throws InstagramException {
        for (UserFeedData user : instagram.searchUser(username).getUserList())
            if (user.getUserName().equals(username))
                return user.getId();
        throw new InstagramException("User not found: " + username);
    }
}

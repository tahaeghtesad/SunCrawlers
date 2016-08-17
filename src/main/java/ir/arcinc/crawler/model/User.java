package ir.arcinc.crawler.model;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by tahae on 8/17/2016.
 */
@NodeEntity
public class User {
    @GraphId
    private Long id;
    private String username;
    private String name;
    private String bio;
    private String profilePictureUrl;

    @Relationship(type = "Following", direction = Relationship.OUTGOING)
    private Set<User> followings = new HashSet<>();

    public User(Long id, String username, String name, String bio, String profilePictureUrl) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.bio = bio;
        this.profilePictureUrl = profilePictureUrl;
    }

    public User(String username, String name, String profilePictureUrl, Long id) {
        this.username = username;
        this.name = name;
        this.profilePictureUrl = profilePictureUrl;
        this.id = id;
    }

    public User(String username, String name, String bio, String profilePictureUrl) {
        this.username = username;
        this.name = name;
        this.bio = bio;
        this.profilePictureUrl = profilePictureUrl;
    }

    public User(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}

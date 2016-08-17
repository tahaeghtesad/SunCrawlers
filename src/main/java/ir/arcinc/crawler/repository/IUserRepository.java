package ir.arcinc.crawler.repository;

import ir.arcinc.crawler.model.User;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by tahae on 8/17/2016.
 */
@Repository
public interface IUserRepository extends GraphRepository<User> {
    public User findById(Long id);
    public User findByUsername(String username);
}

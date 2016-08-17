package ir.arcinc.crawler.config;

import org.neo4j.ogm.session.SessionFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by tahae on 8/17/2016.
 */
@Configuration
@EnableNeo4jRepositories("ir.arcinc.crawler.repository")
@EnableTransactionManagement
public class SpringDataConfig extends Neo4jConfiguration{
    @Override
    public SessionFactory getSessionFactory() {
        org.neo4j.ogm.config.Configuration configuration = new org.neo4j.ogm.config.Configuration();
        configuration.driverConfiguration()
                .setDriverClassName("org.neo4j.ogm.drivers.http.driver.HttpDriver")
                .setURI("http://neo4j:admin@localhost:7474");

        return new SessionFactory(configuration, "ir.arcinc.crawler.model");
    }

}

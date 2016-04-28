package ir.arcinc.yourgraph;

import org.neo4j.jdbc.Driver;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Created by tahae on 3/17/2016.
 */
public class Neo4jJDBC extends INeo4jConnection {

    private Connection connection = null;
    private Thread queryRunner = new Thread(this,"Query Runner");

    public Neo4jJDBC() throws SQLException {
        logger = LoggerFactory.getLogger(Neo4jJDBC.class.getName());
        try {
            Class.forName("org.neo4j.jdbc.Driver");
            Driver driver = new Driver();
            Properties prop = new Properties();
            prop.put("user", "neo4j");
            prop.put("password", "admin@12");
//            prop.put("debug","true");
            connection = driver.connect(Parameters.neo4jConnectionUrl, prop);
            logger.info("Connection established to database @" + Parameters.neo4jConnectionUrl);

            queryRunner.start();

        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> execute(String query) {
        try {
            logger.trace(query);

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)
            ){
                List<Map<String,Object>> table = new LinkedList<>();
                while (rs.next()){
                    Map<String,Object> row = new TreeMap<>();
                    for (int i=1; i <= rs.getMetaData().getColumnCount(); i++){
                        row.put(rs.getMetaData().getColumnName(i),rs.getObject(i));
                    }
                    table.add(row);
                }
                return table;
            }

        } catch (SQLException e) {
            logger.error(query);
            logger.error(e.getMessage());
        }
        return null;
    }

    @Override
    public void close() {
        try {
            connection.close();
            queryRunner.stop();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (true){
            try {
                String query = queries.take();
                logger.trace("Async: " + query);
                try (Statement stmt = connection.createStatement()) {
                    stmt.executeUpdate(query);
                } catch (SQLException e){
                    logger.error(e.getMessage());
                }
            } catch (InterruptedException | RuntimeException e) {
                logger.error(e.getMessage());
            }
        }
    }

    /*@Override
    public void executeAsync(String query) {
        try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate(query);
        } catch (SQLException | RuntimeException e) {
            logger.error(e.getMessage());
        }
    }*/
}

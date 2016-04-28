package ir.arcinc.yourgraph;

import com.google.gson.Gson;
import scala.util.parsing.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Created by tahae on 4/7/2016.
 */
public class Test {
    public static void main(String[] args) {
        try(INeo4jConnection connection = new Neo4jJDBC();
            PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File("graph.sigma.js"))));){

            List<Map<String,Object>> res = connection.execute("match (p:Person) return p.id as id,p.username,p.profile_picture,p.followings,p.followers,p.posts;");

            out.print("{\"nodes\":[");
            for (Map<String,Object> row : res){
                if (row!=res.get(0))
                    out.print(",");
                out.print("{");
                out.print("\"id\":"); out.print("\"" + row.get("id") + "\"");
                out.print("\"label\":"); out.print("\"" + row.get("p.username") + "\"");
                out.print("\"size\":"); out.print("\"" + score(row) + "\"");
                out.print("\"x\":"); out.print("\"" + Math.random() * 4096 + "\"");
                out.print("\"y\":"); out.print("\"" + Math.random() * 4096 + "\"");
                out.print("}");

            }
            out.print("],\"edges\":[");

            res = connection.execute("match (u)-[r:Following]-(t) return r.id as id, u.id as Source,t.id as Target;");
            for (Map<String,Object> row : res) {
                if (row != res.get(0))
                    out.print(",");
                out.print("{");
                out.print("\"id\":"); out.print("\"" + row.get("id") + "\"");
                out.print("\"source\":"); out.print("\"" + row.get("Source") + "\"");
                out.print("\"target\":"); out.print("\"" + row.get("Target") + "\"");
                out.print("}");
            }

            System.out.println("done");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int score(Map<String,Object> user){
        try {
            int posts = (int) user.get("p.posts");
            int followers = (int) user.get("p.followers");
            int followings = (int) user.get("p.followings");
            return (posts+followings)/followers;
        }catch (NullPointerException e){
            return 0;
        }
    }
}

package ir.arcinc.yourgraph;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

/**
 * Created by tahae on 3/27/2016.
 */
public class ConsoleAdder extends QueryResultAdder implements Runnable{
    public ConsoleAdder(INeo4jConnection connection, BlockingQueue<String> queue) {
        super(connection,queue);
    }

    @Override
    public void run() {
        Scanner console = new Scanner(System.in);
        while (console.hasNext()){
            String query = console.nextLine();
            addQueryResults(query);
        }
    }
}

package ir.arcinc.yourgraph;

import java.io.*;

/**
 * Created by tahae on 4/7/2016.
 */
public class Test {
    public static void main(String[] args) {
        try(PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File("hi")),"UTF-8"))) {
            out.println("hi");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}

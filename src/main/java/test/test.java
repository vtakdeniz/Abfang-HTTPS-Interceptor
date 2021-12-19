package main.java.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.TypeDescriptor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

interface car{
    void test();
}
public class test {
    public static void main(String[] args) throws IOException, InterruptedException {
        /*Queue<String> queue = new LinkedList<>();
        queue.add("a");
        queue.add("b");
        queue.add("d");
        queue.add("e");
        queue.add("f");
        queue.add("g");
        queue.stream().forEach((a)->System.out.println(a));
        System.out.println("\n******************************\n");
        queue.remove();
        queue.remove();
        queue.stream().forEach(System.out::println);
*/
//        Consumer a= (l)-> System.out.println();
//        car c = ()-> System.out.println("car");
        Runtime r = Runtime.getRuntime();
        Process p = r.exec("uname -a");
        p.waitFor();
        BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";

        while ((line = b.readLine()) != null) {
            System.out.println(line);
        }

        b.close();
    }
}

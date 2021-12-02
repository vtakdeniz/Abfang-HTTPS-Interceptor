package main.java.test;

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
    public static void main(String[] args) {
        Queue<String> queue = new LinkedList<>();
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

//        Consumer a= (l)-> System.out.println();
//        car c = ()-> System.out.println("car");
    }
}

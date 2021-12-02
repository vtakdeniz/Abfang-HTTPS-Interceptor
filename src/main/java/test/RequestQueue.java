package main.java.test;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RequestQueue {
    public static BlockingQueue browserToServer = new LinkedBlockingQueue();
    public static BlockingQueue serverToBrowser = new LinkedBlockingQueue();

}

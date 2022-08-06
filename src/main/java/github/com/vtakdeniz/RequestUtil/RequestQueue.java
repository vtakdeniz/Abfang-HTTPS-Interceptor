package github.com.vtakdeniz.RequestUtil;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RequestQueue {
    public static BlockingQueue browserToServer = new LinkedBlockingQueue();
    public static BlockingQueue serverToBrowser = new LinkedBlockingQueue();
    public static BlockingQueue forwardable = new LinkedBlockingQueue();
}

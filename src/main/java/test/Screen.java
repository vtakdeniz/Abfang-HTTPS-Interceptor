package main.java.test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

enum ScreenType {
    STANDARD_OUTPUT,
    USER_INTERFACE
}

public class Screen extends Thread{

    private static ScreenType screenType;
    private static BlockingQueue screen_requests = new LinkedBlockingQueue();

    public Screen(ScreenType type){
        this.screenType=type;
    }

    @Override
    public void run() {
     while (true){

     }
    }

    public static void sendToScreen(RequestWrapper wrapper){
        try {
            screen_requests.put(wrapper);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

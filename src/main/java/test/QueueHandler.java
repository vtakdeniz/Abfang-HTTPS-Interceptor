package main.java.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueHandler extends Thread{

    private static BlockingQueue screen_requests = new LinkedBlockingQueue();

    @Override
    public void run() {
        Thread listenBrowserThread = new Thread(() -> listenBrowserRequests());
        listenBrowserThread.start();
        Thread sendToScreenThread = new Thread(() -> sendToScreen());
        sendToScreenThread.start();
    }

    public QueueHandler(){
    }

    private void sendToScreen(){
        try {
            while(true) {
                var request_wrapper=(RequestWrapper)screen_requests.take();
                var request = request_wrapper.parser.getRequest();
                System.out.println(request);
                //System.out.println("Enter '$' to forward request, or '!' to drop it");
                //Scanner in = new Scanner(System.in);
                String s =""; //in.nextLine();
                if(true||s.equals("$")){
                    System.out.println("writing to socket");
                    System.out.println("socket type : "+request_wrapper.targer_socket);
                    if(!request_wrapper.socket.isOutputShutdown()){

                        request_wrapper.targer_socket.write(request);
                        request_wrapper.targer_socket.flush();
                        if(request_wrapper.local_requests.size()>0){
                            request_wrapper.local_requests.poll();
                        }
                        else{
                            request_wrapper.lock.unlock();
                        }
                    }
                    else{
                        System.out.println("Request couldn't be forwarded : ");
                        System.out.println(request);
                    }
                }
                else{
                    request_wrapper.local_requests.poll();
                    System.out.println("Request dropped");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void listenBrowserRequests()  {

            try {
                while(true) {
                var request=RequestQueue.browserToServer.take();
                screen_requests.put(request);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

    }

    private void listenServerRequests()  {
            try {
                while(true) {
                var request=RequestQueue.serverToBrowser.take();
                screen_requests.put(request);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

}
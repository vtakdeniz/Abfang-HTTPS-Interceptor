package main.java.test;

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

        Thread listenWritables =new Thread(()-> listenForwardable());
        listenWritables.start();

    }

    public QueueHandler(){

    }

    private void sendToScreen(){
        try {
            while (true){
                var request_wrapper=(RequestWrapper)screen_requests.take();
                boolean isSuccessful=Screen.sendToScreen(request_wrapper);
                if(!isSuccessful){
                    //TODO : Add custom exception
                    System.out.println("\nCAN'T INSERT\nQUEUE IS FULL");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void listenForwardable(){
        try{
           while(true){
               var request_wrapper=(RequestWrapper)RequestQueue.forwardable.take();
               var request=request_wrapper.parser.getRequest();
               if(!request_wrapper.socket.isOutputShutdown()){
                   request_wrapper.targer_socket.write(request);
                   request_wrapper.targer_socket.flush();
                   if(request_wrapper.local_requests.size()>0){
                       Object isComplete = request_wrapper.local_requests.poll();
                       if(isComplete==null){
                           throw new Exception("CANNOT CLOSE SOCKET AFTER FORWARD\n" +
                                   "Local request of the wrapper queue returned null\n+" +
                                   "Socket : "+request_wrapper.socket+"\n"+
                                   "HOST : "+request_wrapper.parser.getHeader("HOST"));
                       }
                   }
                   else{
                       request_wrapper.lock.countDown();
                   }
               }
               else{
                   throw new Exception("CANNOT FORWARD REQUEST : \n" +
                           request);
               }
           }
        }
        catch (Exception e){
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

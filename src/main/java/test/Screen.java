package main.java.test;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
        if(screenType==ScreenType.STANDARD_OUTPUT){
            Thread so = new Thread(()->sendToUserSO());
            so.start();
        }
        else{
            try {
                throw new Exception("NOT IMPLEMENTED :: \nrun() :: ScreenType couldn't be set to gui");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean sendToScreen(RequestWrapper wrapper){
        boolean isSuccessful=false;
        try {
            isSuccessful=screen_requests.offer(wrapper, 1000,TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return isSuccessful;
    }

    private void sendToUserSO(){
        try {
            while(true) {
                var request_wrapper=(RequestWrapper)screen_requests.take();
                var request = request_wrapper.parser.getRequest();
                System.out.println("1. Enter 'f' to forward request.\n" +
                        "2. Press 'e' to edit request.\n" +
                        "3. Press 'd' to drop request.");
                System.out.println(request);
                handleInput(request_wrapper);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleInput(RequestWrapper request_wrapper) throws Exception {
        boolean isCommandCorrect=false;
        while(!isCommandCorrect){
            Scanner in = new Scanner(System.in);
            String command =in.nextLine();
            if(command.equals("f")){
                isCommandCorrect=true;
                RequestQueue.writable.offer(request_wrapper,1000,TimeUnit.MILLISECONDS);
            }
            else if(command.equals("d")){
                System.out.println("REQUEST DROPPED");
                isCommandCorrect=true;
                Object isComplete = request_wrapper.local_requests.poll();
                if(isComplete==null){
                    throw new Exception("CANNOT CLOSE SOCKET AFTER DROP\n" +
                            "Local request of the wrapper returns null\n+" +
                            "Socket : "+request_wrapper.socket+"\n"+
                            "HOST : "+request_wrapper.parser.getHeader("HOST"));
                }
            }
            else if(command.equals("e")){
                isCommandCorrect=true;
                editCommand(request_wrapper);
            }
            else{
                System.out.println("Wrong Input");
            }

        }

    }

    public static void editCommand(RequestWrapper request_wrapper) throws InterruptedException {
        System.out.println("Press 'b' to edit request body or " +
                "type the name of the header that will be edited");
        boolean isEditParamCorrect=false;

        while(!isEditParamCorrect){
            Scanner edit_input = new Scanner(System.in);
            String edit_string =edit_input.nextLine();
            if (edit_string.equals('b')){
                isEditParamCorrect=true;
            }
            else{
                Object header=request_wrapper.parser.getHeader(edit_string);
                if(header==null){
                    System.out.println("No header is available named with given input. Try again.");
                }
                else {
                    isEditParamCorrect=true;
                    System.out.print("Editing header : "+(String)header);
                    System.out.println("Enter the new value of the header ");
                    Scanner header_input = new Scanner(System.in);
                    String header_value =header_input.nextLine();
                    request_wrapper.parser.setHeader((String)header,header_value);
                    RequestQueue.writable.offer(request_wrapper,1000,TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}

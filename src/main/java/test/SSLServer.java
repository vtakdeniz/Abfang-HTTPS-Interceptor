package main.java.test;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.IOException;
import java.security.cert.CertificateException;
import javax.net.ssl.*;
import javax.swing.plaf.synth.SynthLookAndFeel;

import org.brotli.dec.BrotliInputStream;

class SSLServer extends Thread {
    String protocol = "TLS";
    //String keystoreFilenameBase = "/Users/veliakdeniz/Desktop/ssl_cert#2/youtube.keystore";
    String keystoreFilenameBase = "/Users/veliakdeniz/Desktop/new_interceptor_test/newKeyStoreFileName.keystore";
    char[] storepass = "password".toCharArray();
    char[] keypass = "password".toCharArray();
    String alias = "testxx";
    static int PORT_NUM=9200;
    /*Socket browser_socket;
    SSLSocket target_socket;
    PrintWriter browser_socket_output;
    PrintWriter target_socket_output;
    */
    public SSLServer(int port){
        this.PORT_NUM=port;
    }

   /* public static void main(String[] args) {
        (new SSLServer()).start();
    }*/

    @Override
    public void run(){

        try{
            //////////
            /*KeyStore ks = KeyStore.getInstance("JKS");
            InputStream readStream = new FileInputStream("/Users/veliakdeniz/Desktop/new_interceptor_test/newKeyStoreFileName.jks");
            ks.load(readStream, "password".toCharArray());
            Key key = ks.getKey("keyAlias", "password".toCharArray());
            readStream.close();
            System.out.println(key.getAlgorithm());*/
            //////////

            FileInputStream fIn = new FileInputStream(keystoreFilenameBase);
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(fIn, storepass);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keystore, keypass);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keystore);

            SSLContext ctx = SSLContext.getInstance(protocol);
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            SSLServerSocketFactory factory = ctx.getServerSocketFactory();
            ServerSocket listener = factory.createServerSocket(SSLServer.PORT_NUM);
            SSLServerSocket sslListener = (SSLServerSocket) listener;

            sslListener.setNeedClientAuth(false);
            sslListener.setEnabledProtocols(new String[] {"TLSv1.2"});
            // NIO to be implemented
            while (true) {
                try (Socket browser_socket = sslListener.accept()) {
                    SSLSocketFactory ssl_client_socket_factory =
                            (SSLSocketFactory)SSLSocketFactory.getDefault();
                    SSLSocket ssl_socket =
                            (SSLSocket)ssl_client_socket_factory.createSocket("www.youtube.com", 443);

                    ssl_socket.startHandshake();

                    //this.browser_socket_output = new PrintWriter(this.browser_socket.getOutputStream(),false, Charset.forName("ISO-8859-1"));

                    //this.target_socket_output = new PrintWriter(this.target_socket.getOutputStream(),false, Charset.forName("ISO-8859-1"));


                    Thread browser_listener = new Thread(()->forwardDataFromBrowser(browser_socket,ssl_socket));
                    browser_listener.start();
                    Thread target_listener_thread = new Thread(()->forwardData(ssl_socket,browser_socket));
                    target_listener_thread.start();

                    target_listener_thread.join();
                    /*PrintWriter out = new PrintWriter(browser_socket.getOutputStream(), false);
                    out.print("Hello World!\n");
                    out.print("Hello World!\n");
                    out.flush();*/

                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }


        } catch (FileNotFoundException | KeyManagementException | NoSuchAlgorithmException | CertificateException e) {
            //e.printStackTrace();
        } catch (KeyStoreException e) {
            //e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private void forwardDataFromBrowser(Socket inputSocket, Socket outputSocket) {
        CountDownLatch lock = new CountDownLatch(1);
        
        BlockingQueue local_requests = new LinkedBlockingQueue();
        try {

            BufferedReader inputStream = new BufferedReader(
                    new InputStreamReader(
                            inputSocket.getInputStream()));
            try {
                BufferedWriter outputStream= new BufferedWriter(new OutputStreamWriter(outputSocket.getOutputStream()));
                try {
                    StringBuffer line=new StringBuffer();
                    int temp=0;
                    do {
                        temp = inputStream.read();
                        line.append((char)temp);
                        if(!inputStream.ready()&&temp!=-1){
                            HttpRequestParser hrp= new HttpRequestParser();
                            hrp.parseRequest(line.toString());
                            if(hrp.isHeaderAvailable("Accept-Encoding")){
                                hrp.deleteHeader("Accept-Encoding");
                                line.append(hrp.getRequest());
                                var request_wrapper=new RequestWrapper(hrp,outputStream,outputSocket,lock,local_requests);
                                local_requests.put(request_wrapper);
                                RequestQueue.browserToServer.put(request_wrapper);
                                //TODO Implement exception

                            }
                            /*outputStream.write(line.toString());
                            outputStream.flush();*/
                            line.delete(0,line.length());
                        }
                    }
                    while(temp!=-1);
                } catch (InterruptedException e) {

                }
                finally {
                    //TODO : close socket if  browser  tab is closed
                    if (!outputSocket.isOutputShutdown()) {
                        lock.await();
                        System.out.println("closing socket");
                        outputSocket.shutdownOutput();
                    }
                }
            } finally {
                if (!inputSocket.isInputShutdown()) {
                    inputSocket.shutdownInput();
                }
            }
        } catch (IOException | InterruptedException e) {
            //e.printStackTrace();  // TODO: implement catch
        }
    }

    private static void forwardData(Socket inputSocket, Socket outputSocket) {

        try {
            InputStream inputStream = inputSocket.getInputStream();
            try {
                OutputStream outputStream = outputSocket.getOutputStream();
                try {
                    byte[] buffer = new byte[4096];
                    int read;
                    do {
                        read = inputStream.read(buffer);
                        if (read > 0) {
                            outputStream.write(buffer, 0, read);
                            if (inputStream.available() < 1) {
                                outputStream.flush();
                            }
                        }
                    } while (read >= 0);
                } finally {
                    if (!outputSocket.isOutputShutdown()) {
                        outputSocket.shutdownOutput();
                    }
                }
            } finally {
                if (!inputSocket.isInputShutdown()) {
                    inputSocket.shutdownInput();
                }
            }
        } catch (IOException e) {
           // e.printStackTrace();  // TODO: implement catch
        }
    }

    private static void forwardDataToBrowser(Socket inputSocket, Socket outputSocket) {

        try {
            InputStream inputStream = inputSocket.getInputStream();
            try {
                OutputStream outputStream = outputSocket.getOutputStream();
                try {
                    byte[] buffer = new byte[4096];
                    int read;
                    do {
                        read = inputStream.read(buffer);
                        if (read > 0) {
                            //new BufferedReader(new InputStreamReader(new BrotliInputStream(new ByteArrayInputStream(buffer))));
                            //System.out.println("[+] Server sending : "+new String(buffer).replaceAll("\r\n","*r*n\n"));
                            outputStream.write(buffer, 0, read);
                            if (inputStream.available() < 1) {
                                outputStream.flush();
                                //System.out.println("********-------------*******");
                            }
                        }
                    } while (read >= 0);
                } finally {
                    if (!outputSocket.isOutputShutdown()) {
                        outputSocket.shutdownOutput();
                    }
                }
            } finally {
                if (!inputSocket.isInputShutdown()) {
                    inputSocket.shutdownInput();
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();  // TODO: implement catch
        }
    }

    private static void forwardDataToServer(Socket inputSocket, Socket outputSocket) {

        try {
            InputStream inputStream = inputSocket.getInputStream();
            try {
                OutputStream outputStream = outputSocket.getOutputStream();
                try {
                    byte[] buffer = new byte[4096];
                    int read;
                    do {
                        read = inputStream.read(buffer);
                        if (read > 0) {
                            //new BufferedReader(new InputStreamReader(new BrotliInputStream(new ByteArrayInputStream(buffer))));
                            //System.out.println("[+] Browser sending : "+new String(buffer).replaceAll("\r\n","*r*n\n"));
                            outputStream.write(buffer, 0, read);
                            if (inputStream.available() < 1) {

                                outputStream.flush();
                                //System.out.println("********-------------*******");
                            }
                        }
                    } while (read >= 0);
                } finally {
                    if (!outputSocket.isOutputShutdown()) {
                        outputSocket.shutdownOutput();
                    }
                }
            } finally {
                if (!inputSocket.isInputShutdown()) {
                    inputSocket.shutdownInput();
                }
            }
        } catch (IOException e) {
           // e.printStackTrace();  // TODO: implement catch
        }
    }

    /*public void listen_target_socket(SSLSocket target_socket)  {
        String temp;
        String inputLine;
        BufferedReader in = null;

        System.out.println("target socket is closed  "+this.target_socket.isClosed()+" ");
        System.out.println("target socket is input  "+this.target_socket.isInputShutdown()+" ");
        System.out.println("target socket is output  "+this.target_socket.isOutputShutdown()+" ");

        try {
            in = new BufferedReader(
                    new InputStreamReader(
                            target_socket.getInputStream()));

            while ((inputLine = in.readLine())!=null){
                temp = inputLine;
                browser_socket_output.write(temp);
                System.out.println(temp.replaceAll("\r",""));
                if(!in.ready()){browser_socket_output.flush();continue;}
            }
            browser_socket_output.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /*public void listen_browser_socket(Socket browser_socket){
        String temp;
        String inputLine;
        BufferedReader in = null;

        System.out.println("browser socket is closed  "+this.browser_socket.isClosed()+" ");
        System.out.println("browser socket is input  "+this.browser_socket.isInputShutdown()+" ");
        System.out.println("browser socket is output  "+this.browser_socket.isOutputShutdown()+" ");
        try {

            in = new BufferedReader(
                    new InputStreamReader(
                            browser_socket.getInputStream()));

            while ((inputLine = in.readLine())!=null){

                temp = inputLine;
                target_socket_output.write(temp);
                System.out.println(temp.replaceAll("\r",""));
                if(!in.ready()){target_socket_output.flush();continue;}
            }
            target_socket_output.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

}
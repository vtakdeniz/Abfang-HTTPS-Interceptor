package github.com.vtakdeniz.Server;

import github.com.vtakdeniz.RequestUtil.HttpRequestParser;
import github.com.vtakdeniz.RequestUtil.RequestQueue;
import github.com.vtakdeniz.RequestUtil.RequestWrapper;

import javax.net.ssl.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

class SSLServer extends Thread {
    String protocol = "TLS";
    String keystoreFilenameBase = "/Users/veliakdeniz/Desktop/new_interceptor_test/newKeyStoreFileName.keystore";
    char[] storepass = "password".toCharArray();
    char[] keypass = "password".toCharArray();
    int PORT_NUM;
    public SSLServer(int port,String domain_name){
        this.PORT_NUM=port;
    }
    @Override
    public void run(){
        try{
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
            ServerSocket listener = factory.createServerSocket(this.PORT_NUM);
            SSLServerSocket sslListener = (SSLServerSocket) listener;

            sslListener.setNeedClientAuth(false);
            sslListener.setEnabledProtocols(new String[] {"TLSv1.2"});
            // TODO :NIO to be implemented
            while (true) {
                try (Socket browser_socket = sslListener.accept()) {
                    SSLSocketFactory ssl_client_socket_factory =
                            (SSLSocketFactory)SSLSocketFactory.getDefault();
                    SSLSocket ssl_socket =
                            (SSLSocket)ssl_client_socket_factory.createSocket("www.youtube.com", 443);

                    ssl_socket.startHandshake();
                    Thread browser_listener = new Thread(()->forwardDataFromBrowser(browser_socket,ssl_socket));
                    browser_listener.start();
                    Thread target_listener_thread = new Thread(()->forwardData(ssl_socket,browser_socket));
                    target_listener_thread.start();
                    target_listener_thread.join();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (KeyManagementException | NoSuchAlgorithmException |
                CertificateException | KeyStoreException |
                UnrecoverableKeyException | IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();  // TODO: implement catch
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
            e.printStackTrace();  // TODO: implement catch
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
            e.printStackTrace();  // TODO: implement catch
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
           e.printStackTrace();  // TODO: implement catch
        }
    }
}
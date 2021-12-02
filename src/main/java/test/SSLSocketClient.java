package main.java.test;

import java.io.*;
import javax.net.ssl.*;

/*
 * This example demostrates how to use a SSLSocket as client to
 * send a HTTP request and get response from an HTTPS server.
 * It assumes that the client is not behind a firewall
 */

public class SSLSocketClient {

    public static void main(String[] args) throws Exception {
        try {
            SSLSocketFactory factory =
                    (SSLSocketFactory)SSLSocketFactory.getDefault();
            SSLSocket socket =
                    (SSLSocket)factory.createSocket("www.youtube.com", 443);

            /*
             * send http request
             *
             * Before any application data is sent or received, the
             * SSL socket will do SSL handshaking first to set up
             * the security attributes.
             *
             * SSL handshaking can be initiated by either flushing data
             * down the pipe, or by starting the handshaking by hand.
             *
             * Handshaking is started manually in this example because
             * PrintWriter catches all IOExceptions (including
             * SSLExceptions), sets an internal error flag, and then
             * returns without rethrowing the exception.
             *
             * Unfortunately, this means any error messages are lost,
             * which caused lots of confusion for others using this
             * code.  The only way to tell there was an error is to call
             * PrintWriter.checkError().
             */
            socket.startHandshake();

            PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream())));

            out.print("GET / HTTP/1.1\r\n");
            out.print("Host: www.youtube.com\r\n");
            out.print("Cookie: VISITOR_INFO1_LIVE=WOpsqUYLOSc; PREF=f4=4000000&tz=Europe.Istanbul&f6=40000000; GPS=1; YSC=FZdsPMJFBcA\r\n");
            out.print("Sec-Ch-Ua: \"Chromium\";v=\"95\", \";Not A Brand\";v=\"99\"\r\n");
            out.print("Sec-Ch-Ua-Mobile: ?0\r\n");
            out.print("Sec-Ch-Ua-Platform: \"macOS\"\r\n");
            out.print("Upgrade-Insecure-Requests: 1\r\n");
            out.print("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36\r\n");
            out.print("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9\r\n");
            out.print("Service-Worker-Navigation-Preload: true\r\n");
            out.print("Purpose: prefetch\r\n");
            out.print("X-Client-Data: CLSEywE=\r\n");
            out.print("Sec-Fetch-Site: none\r\n");
            out.print("Sec-Fetch-Mode: navigate\r\n");
            out.print("Sec-Fetch-User: ?1\r\n");
            out.print("Sec-Fetch-Dest: document\r\n");
                //out.print("Accept-Encoding: gzip\r\n");//deflate
            out.print("Accept-Language: tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7\r\n");
            out.print("Connection: close\r\n");
            out.print("\r\n");
            out.flush();

            /*
             * Make sure there were no surprises
             */
            if (out.checkError())
                System.out.println(
                        "SSLSocketClient:  java.io.PrintWriter error");

            /* read response */
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));

            HttpRequestParser htr = new HttpRequestParser();

            String inputLine;
            String result="";
            while (true){
                inputLine = in.readLine();
                if(inputLine!=null)
                {
                    result+=(inputLine+"\n");
                    System.out.println(inputLine);
                }
                if(inputLine==null){
                    //System.out.println("\n\n\n\n\neof");
                    break;
                }
                if((inputLine!=null&&inputLine.equals(""))){
                    //System.out.println("empty string");
                }

            }

            //htr.parseRequest(result);
            //System.out.println(htr.isHeaderAvailable("Server"));
            //htr.decipherGzip();
            //htr.getHeader("Server");
            //System.out.println(result);
            //htr.setHeader("Server","TEST");
            //System.out.println(htr.getRequest());
            //htr.deleteHeader("Server");
            //System.out.println(htr.getRequest());
            //System.out.println(htr.getHeader("Server"));
            //System.out.println(htr.getMessageBody());
            //System.out.println(htr.getHeaderParam("Server"));

            /*in.close();
            out.close();
            socket.close();*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
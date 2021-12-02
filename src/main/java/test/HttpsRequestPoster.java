package main.java.test;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.brotli.dec.BrotliInputStream;
import org.brotli.wrapper.enc.BrotliOutputStream;

public class HttpsRequestPoster {
    public static void main(String[] args) {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

// Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }

// Now you can access an https URL without having the certificate in the truststore
        try {
            URL url = new URL("https://www.youtube.com");
            //URL url = new URL("http://localhost:9090");
            HttpsURLConnection connection = null;
            //Create connection
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Cookie", "VISITOR_INFO1_LIVE=WOpsqUYLOSc; PREF=f4=4000000&tz=Europe.Istanbul&f6=40000000; GPS=1; YSC=6XAs1YGT4Z8");
            connection.setRequestProperty("Sec-Ch-Ua", "\"Chromium\";v=\"95\", \";Not A Brand\";v=\"99\"");
            connection.setRequestProperty("Sec-Ch-Ua", "\"Chromium\";v=\"95\", \";Not A Brand\";v=\"99\"");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            connection.setRequestProperty("Service-Worker-Navigation-Preload", "true");
            connection.setRequestProperty("X-Client-Data", "CLSEywE=");
            connection.setRequestProperty("Sec-Fetch-Site", "none");
            connection.setRequestProperty("Sec-Fetch-Mode", "navigate");
            connection.setRequestProperty("Sec-Fetch-User", "?1");
            connection.setRequestProperty("Sec-Fetch-Dest", "document");
            connection.setRequestProperty("Accept-Language", "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7");
            connection.setRequestProperty("Connection", "close");
            connection.setRequestProperty("Accept-Encoding"," gzip, deflate");
//            connection.setUseCaches(false);

            connection.setDoOutput(false);

            //Send request
            /*DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.close();*/

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+

            Map<String, List<String>> map=connection.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                System.out.println("[+] " + entry.getKey()
                        + " :  " + entry.getValue());
            }

            String line;
            byte[] buffer= new byte[4096];
            int read=0;
/*
            BufferedReader br = new BufferedReader(new InputStreamReader(new BrotliInputStream(new ByteArrayInputStream(buffer))));
            rd.transferTo(br);
            //BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new BrotliOutputStream(new ByteArrayOutputStream(read))));
            while (bw.) {

            }


            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }

            System.out.println(response.toString().replaceAll("\r",""));
            rd.close();*/
            //new FileWriter("httpsposter.txt").write(response.toString().replaceAll("\r",""));

        } catch (MalformedURLException e) {

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*public static String executeGet(String targetURL, String urlParameters) {
        HttpURLConnection connection = null;

        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

//            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }*/
}
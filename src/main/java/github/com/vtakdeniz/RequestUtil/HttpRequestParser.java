package github.com.vtakdeniz.RequestUtil;

import java.io.*;
import java.util.Hashtable;
import java.util.zip.GZIPInputStream;

public class HttpRequestParser {

    private String _requestLine;
    private Hashtable<String, String> _requestHeaders;
    private StringBuffer _messageBody;

    public HttpRequestParser() {
        _requestHeaders = new Hashtable<String, String>();
        _messageBody = new StringBuffer();
    }

    public void parseRequest(String request) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(request));

        setRequestLine(reader.readLine());
        String header = reader.readLine();
        while (header.length() > 0) {
            appendHeaderParameter(header);
            header = reader.readLine();
        }

        String bodyLine = reader.readLine();
        while (bodyLine != null) {
            appendMessageBody(bodyLine);
            bodyLine = reader.readLine();
        }

    }

    public String getRequestLine() {
        return _requestLine;
    }

    public void setRequestLine(String requestLine)  {
        if (requestLine == null || requestLine.length() == 0) {
            System.out.println("Invalid Request-Line: " + requestLine);
        }
        _requestLine = requestLine;
    }

    public void deleteHeader(String header){
        _requestHeaders.remove(header);
    }

    public void setHeader(String header,String value){
        _requestHeaders.put(header,value);
    }

    public void decipherGzip() throws IOException {
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(_messageBody.toString().getBytes()));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
        }
    }

    public void appendHeaderParameter(String header)  {
        int idx = header.indexOf(":");
        if (idx == -1) {
            System.out.println("Invalid Header Parameter: " + header);
        }
        _requestHeaders.put(header.substring(0, idx), header.substring(idx + 1, header.length()));
    }

    public String getMessageBody() {
        return _messageBody.toString();
    }

    public void appendMessageBody(String bodyLine) {
        _messageBody.append(bodyLine).append("\r\n");
    }


    public boolean isHeaderAvailable(String header){
        return  _requestHeaders.containsKey(header);
    }
    public String getRequest(){
        String result=getRequestLine()+"\n";
        for (String key:_requestHeaders.keySet()) {
            result+=(key+": "+_requestHeaders.get(key)+"\n");
        }
        result+= ("\n"+_messageBody);
        return result;
    }

    public String getHeader(String headerName){
        return _requestHeaders.get(headerName);
    }

    public int getTunnelPort(){
        String host = getHeader("Host");
        String sp[] = host.split(":");
        return Integer.parseInt(sp[1]);
    }

    public String getTunnelUrl(){
        String host = getHeader("Host");
        String sp[] = host.split(":");
        return sp[0].trim();
    }

}
package main.java.test;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientHandler {
  /*  private Socket socket = null;
    private Socket remoteSocket = null;
    private HttpRequestParser request = null;
    ClientHandler(Socket socket)
    {
        this.socket = socket;
        request = new HttpRequestParser();
        request.parseRequest(socket); // I read and parse the HTTP request here
    }

    public void run()
    {

        remoteSocket = new Socket(request.getTunnelUrl(),request.getTunnelPort());

        if(request.isSecure() )
        {
            // send ok message to client
            String ConnectResponse = "HTTP/1.0 200 Connection established\r\n" +
                    "Proxy-agent: ProxyServer/1.0\r\n" +
                    "\r\n\r\n";
            try
            {
                DataOutputStream out =  new DataOutputStream(socket.getOutputStream());
                out.writeByte(ConnectResponse);
                out.flush();
            } catch(Exception e) {}

        }

        // start connecting remoteSocket and clientSocket
    }*/
}

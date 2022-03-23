package github.com.vtakdeniz.RequestUtil;

import java.io.BufferedWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class RequestWrapper {
    public HttpRequestParser parser;
    public BufferedWriter targer_socket;
    public Socket socket;
    public CountDownLatch lock;
    public BlockingQueue local_requests;

    public RequestWrapper(HttpRequestParser _parser, BufferedWriter _target_socket, Socket _socket, CountDownLatch _lock, BlockingQueue _local_requests){
        this.parser=_parser;
        this.targer_socket=_target_socket;
        this.socket=_socket;
        this.lock=_lock;
        this.local_requests=_local_requests;
    }
}

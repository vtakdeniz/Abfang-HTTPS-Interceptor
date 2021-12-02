package main.java.test;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;

public class RequestWrapper {
    public HttpRequestParser parser;
    public BufferedWriter targer_socket;
    public Socket socket;
    public Lock lock;
    public BlockingQueue local_requests;

    public RequestWrapper(HttpRequestParser _parser,BufferedWriter _target_socket,Socket _socket,Lock _lock,BlockingQueue _local_requests){
        this.parser=_parser;
        this.targer_socket=_target_socket;
        this.socket=_socket;
        this.lock=_lock;
        this.local_requests=_local_requests;
    }
}

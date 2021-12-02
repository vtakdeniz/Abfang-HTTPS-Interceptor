package main.java.test;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.net.Socket;

public class RequestWrapper {
    public HttpRequestParser parser;
    public BufferedWriter targer_socket;
    public RequestWrapper(HttpRequestParser _parser,BufferedWriter _target_socket){
        this.parser=_parser;
        this.targer_socket=_target_socket;
    }
}

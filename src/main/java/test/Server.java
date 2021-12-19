package main.java.test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.IOException;

public class Server extends Thread {

    public static void main(String[] args) {
        (new Server()).start();
    }

    public static int ssl_server_port = 9095;

    public Server() {
        super("Server Thread");
    }

    @Override
    public void run() {
        /**
         * TODO  : SERVER class works as initiator for now. Fix it later.
         */
        QueueHandler queueHandler = new QueueHandler();
        queueHandler.start();
        Screen screen = new Screen(ScreenType.STANDARD_OUTPUT);
        screen.start();
        ///////////////////// Initiation Done

        try (ServerSocket serverSocket = new ServerSocket(9090)) {
            Socket socket;
            try {
                while ((socket = serverSocket.accept()) != null) {
                    (new Handler(socket)).start();
                }
            } catch (IOException e) {
                //e.printStackTrace();  // TODO: implement catch
            }
        } catch (IOException e) {
            //e.printStackTrace();  // TODO: implement catch
            return;
        }
    }

    public static class Handler extends Thread {
        public static final Pattern CONNECT_PATTERN = Pattern.compile("CONNECT (.+):(.+) HTTP/(1\\.[01])",
                Pattern.CASE_INSENSITIVE);
        private final Socket clientSocket;
        private boolean previousWasR = false;

        public Handler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
    public void run() {

            try {
                String request = readLine(clientSocket);
                Matcher matcher = CONNECT_PATTERN.matcher(request);
                if (matcher.matches()) {
                    String header;
                    do {
                        header = readLine(clientSocket);
                    } while (!"".equals(header));
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(clientSocket.getOutputStream(),
                            "ISO-8859-1");

                    final Socket forwardSocket;
                    try {
                            ssl_server_port++;
                            new SSLServer(ssl_server_port,matcher.group(1)).start();
                            Thread.sleep(700);
                            forwardSocket = new Socket("127.0.0.1", ssl_server_port);
                         //forwardSocket = new Socket(matcher.group(1), Integer.parseInt(matcher.group(2)));

                    } catch (IOException| InterruptedException | NumberFormatException e) {
                        //e.printStackTrace();  // TODO: implement catch
                        outputStreamWriter.write("HTTP/" + matcher.group(3) + " 502 Bad Gateway\r\n");
                        outputStreamWriter.write("Proxy-agent: Simple/0.1\r\n");
                        outputStreamWriter.write("\r\n");
                        outputStreamWriter.flush();
                        return;
                    }
                    try {
                        outputStreamWriter.write("HTTP/" + matcher.group(3) + " 200 Connection established\r\n");
                        outputStreamWriter.write("Proxy-agent: Simple/0.1\r\n");
                        outputStreamWriter.write("\r\n");
                        outputStreamWriter.flush();

                        Thread remoteToClient = new Thread(() -> forwardData(forwardSocket, clientSocket));
                        remoteToClient.start();
                        try {
                            if (previousWasR) {
                                int read = clientSocket.getInputStream().read();
                                if (read != -1) {
                                    if (read != '\n') {
                                        forwardSocket.getOutputStream().write(read);
                                    }
                                    forwardData(clientSocket, forwardSocket);
                                } else {
                                    if (!forwardSocket.isOutputShutdown()) {
                                        forwardSocket.shutdownOutput();
                                    }
                                    if (!clientSocket.isInputShutdown()) {
                                        clientSocket.shutdownInput();
                                    }
                                }
                            } else {
                                forwardData(clientSocket, forwardSocket);
                            }
                        } finally {
                            try {
                                remoteToClient.join();
                            } catch (InterruptedException e) {
                                //e.printStackTrace();  // TODO: implement catch
                            }
                        }
                    } finally {
                        forwardSocket.close();
                    }
                }
            } catch (IOException e) {
                //e.printStackTrace();  // TODO: implement catch
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    //e.printStackTrace();  // TODO: implement catch
                }
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
                //e.printStackTrace();  // TODO: implement catch
            }
        }

        private String readLine(Socket socket) throws IOException {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int next;
            readerLoop:
            while ((next = socket.getInputStream().read()) != -1) {
                if (previousWasR && next == '\n') {
                    previousWasR = false;
                    continue;
                }
                previousWasR = false;
                switch (next) {
                    case '\r':
                        previousWasR = true;
                        break readerLoop;
                    case '\n':
                        break readerLoop;
                    default:
                        byteArrayOutputStream.write(next);
                        break;
                }
            }
            return byteArrayOutputStream.toString("ISO-8859-1");
        }
    }
}

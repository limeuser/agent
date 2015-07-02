package cn.oasistech.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class AsynClient {
    private InetSocketAddress serverAddress;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private ClientHandler handler;
    Thread readerThread;
    private byte[] recvBuffer = new byte[2048];
    private final static Logger logger = new Logger().addPrinter(System.out);
    
    public AsynClient(ClientHandler handler) {
        this.handler = handler;
    }
    
    public boolean start(InetSocketAddress server) {
        if (socket != null) {
            logger.log("started");
            return true;
        }
        
        this.serverAddress = server;
        
        if (connect() == false) {
            return false;
        }
        
        readerThread = new Thread(new Reader());
        readerThread.start();
        
        return true;
    }
    
    public void stop() {
        disconnect();
        readerThread.stop();
    }
    
    private boolean connect() {
        if (socket != null) {
            return true;
        }
        
        try {
            this.socket = new Socket();
            this.socket.connect(serverAddress);
            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();
            logger.log("start asyn client, connect server:%s", serverAddress.toString());
            return true;
        } catch (IOException e) {
            this.socket = null;
            logger.log("connect server exception:", e);
            return false;
        }
    }
    
    private void disconnect() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e1) {
            logger.log("close connection exception:", e1);
        } finally {
            socket = null;
        }
    }
    
    private void reconnect() {
        disconnect();
        
        // 重连,3s重连一次
        while (true) {
            if (connect()) {
                break;
            }
            try {
                Thread.sleep(3 * 1000);
            } catch (InterruptedException e1) { }
        }
    }
    
    public boolean send(byte[] data) {
        if (socket == null) {
            return false;
        }
        
        try {
            out.write(data);
            return true;
        } catch (SocketException e) {
            logger.log("socket exception when sending, reconnect ...", e);
            reconnect();
            return false;
        } catch (IOException e) {
            logger.log("sending exception:", e);
            return false;
        }
    }
    
    public class Reader implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    int length = in.read(recvBuffer);
                    if (length > 0) {
                        handler.handle(recvBuffer, 0, length);
                    }
                } catch (SocketException e) {
                    logger.log("socket exception when receiving, reconnect...", e);
                    reconnect();
                } catch (IOException e) {
                    logger.log("recv data exception", e);
                }
            }
        }
    }
}

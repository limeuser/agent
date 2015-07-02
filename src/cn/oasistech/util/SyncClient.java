package cn.oasistech.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class SyncClient {
    private InetSocketAddress serverAddress;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private final Logger logger = new Logger().addPrinter(System.out);
    
    public boolean start(InetSocketAddress server) {
        if (socket != null) {
            logger.log("started");
            return true;
        }
        
        this.serverAddress = server;
        return connect();
    }
    
    public void stop() {
        disconnect();
    }
    
    public boolean send(byte[] data) {
        if (socket == null) {
            logger.log("disconnecting");
            return false;
        }
        
        try {
            out.write(data);
            return true;
        } catch (SocketException e) {
            logger.log("socket exception when sending, reconnect...", e);
            reconnect();
            logger.log("reconnect success");
            return false;
        } catch (IOException e) {
            logger.log("send exception:", e);
            return false;
        }
    }

    public int recv(byte[] data) {
        if (socket == null) {
            logger.log("disconnecting");
            return 0;
        }
        
        try {
            return in.read(data);
        } catch (SocketException e) {
            logger.log("socket exception when receiving, reconnect...", e);
            reconnect();
            return 0;
        } catch (IOException e) {
            logger.log("receive exception:", e);
            return 0;
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
    
    private boolean connect() {
        if (socket != null) {
            return true;
        }
        
        try {
            this.socket = new Socket();
            this.socket.connect(serverAddress);
            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();
            logger.log("start sync client, connect server:%s", serverAddress.toString());
            return true;
        } catch (IOException e) {
            this.socket = null;
            logger.log("connect server exception:", e);
            return false;
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
}

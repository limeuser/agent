package cn.oasistech.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

// socket client reconnect server when disconnected
public class SocketClient {
    private InetSocketAddress serverAddress;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private final Logger logger = new Logger().addPrinter(System.out);
    
    public boolean connect(Address server) {
        if (socket != null) {
            logger.log("socket client has connected server:%s", serverAddress.toString());
            return true;
        }
        
        this.serverAddress = server.toSocketAddress();
        if (this.serverAddress == null) {
            return false;
        }
        
        return connect();
    }
    
    public boolean reconnect(Address server) {
        disconnect();
        return connect(server);
    }
    
    public boolean send(byte[] data) {
        if (socket == null) {
            logger.log("socket client disconnected");
            return false;
        }
        
        try {
            out.write(data);
            return true;
        } catch (SocketException e) {
            logger.log("socket exception when sending, reconnect ...", e);
            reconnect();
            logger.log("reconnect success");
            return false;
        } catch (IOException e) {
            logger.log("send data exception:", e);
            return false;
        }
    }
    
    public int recv(byte[] buffer) {
        try {
            return in.read(buffer);
        } catch (SocketException e) {
            logger.log("socket exception when receiving, reconnect...", e);
            reconnect();
            logger.log("reconnect success");
            return 0;
        } catch (IOException e) {
            logger.log("recv data exception", e);
            return 0;
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
            logger.log("socket client connect server:%s", serverAddress.toString());
            return true;
        } catch (IOException e) {
            this.socket = null;
            logger.log("socket client connect server exception:", e);
            return false;
        }
    }
    
    public void disconnect() {
        if (socket == null) {
            return;
        }
        
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
}

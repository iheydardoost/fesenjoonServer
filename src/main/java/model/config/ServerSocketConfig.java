package model.config;

import java.net.InetAddress;

public class ServerSocketConfig {
    private InetAddress ipAddress;
    private int port;

    public ServerSocketConfig() {
    }

    public ServerSocketConfig(InetAddress ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

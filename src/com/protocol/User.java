package com.protocol;

/**
 * C-C
 * com.protocol
 *
 * @author GOLD
 * @date 2019/4/4
 */
public class User {

    private String name;
    private String ip;
    private int port;
    private int vport;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getVport() {
        return vport;
    }

    public void setVport(int vport) {
        this.vport = vport;
    }
}

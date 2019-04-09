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
    private String status;

    public String unique() {
        if (name == null && ip == null) {
            return null;
        }
        String s = name + "," + ip + "," + String.valueOf(port);
        return s;
    }

    @Override
    public String toString() {
        String s = name + ":" + port + "    [" + status + "]";
        return s;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}

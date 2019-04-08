package com.protocol;

/**
 * C-C
 * com.protocol
 *
 * @author GOLD
 * @date 2019/4/8
 */
public class Message {

    private User des;
    private User origin;
    private String message;

    public Message(User des, User origin, String message) {
        this.des = des;
        this.origin = origin;
        this.message = message;
    }

    @Override
    public String toString() {
        String s1 = des.toString() + "\n";
        String s2 = origin.getName() + ":" + origin.getPort() + " Message:\n";
        String s3 = message + "\n";
        return s1 + s2 + s3;
    }

}

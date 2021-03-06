package me.krickl.memebotj.Utility;

import me.krickl.memebotj.UserHandler;

/**
 * This file is part of memebotj.
 * Created by unlink on 07/04/16.
 */
public class MessagePackage {
    public String[] messageContent = null;
    public UserHandler sender = null;
    public String messageType = null;
    public String channel = null;

    public MessagePackage(String[] content, UserHandler uh, String type, String channel) {
        messageContent = content;
        sender = uh;
        messageType = type;
        this.channel = channel;
    }
}

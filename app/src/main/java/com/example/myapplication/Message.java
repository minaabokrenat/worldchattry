package com.example.myapplication;

import com.google.firebase.database.ServerValue;

public  class Message {
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_IMAGE = 2;

    private String message;
    private String senderID;
    private String recieveID;
    private int MessageType;
    private Object timestamp;

    public Message() {
    }


    public Message(String message, String senderID, String recieveID, int messageType) {
        this.message = message;
        this.senderID = senderID;
        this.recieveID = recieveID;
        MessageType = messageType;
        timestamp = ServerValue.TIMESTAMP;

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getRecieveID() {
        return recieveID;
    }

    public void setRecieveID(String recieveID) {
        this.recieveID = recieveID;
    }

    public int getMessageType() {
        return MessageType;
    }

    public void setMessageType(int messageType) {
        MessageType = messageType;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", senderID='" + senderID + '\'' +
                ", recieveID='" + recieveID + '\'' +
                ", MessageType=" + MessageType +
                ", timestamp=" + timestamp +
                '}';
    }
}

package com.example.myapplication.Infra;

import java.io.Serializable;

public class NodeInfo implements Serializable {
    private static final long serialVersionUTD = 52352L;

    private int id;
    private String ip;
    private int port;
    private int firstTopic;
    private int lastTopic;

    public NodeInfo(){}

    public NodeInfo(String ip, int port){
        this.ip=ip;
        this.port = port;
    }

    public NodeInfo(int id, String ip, int port){
        this.id =id;
        this.ip = ip;
        this.port = port;
    }

    public NodeInfo(int id, String ip, int port, int first, int last){
        this.id =id;
        this.ip = ip;
        this.port = port;
        firstTopic=first;
        lastTopic = last;
    }

    public int getID(){return id;}
    public void setID(int id){this.id = id;}

    public String getIP(){return ip;}
    public void setIP(String ip){this.ip = ip;}

    public int getPort(){return port;}
    public void setPort(int port){this.port = port;}

    public int getFirst(){return firstTopic;}
    public void setFirst(int first){firstTopic = first;}
    public int getLast(){return lastTopic;}
    public void setLast(int last){lastTopic = last;}


    public String toString(){
        return "IP: " + ip + "\nPort: " + port;
    }
}

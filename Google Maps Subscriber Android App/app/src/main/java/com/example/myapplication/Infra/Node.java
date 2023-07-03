package com.example.myapplication.Infra;

import java.util.ArrayList;

public interface  Node {
    NodeInfo info = new NodeInfo();

    ArrayList<NodeInfo> b = new ArrayList();

    void init(int a);

    void connect();

    void disconnect();

}

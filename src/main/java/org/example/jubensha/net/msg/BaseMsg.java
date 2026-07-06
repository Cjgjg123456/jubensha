package org.example.jubensha.net.msg;

import java.io.Serializable;
import java.net.Socket;

public abstract class BaseMsg implements Serializable {
    protected Socket client;
    protected String type;
    protected long timestamp;

    public BaseMsg(String type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public Socket getClient() {
        return client;
    }

    public void setClient(Socket client) {
        this.client = client;
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public abstract void doBiz();
}
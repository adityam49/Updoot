package com.ducktapedapps.updoot.model;

import java.io.Serializable;

public class Thing implements Serializable {
    private String kind;
    private Data data;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Thing{" +
                "kind='" + kind + '\'' +
                ", Data=" + data +
                '}';
    }
}

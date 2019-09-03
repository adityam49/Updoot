package com.ducktapedapps.updoot.model;

import java.io.Serializable;

public class thing implements Serializable {
    private String kind;
    private data data;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public data getData() {
        return data;
    }

    public void setData(data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "thing{" +
                "kind='" + kind + '\'' +
                ", data=" + data +
                '}';
    }
}

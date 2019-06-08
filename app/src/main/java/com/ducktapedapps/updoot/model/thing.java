package com.ducktapedapps.updoot.model;

public class thing {
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

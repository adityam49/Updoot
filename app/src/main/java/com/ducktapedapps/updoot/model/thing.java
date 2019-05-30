package com.ducktapedapps.updoot.model;

public class thing<T> {
    private String kind;
    private T data;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
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

package com.ducktapedapps.updoot.utils;

//event wrapper used to consume events such as Toast messages only once, not using this will cause a Toast message on config change
public class SingleEventPublisher<T> {
    private T content;
    private boolean isHandled;

    public SingleEventPublisher(T content) {
        this.content = content;
        this.isHandled = false;
    }

    public T getContentIfNotHandled() {
        if (!isHandled) {
            isHandled = true;
            return content;
        } else {
            return null;
        }
    }
}

package com.ducktapedapps.updoot.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class images implements Serializable {

    private source source;

    @NonNull
    @Override
    public String toString() {
        return source.getUrl();
    }

    class source implements Serializable {
        private String url;
        private int height;
        private int width;

        public String getUrl() {
            if (url != null && url.contains("&amp;s")) {
                url = url.replace("&amp;s", "&s");
            }
            return url;
        }
    }
}

package com.ducktapedapps.updoot.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

public class Preview implements Serializable {
    private List<Images> images;

    public List<Images> getImages() {
        return images;
    }

    @NonNull
    @Override
    public String toString() {
        if (images != null && images.get(0) != null) {
            return images.get(0).toString();
        }
        return super.toString();
    }
}

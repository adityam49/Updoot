package com.ducktapedapps.updoot.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

public class preview implements Serializable {
    private List<images> images;

    public List<com.ducktapedapps.updoot.model.images> getImages() {
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

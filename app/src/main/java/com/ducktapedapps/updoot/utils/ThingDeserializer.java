package com.ducktapedapps.updoot.utils;

import com.ducktapedapps.updoot.model.CommentData;
import com.ducktapedapps.updoot.model.LinkData;
import com.ducktapedapps.updoot.model.ListingData;
import com.ducktapedapps.updoot.model.Thing;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;


public class ThingDeserializer implements JsonDeserializer<Thing> {
    private static final String TAG = "ThingDeserializer";

    @Override
    public Thing deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = null;
        if (json instanceof JsonObject) {
            jsonObject = json.getAsJsonObject();
        } else if (json instanceof JsonArray) {
            jsonObject = json.getAsJsonArray().get(1).getAsJsonObject();
        }
        Thing thing = new Thing();
        if (jsonObject != null) {
            String kind = jsonObject.get("kind").getAsString();
            if (kind != null) {
                JsonElement element = jsonObject.get("data");
                switch (kind) {
                    case "Listing":
                        thing.setData(context.deserialize(element, ListingData.class));
                        thing.setKind("Listing");
                        break;
                    case "t3":
                        thing.setData(context.deserialize(element, LinkData.class));
                        thing.setKind("t3");
                        break;
                    case "t1":
                        thing.setData(context.deserialize(element, CommentData.class));
                        thing.setKind("t1");
                        break;
                }
            }
        }
        return thing;
    }
}

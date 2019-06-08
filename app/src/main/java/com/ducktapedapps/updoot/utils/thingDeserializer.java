package com.ducktapedapps.updoot.utils;

import com.ducktapedapps.updoot.model.LinkData;
import com.ducktapedapps.updoot.model.ListingData;
import com.ducktapedapps.updoot.model.thing;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;


public class thingDeserializer implements JsonDeserializer<thing> {
    private static final String TAG = "thingDeserializer";

    @Override
    public thing deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String kind = jsonObject.get("kind").getAsString();
        thing thing = new thing();
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
            }
        }
        return thing;
    }
}

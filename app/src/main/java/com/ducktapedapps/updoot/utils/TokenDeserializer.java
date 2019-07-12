package com.ducktapedapps.updoot.utils;

import com.ducktapedapps.updoot.model.Token;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class TokenDeserializer implements JsonDeserializer<Token> {

    private static final String TAG = "TokenDeserializer";

    @Override
    public Token deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        JsonElement access_token_jsonElement = jsonObject.get("access_token");
        if (access_token_jsonElement != null) {
            JsonElement refresh_token_jsonElement = jsonObject.get("refresh_token");
            if (refresh_token_jsonElement != null) {
                return new Token(access_token_jsonElement.getAsString(), refresh_token_jsonElement.getAsString(), jsonObject.get("expires_in").getAsLong() + System.currentTimeMillis() / 1000);
            } else {
                return new Token(access_token_jsonElement.getAsString(), jsonObject.get("expires_in").getAsLong() + System.currentTimeMillis() / 1000);
            }
        } else {
            throw new JsonParseException("access token json element is null");
        }
    }
}

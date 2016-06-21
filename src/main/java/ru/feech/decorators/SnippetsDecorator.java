package ru.feech.decorators;

import org.bson.Document;
import org.bson.types.Binary;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Kirill on 6/16/2016.
 */
public class SnippetsDecorator {

    public static Map<String, Object> index(Document document)
    {
        Map<String, Object> result = new TreeMap<>();
        result.put("id", document.getObjectId("_id").toHexString());
        result.put("text", document.get("snippet", Document.class).getString("text"));
        result.put("num", document.get("snippet", Document.class).getInteger("num"));
        result.put("from", document.get("snippet", Document.class).getDouble("from"));
        result.put("to", document.get("snippet", Document.class).getDouble("to"));
        result.put("story_id", document.get("snippet", Document.class).getObjectId("story_id").toHexString());

        return  result;
    }

    public static Map<String, Object> show(Document document)
    {
        Map<String, Object> result = index(document);
        result.put("mp3", document.get("snippet", Document.class).get("mp3", Binary.class).getData());
        return  result;
    }
}

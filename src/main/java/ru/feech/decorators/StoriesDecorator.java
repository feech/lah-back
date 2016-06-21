package ru.feech.decorators;

import org.bson.Document;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Kirill on 6/16/2016.
 */
public class StoriesDecorator {
    public static Map<String, Object> index(Document document)
    {
        Map<String, Object> result = new TreeMap<>();
        result.put("id", document.getObjectId("_id").toHexString());
        result.put("title", document.get("story", Document.class).getString("title"));

        return  result;
    }

    public static Map<String, Object> show(Document document)
    {
        Map<String, Object> result = new TreeMap<>();
        result.put("id", document.getObjectId("_id").toHexString());
        result.put("title", document.get("story", Document.class).getString("title"));
        result.put("lang", document.get("story", Document.class).getString("lang"));
        result.put("file_id", document.get("story", Document.class).getObjectId("file_id").toHexString());

        return  result;
    }
}

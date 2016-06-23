package ru.feech.controllers.front;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.feech.decorators.SnippetsDecorator;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Kirill on 6/16/2016.
 */
@RestController("FrontSnippetsCtrl")
@RequestMapping("/front/snippets")
public class SnippetsCtrl {

    @Autowired
    MongoDatabase database;

    // snippets by included words
    @RequestMapping(method = RequestMethod.GET)
    public Map<String, Object> search_snippets(@RequestParam(value = "words", required = false, defaultValue = "") String words,
                                               @RequestParam(value = "page", defaultValue = "0") Long _page) {

        if (words.isEmpty()) {
            throw new RuntimeException("pattern are required");
        }

        Long count = database.getCollection("snippets")
                .count(new Document("$text",
                        new Document("$search", words)));

        Long last_page = (count + 24) / 25 - 1;
        Long page = _page;
        if (page > last_page) {
            page = last_page;
        }

        Map<String, Object> result = new TreeMap<>();
        Iterable<Map<String, Object>> snippets = database.getCollection("snippets")
                .find(new Document("$text",
                        new Document("$search", words)))
                .skip((int) (page*25))
                .limit(25)
                .map(SnippetsDecorator::index);

        result.put("pages", last_page + 1);
        result.put("page", page);
        result.put("snippets", snippets);
        return result;
    }

    // snippet's info
    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    public Map<String, Object> get_sound(@PathVariable("id") String id) {
        return database.getCollection("snippets")
                .find(new Document("_id",
                        new ObjectId(id)))
                .map(SnippetsDecorator::show)
                .first();
    }

    // snippet's sound
    @RequestMapping(method = RequestMethod.GET, path = "/{id}/sound", produces = "audio/mpeg")
    public byte[] get_soundb(@PathVariable("id") String id) {
        return database.getCollection("snippets")
                .find(new Document("_id",
                        new ObjectId(id)))
                .map(d -> d.get("snippet", Document.class)
                        .get("mp3", Binary.class))
                .first().getData();
    }
}

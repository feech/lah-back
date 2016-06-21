package ru.feech.controllers.front;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.feech.decorators.SnippetsDecorator;

import java.util.Map;

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
    public Iterable<Map<String, Object>> search_snippets(@RequestParam(value = "words", required = false, defaultValue = "") String words,
                                                         @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                         @RequestParam(value = "story_id", required = false, defaultValue = "") String story_id) {

        if(words.isEmpty() && story_id.isEmpty())
        {
            throw new RuntimeException("story or pattern are required");
        }
        if (!story_id.isEmpty())
        {
            return database.getCollection("snippets")
                    .find(new Document("snippet.story_id", new ObjectId(story_id)))
                    .sort(new Document("snippet.num", 1))
                    .limit(25)
                    .map(SnippetsDecorator::index);
        }

        return database.getCollection("snippets")
                .find(new Document("$text",
                        new Document("$search", words)))
                .limit(25)
                .map(SnippetsDecorator::index);
    }

    // snippet's sound
    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    public Map<String, Object> get_sound(@PathVariable("id") String id) {
        return database.getCollection("snippets")
                .find(new Document("_id",
                        new ObjectId(id)))
                .map(SnippetsDecorator::show)
                .first();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/b/{id}", produces = "audio/mpeg")
    public byte[] get_soundb(@PathVariable("id") String id) {
        return database.getCollection("snippets")
                .find(new Document("_id",
                        new ObjectId(id)))
                .map(d-> d.get("snippet", Document.class)
                        .get("mp3", Binary.class))
                .first().getData();
    }
}

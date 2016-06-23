package ru.feech.controllers.front;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.feech.decorators.SnippetsDecorator;
import ru.feech.decorators.StoriesDecorator;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;
import static com.mongodb.client.model.Sorts.ascending;


/**
 * Created by Kirill on 6/16/2016.
 */
@RestController("FrontStoriesCtrl")
@RequestMapping("/front/stories")
public class StoriesCtrl {
    private final static Logger logger = Logger.getLogger(StoriesCtrl.class.getName());

    @Autowired
    MongoDatabase database;

    @Autowired
    GridFSUploadOptions gridFSUploadOptions;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${rabbit.queue_on_split}")
    String queue_on_split;

    final static byte[] bytes = {1, 2, 3};

    @RequestMapping("/test")
    public byte[] testget() {
        return bytes;
    }

    /**
     * recommended stories
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public Iterable<Map<String, Object>> get(@RequestParam(name = "page", required = false) Integer page,
                                             @RequestParam(name = "pattern", required = false) String pattern) {

        FindIterable<Document> stories;

        if (pattern != null && !pattern.isEmpty()) {
            stories = database.getCollection("stories")
                    .find(regex("story.title",
                            String.format("%s", pattern)));
        } else {
            stories = database.getCollection("stories")
                    .find();

        }
        if (page != null && page > 0) {
            stories = stories.skip(page * 25);
        }
        return stories
                // @todo - filter not filled stories
                .limit(25)
                .map(StoriesDecorator::index);
    }

    // story's params
    @RequestMapping(method = RequestMethod.GET, path = "/{story_id}")
    public Map<String, Object> story(@PathVariable("story_id") String story_id) {
        return database.getCollection("stories")
                .find(new Document("_id", new ObjectId(story_id)))
                .map(StoriesDecorator::show)
                .first();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{story_id}/sound", produces = "audio/mpeg")
    public void story_sound(HttpServletResponse response,
                            @PathVariable("story_id") String story_id) throws IOException {
        ObjectId mp3_id = database.getCollection("stories")
                .find(new Document("_id", new ObjectId(story_id)))
                .map(d -> d.get("story", Document.class)
                        .getObjectId("mp3_id"))
                .first();

        gridFSBucket.downloadToStream(mp3_id, response.getOutputStream());

        response.setContentType("audio/mpeg");
    }


    // snippet for story
    // on given page [0..)
    @RequestMapping(method = RequestMethod.GET, path = "/{story_id}/snippets")
    public Map<String, Object> story_snippet(@PathVariable("story_id") String story_id,
                                                       @RequestParam(value = "page", required = false, defaultValue = "0") Long _page) {
        Long count = database.getCollection("snippets")
                .count(new Document("snippet.story_id", new ObjectId(story_id)));

        Long last_page = (count+24)/25-1;
        Long page = _page;
        if(page>last_page)
        {
            page=last_page;
        }

        Map<String, Object> result = new TreeMap<>();
        Iterable<Map<String, Object>> snippets = database.getCollection("snippets")
                .find(eq("snippet.story_id", new ObjectId(story_id)))
                .sort(ascending("snippet.num"))
                .skip((int) (page*25))
                .limit(25)
                .map(SnippetsDecorator::index);

        result.put("pages", last_page+1);
        result.put("page", page);
        result.put("snippets", snippets);
        return result;
    }


    @RequestMapping(method = RequestMethod.POST)
    public String post(@RequestParam("name") String name,
                       @RequestParam("file") MultipartFile file,
                       @RequestParam("subtitles") MultipartFile file_text) throws IOException {
        System.out.printf("%s %s %d %d %n", name, file.getOriginalFilename(), (int) file.getSize(), (int) file_text.getSize());

        ObjectId fileId = gridFSBucket.uploadFromStream(
                file.getOriginalFilename(),
                file.getInputStream(),
                gridFSUploadOptions);


        Document story = new Document("story",
                new Document()
                        .append("lang", "english")
                        .append("title", name)
                        .append("file_id", fileId)
                        .append("subtitles", file_text.getBytes()));
        database.getCollection("stories")
                .insertOne(story);

        System.out.printf("created %s %s %n", story.getObjectId("_id").toHexString(), fileId.toHexString());
        rabbitTemplate.convertAndSend(queue_on_split, story.get("_id", ObjectId.class).toHexString());

        return "";
    }


}

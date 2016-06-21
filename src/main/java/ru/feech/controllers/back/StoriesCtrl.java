package ru.feech.controllers.back;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by Kirill on 6/9/2016.
 * <p>
 * access to data
 */

@RestController("BackStoriesCtrl")
@RequestMapping("/back/stories")
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


    @RequestMapping(method = RequestMethod.GET)
    public String get() {
        return "hi there";
    }


    @RequestMapping(method = RequestMethod.GET, path = "/story_file", produces = "audio/mpeg")
    public byte[] getFile(@RequestParam("file_id") String id) throws ServletException {

        GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(new ObjectId(id));
        int fileLength = (int) downloadStream.getGridFSFile().getLength();
        byte[] bytesToWriteTo = new byte[fileLength];
        int read = downloadStream.read(bytesToWriteTo);
        downloadStream.close();

        if (read < fileLength) {
            logger.warning("downloaded from mongo less bites then expected");
        }

        return bytesToWriteTo;

    }

    // download original media
    @RequestMapping(method = RequestMethod.GET, path = "/{story_id}/file")
    public void getStoryFile(HttpServletResponse response,
                             @PathVariable("story_id") String story_id) throws ServletException, IOException {

        ObjectId file_id = database.getCollection("stories")
                .find(new Document("_id", new ObjectId(story_id)))
                .first()
                .get("story", Document.class)
                .getObjectId("file_id");

        gridFSBucket.downloadToStream(file_id, response.getOutputStream());

        // Set the content type and attachment header.
//        response.addHeader("Content-disposition", "attachment;filename=myfilename.txt");
        response.setContentType("audio/mpeg");

    }

    // upload processed media
    @RequestMapping(method = RequestMethod.POST, path = "/file")
    public String setStoryFile(@RequestParam("story_id") String id,
                               @RequestParam("file") MultipartFile file) throws IOException {

        ObjectId fileId = gridFSBucket.uploadFromStream(
                file.getOriginalFilename(),
                file.getInputStream(),
                gridFSUploadOptions);


        database.getCollection("stories")
                .updateOne(new Document("_id", new ObjectId(id)),
                        new Document("$set",
                                new Document("story.mp3_id", fileId)));
        return "";
    }

    // obtain original subtitles
    @RequestMapping(method = RequestMethod.GET, path = "/subtitles")
    public byte[] getStorySubtitles(@RequestParam("story_id") String id) throws ServletException {

        return database.getCollection("stories")
                .find(new Document("_id", new ObjectId(id)))
                .first()
                .get("story", Document.class)
                .get("subtitles", Binary.class).getData();

    }

    @RequestMapping(method = RequestMethod.POST, path = "/snippet")
    public String keepSnippets(@RequestParam(value = "story_id") String story_id,
                               @RequestParam(value = "num", required = false) Integer number,
                               @RequestParam(value = "from") Double from,
                               @RequestParam(value = "to") Double to,
                               @RequestParam(value = "file") MultipartFile file,
                               @RequestParam(value = "text") String text
    ) throws IOException {
        if (from > to) {
            logger.warning("from gt to");
        }
        Document snippet = new Document("snippet",
                new Document()
                        .append("story_id", new ObjectId(story_id))
                        .append("num", number)
                        .append("from", from)
                        .append("to", to)
                        .append("text", text)
                        .append("mp3", file.getBytes()));
        database.getCollection("snippets")
                .insertOne(snippet);
        return "";
    }
}

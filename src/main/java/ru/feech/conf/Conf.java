package ru.feech.conf;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import org.bson.Document;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Kirill on 6/8/2016.
 */
@Configuration
public class Conf {

    @Bean
    public MongoDatabase database() {
        MongoClient mongoClient = new MongoClient("172.17.0.3");
        return mongoClient.getDatabase("fluency");
    }

    @Bean
    public GridFSUploadOptions gridFSUploadOptions() {
        // Create some custom options
        return new GridFSUploadOptions()
                .chunkSizeBytes(524288)
                .metadata(new Document("type", "presentation"));
    }

    @Bean
    public GridFSBucket gridFSBucket(MongoDatabase database) {
        return GridFSBuckets.create(database);
    }
}

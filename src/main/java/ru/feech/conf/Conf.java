package ru.feech.conf;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Kirill on 6/8/2016.
 */
@Configuration
public class Conf {

    @Value("${mongo.host}")
    String host;

    @Value("${mongo.database}")
    String database_name;

    @Bean
    public MongoDatabase database() {
        MongoClient mongoClient = new MongoClient(host);
        return mongoClient.getDatabase(database_name);
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

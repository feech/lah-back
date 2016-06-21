package ru.feech;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.io.PrintStream;
import java.util.function.Consumer;

/**
 * Created by Kirill on 6/5/2016.
 */
//@Service
public class BdWorker implements CommandLineRunner {

    @Autowired
    MongoDatabase database;
    @Override
    public void run(String... strings) throws Exception {
        PrintStream printf = System.out.printf("111 %n");

        database.getCollection("restaurants")
                .find(new Document("address.building", "6909"))
                .forEach((Consumer<Document>) document -> System.out.println(document.get("name")));

//        db.getCollection("enso").insertOne(
//                new Document("line",
//                        new Document()
//                        .append("data", "12")
//                )
//        );

    }
}

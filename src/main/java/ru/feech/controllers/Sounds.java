package ru.feech.controllers;

import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Kirill on 6/6/2016.
 */

@Controller
public class Sounds {

    @Autowired
    MongoDatabase database;

    @RequestMapping(method = RequestMethod.GET, value = "/")
    public String provideUploadInfo(Model model) {
//        File rootFolder = new File(Application.ROOT);
//        List<String> fileNames = Arrays.stream(rootFolder.listFiles())
//                .map(f -> f.getName())
//                .collect(Collectors.toList());
//
//        model.addAttribute("files",
//                Arrays.stream(rootFolder.listFiles())
//                        .sorted(Comparator.comparingLong(f -> -1 * f.lastModified()))
//                        .map(f -> f.getName())
//                        .collect(Collectors.toList())
//        );

        return "uploadForm";
    }

//    @RequestMapping(path = "/x",
//            method = {RequestMethod.GET},
//            produces = "audio/mpeg"
//    )
//    public String get()
//    {
//        return String.format("data%n");
//    }

    @RequestMapping(path = "/mp3",
            method = {RequestMethod.GET},
            produces = "audio/mpeg"

    )
    public InputStreamResource get_mp3()
    {
        InputStreamResource is = new InputStreamResource(new ByteArrayInputStream("hey hey\n".getBytes()));
        return is;
    }

    @RequestMapping(path = "/",
            method = {RequestMethod.POST}

    )
    public String post_mp3(@RequestParam("name") String name,
                         @RequestParam("file") MultipartFile file) throws IOException {
        System.out.println("+++");
        System.out.printf("%s %n", name);

        int len =0 ;
        InputStream is = new BufferedInputStream(file.getInputStream());
        while (true)
        {
            int read = is.read();
            if(read==-1)
            {
                break;
            }
            len++;
        }
        System.out.printf("obtained %d %n", len);
//        database.getCollection("mp3").insertOne(
//                new Document("sentence",
//                        new Document()
//                        .append("name", name)
//                        .append("data", file)
//                )
//        );
//        InputStreamResource is = new InputStreamResource(new ByteArrayInputStream("hey hey\n".getBytes()));
        return "redirect:/";
    }
//    @RequestMapping(path = "/x", method = {RequestMethod.POST})
//    public Void post()
//    {
//        return null;
////        return Voi;
//    }


}

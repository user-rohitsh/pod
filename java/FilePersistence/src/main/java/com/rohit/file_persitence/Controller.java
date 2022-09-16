package com.rohit.file_persitence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

@RestController
public class Controller {

    private final OutputStream file_stream;
    MyProperties properties;

    @Autowired
    public Controller(MyProperties properties) throws IOException {

        this.properties = properties;
        Path path = Paths.get(properties.getFile_name());
        file_stream = Files.newOutputStream(path, CREATE, APPEND);
    }

    @PostMapping(value = "/save",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<String> saveToFile(@RequestBody String data) {
        try {
            System.out.println("Received data -----" + data);
            file_stream.write(data.getBytes(StandardCharsets.UTF_8));
            return new ResponseEntity<String>("Saved successfully", HttpStatus.CREATED);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return new ResponseEntity<String>("Error while saving", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

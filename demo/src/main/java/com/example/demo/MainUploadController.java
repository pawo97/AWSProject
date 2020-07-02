package com.example.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

@CrossOrigin(maxAge = 3600, origins = "*")
@Controller
public class MainUploadController {

    // Nazwa kubelka w AWS
    private String bucketName = "dataproject23";

    // Strona startowa z lista plikow w kubelku
    @GetMapping("/")
    public ResponseEntity<?> getUploadMainPage(@RequestParam(required = false, name = "myFile") File name, Model model) {

        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                                           .withRegion(Regions.US_EAST_1)
                                           .build();

        ListObjectsV2Result result = s3.listObjectsV2(bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        ArrayList<Image> images = new ArrayList<>();
        int i = 0;
        for (S3ObjectSummary os : objects) {
            // System.out.println("* " + os.getKey());
            images.add(new Image(i++, os.getKey()));
        }

        // model.addAttribute("images", images);
        System.out.println("Wyswietlono");
        return new ResponseEntity<>(images, HttpStatus.OK);
    }

    // Request do pobrania danego pliku

    @GetMapping("/d/{filename}")
    public ResponseEntity<?> download(@PathVariable String filename) {

        String nameOfFileToDownload = filename;
        System.out.format("Downloading %s from S3 bucket %s...\n", nameOfFileToDownload, bucketName);
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                                                 .withRegion(Regions.US_EAST_1)
                                                 .build();
        try {
            S3Object o = s3.getObject(bucketName, nameOfFileToDownload);
            S3ObjectInputStream s3is = o.getObjectContent();
            FileOutputStream fos = new FileOutputStream(new File(nameOfFileToDownload));
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }
            s3is.close();
            fos.close();
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return new ResponseEntity<>(nameOfFileToDownload, HttpStatus.OK);
    }

    // Request do przeslania danego pliku o danym podanej nazwie podanej w inpucie
    @RequestMapping(method = RequestMethod.POST, value = "/upload")
    public String uplodnPlease(@RequestParam("name") String name, @RequestParam("file") MultipartFile file,
            RedirectAttributes reAttributes) {

        String bucketName = "dataproject23";
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                                           .withRegion(Regions.US_EAST_1)
                                           .build();

        File f = new File(name);
        System.out.println(f.getAbsolutePath());
        String path = f.getAbsolutePath();

        try {
            s3.putObject(bucketName, name, moveAndStoreFile(file, name, path));
        } catch (SdkClientException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "main";
    }

    // Konwersja do multipart file - inaczej sie nie da przeslac
    public static File moveAndStoreFile(MultipartFile file, String name, String path) throws IOException {
        String url = path + name;
        System.out.println(url);
        File fileToSave = new File(url);
        fileToSave.createNewFile();
        FileOutputStream fos = new FileOutputStream(fileToSave);
        fos.write(file.getBytes());
        fos.close();
        return fileToSave;
    }
}

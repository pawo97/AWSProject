import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.util.IOUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

public class ImageConverterS3 {

    public void getMessageToQueue(AmazonSQS sqs, String queueUrl) {
        List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();


            for (Message m : messages) {
                System.out.println(m.getBody());
                if(m.getBody().contains(".jpg")) {
                    try {
                        Thread.sleep(5000);
                        MultipartFile file = saveGraphicAsImage(m.getBody());
                        uploadFile(file);
                        download(m.getBody());
                        sqs.deleteMessage(queueUrl, m.getReceiptHandle());
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }

                }
            }
        }

    public String download(String nameOfFileToDownload) {

        System.out.format("Downloading %s from S3 bucket %s...\n", nameOfFileToDownload, ImageConverter.bucketName);

        BasicSessionCredentials awsCreds = ImageConverter.createCredentials();

        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(ImageConverter.region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();

        try {
            S3Object o = s3.getObject(ImageConverter.bucketName, nameOfFileToDownload);
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

        return nameOfFileToDownload;
    }

    public boolean uploadFile(MultipartFile file) {

        File f = new File(file.getOriginalFilename());
        System.out.println(f.getAbsolutePath());
        String path = f.getAbsolutePath();
        BasicSessionCredentials awsCreds = ImageConverter.createCredentials();

        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(ImageConverter.region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
        try {
            s3.putObject(ImageConverter.bucketName, file.getOriginalFilename(), moveAndStoreFile(file, path));
            return true;
        } catch (SdkClientException e) {
            System.out.println(e.getMessage());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static File moveAndStoreFile(MultipartFile file, String path) throws IOException {
        File fileToSave = new File(path);
        fileToSave.createNewFile();
        FileOutputStream fos = new FileOutputStream(fileToSave);
        fos.write(file.getBytes());
        fos.close();
        return fileToSave;
    }

    public MultipartFile saveGraphicAsImage(String fileName) throws IOException {

        // Constructs a BufferedImage of one of the predefined image types.
        BufferedImage bufferedImage = ImageIO.read(new File(fileName));

        // Create a graphics which can be used to draw into the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();

        // fill all the image with white
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        // create a circle with black
        g2d.setColor(Color.black);
        g2d.fillOval(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        // create a string with yellow
        g2d.setColor(Color.yellow);
        g2d.drawString("EDITED" + fileName,  bufferedImage.getWidth()/2, bufferedImage.getHeight()/2);


        // Disposes of this graphics context and releases any system resources that it is using.
        g2d.dispose();

        // Save as JPEG
        File file = new File("edited_" + fileName);
        ImageIO.write(bufferedImage, "jpg", file);

        //conversion to multipartfile
        FileInputStream input = new FileInputStream(file);
        return new MockMultipartFile("file",
                file.getName(), "image/jpg", IOUtils.toByteArray(input));
    }
}


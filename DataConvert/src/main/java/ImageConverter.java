import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageConverter {

    private static String queueUrl = "https://sqs.us-east-1.amazonaws.com/674297906961/kolejka23";
    public static String bucketName = "testowy24";
    public static String region = "us-east-1";
    public String filename = "facebook.jpg";

    public static BasicSessionCredentials createCredentials()
    {
        String awsId = "ASIAZZ72JQ4IZUF5L3LU";
        String awsKey = "vKA2Pvd6w0BpB/0KNTcJMufOy36a+bX+sz7wH/Vg";
        String sessionToken = "FwoGZXIvYXdzENP//////////wEaDP6Jt7H5AlosUSvEBSLDATpgoovPi+uxJi1P0dn+aE0a8iNmmQVbzGd3pVd5GObgu/cBDwUjChkeFkjfnyiIflUezanRmT780oG/KM5DHMKVM7QJMQ1uiAcq6uzhkngsevrUYQVgtSN8eH7QSyBEXoThAbRN1ZYTMt9VfJiNX4MvNBX+wwH6I9zIXT1H/6qiIQ28Jj1MaVMRxscY1vHLtgXjU97kpWZK/WMBDEeyV24nw4CtMx83zx0GS+nYdBUjejhr1pud7UOYfEDnpE7FC7yhHCj/h8j6BTItaEXpzCrjcs8jw7oN6jipSEfzj/mT3Z6vQuL3AOenISEzvVAVrGKFXLDZ/YDy";       BasicSessionCredentials awsCreds = new BasicSessionCredentials(awsId, awsKey, sessionToken);
        return awsCreds;
    }


    public static void main(String[] args) {
        try
        {
            BasicSessionCredentials awsCreds = createCredentials();
            AmazonSQS sqs = AmazonSQSClientBuilder.standard()
                    .withRegion(Regions.fromName(region))
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .build();
            ImageConverterS3 image = new ImageConverterS3();
            while(true)
            {
                image.getMessageToQueue(sqs, queueUrl);
            }

        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
    }



}

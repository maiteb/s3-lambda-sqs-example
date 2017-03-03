package br.com.maiteb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class S3TriggeredRequestHandler implements RequestHandler<S3Event, Object> {

    @Override
    public Object handleRequest(S3Event input, Context context) {
        List<S3EventNotificationRecord> s3EventNotificationRecords = input.getRecords();

        s3EventNotificationRecords.forEach(event -> processEvent(event, context.getLogger()));

        return "success";
    }

    private Object processEvent(S3EventNotificationRecord event, LambdaLogger logger) {
        AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
        S3Object object = s3
            .getObject(new GetObjectRequest(event.getS3().getBucket().getName(), event.getS3().getObject().getKey()));

        try (BufferedReader br = new BufferedReader(new InputStreamReader(object.getObjectContent()))) {
            String line;
            while ((line = br.readLine()) != null) {
                logger.log(line);
            }
        } catch (IOException e) {
            logger.log("Erro ao ler arquivo do s3");
        }

        return null;
    }

}

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
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class S3ToSQSRequestHandler implements RequestHandler<S3Event, Object> {

    private AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();

    private static final String QUEUE_NAME_ENV = "QUEUE_NAME";

    @Override
    public Object handleRequest(S3Event input, Context context) {
        List<S3EventNotificationRecord> s3EventNotificationRecords = input.getRecords();

        String queueName = System.getenv(QUEUE_NAME_ENV);
        AmazonSQSClient sqsClient = new AmazonSQSClient();
        String queueUrl = sqsClient.getQueueUrl(new GetQueueUrlRequest(queueName)).getQueueUrl();

        s3EventNotificationRecords.stream()
            .map(event -> processEvent(event, context.getLogger()))
            .forEach(message -> sendToSQS(message, sqsClient, queueUrl));

        return "success";
    }

    private void sendToSQS(String message, AmazonSQSClient sqsClient, String queueUrl) {
        SendMessageRequest myMessageRequest = new SendMessageRequest(queueUrl,
            message);
        sqsClient.sendMessage(myMessageRequest);
    }

    private String processEvent(S3EventNotificationRecord event, LambdaLogger logger) {
        S3Object object = s3
            .getObject(new GetObjectRequest(event.getS3().getBucket().getName(), event.getS3().getObject().getKey()));

        StringBuffer bf = new StringBuffer();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(object.getObjectContent()))) {
            String line;
            while ((line = br.readLine()) != null) {
                bf.append(line + "\n");
            }
        } catch (IOException e) {
            logger.log("Erro ao ler arquivo do s3");
        }

        return bf.toString();
    }

}

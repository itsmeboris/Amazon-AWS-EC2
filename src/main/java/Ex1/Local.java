package Ex1;

import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static Ex1.awsVars.*;

public class Local {

    final static String InstanceID = "ami-076515f20540e6e0b";
    final static String localApplicationID = LOCAL_APPLICATION_ID;

    final static Tag MANAGER_TAG = Tag.builder()
            .key("Name")
            .value("manager")
            .build();

    public static void main(String[] args) {
        final String USAGE =
                "To run this,supply an input file output file and number of files per worker\n";
        if (args.length < 3) {
            System.out.println(USAGE);
            System.exit(1);
        }
        String input = args[0];
        String output = args[1];
        int n = Integer.parseInt(args[2]);
        boolean terminate = false;
        if(args.length > 3 && args[3].equals("terminate")) {
            terminate = true;
        }
        AWS aws = new AWS();
        if(args.length == 5 && args[4].equals("b")) {
            deleteAllBuckets(aws);
        }
        uploadFileToS3(aws, INPUT_BUCKET_NAME + localApplicationID, input, input);
        //upload the jar
        uploadFileToS3(aws, APPLICATION_CODE_BUCKET_NAME,
                EX1_JAR, EX1_JAR);
        //upload worker script
        uploadFileToS3(aws, APPLICATION_CODE_BUCKET_NAME,
                WORKER_SCRIPT, WORKER_SCRIPT);
        //upload manager script
        uploadFileToS3(aws, APPLICATION_CODE_BUCKET_NAME,
                MANAGER_SCRIPT, MANAGER_SCRIPT);
        System.out.println("uploaded input file");
        String managerID;
        try {
            managerID = checkIfManagerRunning(aws);
            if(managerID == null){
                managerID = initiateManager(aws);
                System.out.println("Started manager");
            }
            else {
                System.out.println("manager is already running");
            }
            aws.initSQS();
            sendToSQS(aws, n, terminate, input, output);
            System.out.println("message was sent to queue");
            
            retrieveFromSQS(aws, TERMINATED_STRING);
            System.out.println("Output in S3");

            downloadOutput(aws, output);
            System.out.println("Downloaded output File");

            if(terminate){
                terminateManager(aws, managerID);
            }

        } catch (Ec2Exception e) {
            System.err.println( e.getLocalizedMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void deleteAllBuckets(AWS aws) {
        aws.deleteAllBuckets();
    }

    private static void terminateManager(AWS aws, String managerID) {
        retrieveFromSQS(aws, TERMINATED_STRING);
        aws.EC2TerminateInstance(managerID);
    }

    private static void downloadOutput(AWS aws, String output) throws IOException {
        File file = new File(output);
        file.createNewFile();
        aws.S3DownloadFiles(OUTPUT_BUCKET_NAME + localApplicationID, localApplicationID, file);
    }

    private static void retrieveFromSQS(AWS aws, String stringKey) {
        String outputQueueURL = aws.SQSinitializeQueue(APP_OUTPUT_QUEUE_NAME + localApplicationID, "0");
        boolean found = false;
        System.out.println("waiting to receive output");
        while(!found){
            List<Message> messages = aws.SQSReceiveMessages(outputQueueURL);
            for (Message message: messages){
                if (message.body().equals(stringKey)){
                    found = true;
                    String receiptHandle = message.receiptHandle();
                    aws.SQSDeleteMessage(outputQueueURL, receiptHandle);
                    break;
                }
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendToSQS(AWS aws, int n, boolean terminate, String input, String output) throws QueueDoesNotExistException {
        String inputQueueURL = aws.SQSinitializeQueue(APP_INPUT_QUEUE_NAME + localApplicationID, "0");
        String message = localApplicationID + SQS_MSG_DELIMETER + terminate
                            + SQS_MSG_DELIMETER + n + SQS_MSG_DELIMETER + input + SQS_MSG_DELIMETER + output;
        aws.SQSSendMessage(inputQueueURL, message);
    }

    private static String uploadFileToS3(AWS aws,String Bucket, String key, String input) {
        File file = new File(input);
        return aws.S3UploadFile(Bucket, key, file);
    }

    private static String initiateManager(AWS aws){
        return aws.EC2initiateInstance(InstanceID, 1, 1,
                               INSTANCE_TYPE, MANAGER_SCRIPT, MANAGER_TAG).get(0);
    }

    private static String checkIfManagerRunning(AWS aws){
        return aws.EC2SearchByTag(MANAGER_TAG,"running");
    }
}

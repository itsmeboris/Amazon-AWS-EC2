package Ex1;

import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.sqs.model.Message;

import java.io.File;
import java.util.*;

import static Ex1.awsVars.*;

public class Manager {

    private static boolean terminate;
    private static HashMap<String, WorkerDataStructure> localApplications;
    private static HashMap<String, String> awsQueues;
    private static int numOfWorkers;
    private final static Tag WORKER_TAG = Tag.builder()
            .key("Name")
            .value("worker")
            .build();
    private static Collection<String> workerInstanceIDs = new ArrayList<>();

    public static void main(String[] args) {
        AWS aws = new AWS();
        aws.InitAllServices();
        terminate = false;
        localApplications = new HashMap<>();
        try{
            initializeAllQueues(aws);
            while(true) {
                if(!terminate) {
                    //tasks to do
                    downloadInputFile(aws);
                }
                //check if finished all work
                checkFinishedTask(aws);
                if(localApplications.isEmpty() && terminate) {
                    deleteWorkers(aws);
                    aws.SQSDeleteQueue(awsQueues.get(MNG_INPUT_QUEUE_NAME));
                    aws.SQSDeleteQueue(awsQueues.get(MNG_OUTPUT_QUEUE_NAME));
                    System.out.println("MANAGER is exiting");
                    break;
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            aws.SQSSendMessage(awsQueues.get(APP_OUTPUT_QUEUE_NAME),TERMINATED_STRING);
            System.out.println("MANAGER FINISHED");
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

    }

    private static void deleteWorkers(AWS aws) {
        if(workerInstanceIDs.size() > 0){
            aws.EC2TerminateInstance(workerInstanceIDs);
            workerInstanceIDs.clear();
            numOfWorkers = 0;
        }
    }
    //check if finished all work
    private static void checkFinishedTask(AWS aws) {

    }

    //tasks to do
    private static void downloadInputFile(AWS aws) {
        // check if there is a new file
        String queueURL = awsQueues.get(APP_INPUT_QUEUE_NAME);
        List<Message> messages = aws.SQSReceiveMessages(queueURL);

        if(!messages.isEmpty()){
            Message message = messages.get(0);
            AWSMessage awsMessage = new AWSMessage(message, SQS_MSG_DELIMETER);
            File file = downloadFile(aws, awsMessage);
            List<String> inputs = parseInput(file);
            System.out.println("Downloaded Input File");
            initiateWrokers(aws, awsMessage.getSize(), inputs.size());
            System.out.println("started workers");
            localApplications.put(
                    awsMessage.getLocalApplicationID(),
                    new WorkerDataStructure(
                            awsMessage.getLocalApplicationID(),inputs.size()));
            sendWork(aws, inputs, awsMessage.getLocalApplicationID());
            System.out.println("Sent Messages to Workers");
            if(awsMessage.getTerminate().equalsIgnoreCase(TERMINATED_STRING)) {
                terminate = true;
                System.out.println("Request to terminate");
            }
            String receiptHandle = message.receiptHandle();
            aws.SQSDeleteMessage(queueURL, receiptHandle);
            System.out.println("deleted Message");
        }
    }

    private static void sendWork(AWS aws, List<String> inputs, String localApplicationID) {

    }

    private static List<String> parseInput(File file) {
        return null;
    }

    private static void initiateWrokers(AWS aws, int inputPerWorker, int size) {

    }

    private static File downloadFile(AWS aws, AWSMessage message) {
        return null;
    }

    private static void initializeAllQueues(AWS aws) {
        ArrayList<Map.Entry<String, String>> queues = new ArrayList<>();
        queues.add(new AbstractMap.SimpleEntry<>(APP_OUTPUT_QUEUE_NAME, "0"));
        queues.add(new AbstractMap.SimpleEntry<>(APP_INPUT_QUEUE_NAME, "0"));
        queues.add(new AbstractMap.SimpleEntry<>(MNG_OUTPUT_QUEUE_NAME, "0"));
        queues.add(new AbstractMap.SimpleEntry<>(MNG_INPUT_QUEUE_NAME, "30"));
        awsQueues = aws.SQSinitializeQueue(queues);
    }
}

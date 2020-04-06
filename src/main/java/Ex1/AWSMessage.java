package Ex1;

import software.amazon.awssdk.services.sqs.model.Message;

public class AWSMessage {

    private String localApplicationID;
    private String terminate;
    private int n;
    private String input;
    private String output;


    public AWSMessage(Message message, String delimiter) {
        String[] parse = message.body().split(delimiter);
        this.localApplicationID = parse[0];
        this.terminate = parse[1];
        this.n = Integer.parseInt(parse[2]);
        this.input = parse[3];
        this.output = parse[4];
    }


    public String getLocalApplicationID() {
        return localApplicationID;
    }

    public String getTerminate() {
        return terminate;
    }

    public int getSize() {
        return n;
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }
}

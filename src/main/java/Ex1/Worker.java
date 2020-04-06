package Ex1;

import software.amazon.awssdk.services.ec2.model.Tag;

public class Worker {

    final static Tag MAGAER_TAG = Tag.builder()
            .key("Name")
            .value("manager")
            .build();
    private static String localApplicationId;

    public static void main(String[] args) {

    }
}

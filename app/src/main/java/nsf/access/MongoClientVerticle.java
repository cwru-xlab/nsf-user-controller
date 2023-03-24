package nsf.access;

import io.vertx.core.AbstractVerticle;

public class MongoClientVerticle extends AbstractVerticle {

    /*
    Convenience method so you can run it in your IDE
     */
    public static void main(String[] args) {
        Runner.runExample(MongoClientVerticle.class);
    }

    @Override
    public void start() {

//        MongoClient mongoClient = MongoDbHelper.getMongoClient(vertx);
//        Policy policy = Policy.builder().serviceProviderId("serviceProviderId1").version("version1").addOperation(Operation.READ).addResource("resource1").build();
//        String collectionName = "access_control";
//        AccessControlService accessControlService = AccessControlService.builder().client(mongoClient).collection(collectionName).build();
//        accessControlService.createPolicyById(policy);
//        accessControlService.readPolicyById("serviceProviderId1").onComplete(res -> {
//            System.out.println(res.result());
//        });
    }
}
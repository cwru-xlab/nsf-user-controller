package nsf.access;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.mongo.MongoClient;

public class MongoClientVerticle extends AbstractVerticle {

    /*
    Convenience method so you can run it in your IDE
     */
    public static void main(String[] args) {
        Runner.runExample(MongoClientVerticle.class);
    }

    @Override
    public void start() throws Exception {

        MongoClient mongoClient = MongoDbHelper.getMongoClient(vertx);
        Policy policy = Policy.builder().addOperation(Operation.READ).addResource("resource1").build();
        ServiceProvider serviceProvider = ServiceProvider.builder().serviceProviderId("serviceProviderId1").version("version1").addPolicy(policy).build();
        String collectionName = "access_control";
        AccessControlService accessControlService = AccessControlService.builder().client(mongoClient).collection(collectionName).build();
        accessControlService.createServiceProviderById(serviceProvider);
        accessControlService.readServiceProviderById("serviceProviderId1");
    }
}
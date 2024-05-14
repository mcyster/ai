
module com.cyster.assistant {
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.module.jsonSchema.jakarta;
    requires java.net.http;
    requires org.slf4j;
    requires jvm.openai;
    
    exports com.cyster.assistant.service.advisor;
    exports com.cyster.assistant.service.conversation;
    exports com.cyster.assistant.service.scenario;
    
    //opens com.cyster.assistant.impl.advisor.openai to org.junit.jupiter.api;
    //opens com.cyster.assistant.impl.advisor.openai to com.cyster.assistant.test.impl.advisor.openai;
    opens com.cyster.assistant.impl.advisor.openai to org.junit.jupiter.api;

}

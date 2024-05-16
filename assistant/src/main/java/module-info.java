
module com.cyster.assistant {
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.module.jsonSchema.jakarta;
    requires java.net.http;
    requires org.slf4j;
    requires com.google.common;
    requires jvm.openai;
    
    exports com.cyster.assistant.service.advisor;
    exports com.cyster.assistant.service.conversation;
    exports com.cyster.assistant.service.scenario;
    
    uses com.cyster.assistant.service.advisor.AdvisorService; 
}

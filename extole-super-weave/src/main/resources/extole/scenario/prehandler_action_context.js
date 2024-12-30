
public interface PrehandlerActionContext extends PrehandlerContext {

    void replaceCandidatePerson(Person person);

    @Deprecated // TODO Use void log(String message) instead ENG-16894
    void addLogMessage(String logMessage);

    ProcessedRawEventBuilder getEventBuilder();
}

public interface PrehandlerContext extends GlobalContext, LoggerContext,
    RuntimeVariableContext {

    RawEvent getRawEvent();

    ProcessedRawEvent getProcessedRawEvent();

    @Nullable
    Person getCandidatePerson();

}

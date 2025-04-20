package com.extole.admin.weave.scenarios.report.writer;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.template.StringTemplate;
import com.extole.admin.weave.scenarios.help.ExtoleHelpScenario;
import com.extole.admin.weave.session.ExtoleSessionContext;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ExtoleReportWriterScenario implements Scenario<Void, ExtoleSessionContext> {
    private static final String DESCRIPTION = "Writes a configurable Extole report";

    private ExtoleHelpScenario helpScenario;

    ExtoleReportWriterScenario(ExtoleHelpScenario helpScenario) {
        this.helpScenario = helpScenario;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Scenario", "");
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Class<Void> getParameterClass() {
        return null;
    }

    @Override
    public Class<ExtoleSessionContext> getContextClass() {
        return ExtoleSessionContext.class;
    }

    @Override
    public ActiveConversationBuilder createConversationBuilder(Void parameters, ExtoleSessionContext context) {
        String messageTemplate = """
An Extole configurable report is defining by 'mappings' which is a series of columns separated by a new line or semicolon
A column is defined as the column name, followed by an equals sign, followed by an expression: NAME=EXPRESSION

# Examples

Count of events by name:
name=event.name;
count=group_count_distinct(event.id, name:"all");

Count of events by namem app_type and api_type:
name=event.name;
app_type=event.appType;
api_type=event.apiType;
count=group_count_distinct(event.id, name:"all");

Count of visitors to the terms page by day:
day=END_DATE(event.eventTime, period:"DAY");
person_id=person(event.personId).id;
count=group_count_distinct(event.id, name:"terms");

Count of shared and converted steps by person:
person_email=person(event.personId).email;
shares=group_count_distinct(event.id, step_name:"shared");
conversions=group_count_distinct(event.id, step_name:"converted");


The attribues on event are defined in StepRecord

public interface StepRecord {

    String DATA_FIELD_LOCALE = "locale";
    String DATA_FIELD_COUNTRY = "country";
    String DATA_FIELD_LANGUAGE = "language";
    String DATA_FIELD_AMOUNT = "amount";
    String DATA_FIELD_SOURCE = "source";
    String DATA_FIELD_SOURCE_TYPE = "source_type";
    String DATA_FIELD_CHANNEL = "channel";
    String DATA_FIELD_REFERRAL_REASON = "referral_reason";
    String DATA_FIELD_REFERRAL_REASON_CODE = "referral_reason_code";
    String DATA_FIELD_PARTNER_USER_ID = "partner_user_id";
    String DATA_FIELD_RELATED_PERSON_ID = "related_person_id";
    String DATA_FIELD_PARTNER_EVENT_ID_NAME = "partner_event_id_name";
    String DATA_FIELD_PARTNER_EVENT_ID_VALUE = "partner_event_id_value";
    String DATA_FIELD_API_TYPE = "api_type";

    String getId();

    String getClientId();

    String getEventTime();

    String getRequestTime();

    String getProgramLabel();

    @Nullable
    String getCampaignId();

    @Nullable
    String getDeviceProfileId();

    @Nullable
    String getIdentityProfileId();

    String getPersonId();

    String getContainer();

    String getPrimaryStepName();

    String getName();

    boolean isFirstSiteVisit();

    @Nullable
    Boolean getFirstProgramVisit();

    @Nullable
    Boolean getFirstCampaignVisit();

    @Nullable
    String getQuality();

    @Nullable
    String getRelatedPersonId();

    String getRootEventId();

    String getVisitType();

    String getAttribution();

    Map<String, String> getData();

    @Nullable
    String getJourneyName();

    String getDeviceType();

    String getDeviceOs();

    @Nullable
    String getVariant();

    String getAppType();

    Map<String, String> getAppData();
}


Functions include:

person(person_id)
Fetches a person by id
Where: 
- person_id is the id of the person


Campaign(campaign_id)
Fetch a campaign by id
Where
- campaign_id is the id of the campaign

Contant(value)
Returns a constant
Where
- value is a constant string valuee


group_count(propertyName, filter)
Counts the number of uniqe values of propertyName filtered by filer
Filter is step_name followed by a the name of a step event or "all" to count all steps 
""";

        String message = new StringTemplate(messageTemplate).render(parameters);

        return this.helpScenario.createConversationBuilder(null, context).addMessage(message);
    }

}

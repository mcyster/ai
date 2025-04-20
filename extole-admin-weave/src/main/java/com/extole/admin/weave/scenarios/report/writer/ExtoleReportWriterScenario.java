package com.extole.admin.weave.scenarios.report.writer;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.tool.SearchTool;
import com.extole.admin.weave.scenarios.prehandler.ExtoleApiStore;
import com.extole.admin.weave.session.ExtoleSessionContext;

@Component
public class ExtoleReportWriterScenario implements Scenario<Void, ExtoleSessionContext> {
    private static final String DESCRIPTION = "Writes a configurable Extole report";

    private final Advisor<ExtoleSessionContext> advisor;

    ExtoleReportWriterScenario(AiAdvisorService aiAdvisorService, ExtoleApiStore extoleStore) {

        String instructions = """
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

                The attributes on event are defined in the java class StepRecord

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
                """
                .stripIndent();

        AdvisorBuilder<ExtoleSessionContext> builder = aiAdvisorService.getOrCreateAdvisorBuilder(getName());

        builder.setInstructions(instructions);

        SearchTool.Builder<ExtoleSessionContext> searchToolBuilder = builder
                .searchToolBuilder(ExtoleSessionContext.class);
        extoleStore.createStoreTool(searchToolBuilder);

        this.advisor = builder.getOrCreate();
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
        return Void.class;
    }

    @Override
    public Class<ExtoleSessionContext> getContextClass() {
        return ExtoleSessionContext.class;
    }

    @Override
    public ActiveConversationBuilder createConversationBuilder(Void parameters, ExtoleSessionContext context) {
        var builder = this.advisor.createConversationBuilder(context);

        return builder;
    }

}

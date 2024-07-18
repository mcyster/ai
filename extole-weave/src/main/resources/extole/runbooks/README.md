
# Runbooks to run against Extole Support Tickets

## Selection

Keywords should differentiate runbook from other runbooks. Best runbook is found by using a vector store to find the nearest runbook that has the set of words in the ticket that are "closest" to the keywords

Test with
```
curl -s -H 'Content-Type: application/json' 'http://localhost:8090/conversations/messages' -d '{"scenario":"extoleSupportTicketRunbookSelector", "parameters": { "ticketNumber": "SUP-NNNN" }}' | jq .
```

## Instrucions

Instructions are followed by the AI to execute the Runbook against a ticket

The supportHelp scenario is used to execute the Runbook, it has a large range of tools including (todo expose in api):
- extoleClientGetTool
- extolePersonSearchTool
- extolePersonGetTool
- extolePersonDataGetTool
- extolePersonRewardsGetTool
- supportTicketCommentSearchTool
- supportTicketCommentGetTool
- supportTicketCommentAddTool
- extoleClientEventSearchTool
- extoleNotificationGetTool
- extoleSummartReportTool
- access to all the configure reports as tools
- ....

The instructions support a mustache expression with the following variables avaliable (see RunbookScenarioParameters)
- ticketNumber
- runbookName
- clientId
- clientShortName

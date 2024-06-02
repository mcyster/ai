
# Extole

Extole is a SaaS marketing platform that leverages Sage
- in the administirative UI
- in the ticketing system to assist with classification and resolution of tickets

## Extole Ticket Scenarios

To ask a general question as a super user:
```
curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/conversations/messages' -d '{"scenario":"extoleSupportHelp", "prompt": "What is the client short name for Nuts.com" }' | jq -r .response
The client short name for Nuts.com is "nuts".
```

Classify a support ticket, example to classify support ticket SUP-41162:
```
curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/conversations/messages' -d '{"scenario":"extoleTicketRunbook", "prompt": "SUP-41162" }' | jq .
```

Find and apply the best runbook to a support ticket, example for SUP-41162:
```
curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/conversations/messages' -d '{"scenario":"extoleSupportTicket", "prompt": "SUP-41162" }' | jq .
```


## Extole Runbooks

The 'extoleSupportTicket' scenario, determines that best Runbook that matches a ticket then executes that Runbook.

There are a number of predefined runbooks
- extoleRunbookOther - the runbook of last resort
- extoleRunbookNotificationTrafficDecrease - this runbook handles decrease in traffic notification tickets
- extoleRunbookNotificationTrafficIncrease  - this runbook handles increase in traffic notification tickets
- ... 

It is also possible to configure Runbooks using YAML, by putting .yml files in the resource directory [extole/runbook](https://github.com/mcyster/ai/tree/main/sage-extole/src/main/resources/extole/runbooks), with the following attributes:
- name - name of the runbook (extoleRunbook is automatically prefixed)
- description - description of where the runbook should be applied
- keywords - words that would appear in a ticket that would match this runbook
- instructions - instructions to the advisor on what to do.

Restart sage-app and check your runbook is loaded with:
```
curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/scenarios' | jq -r '.[].name'   | grep extoleRunbook
extoleRunbookNotificationWebhook
extoleRunbookNotificationEmailRender
extoleRunbookNotificationPrehandler
extoleRunbookOther
extoleRunbookNotificationTrafficIncrease
extoleRunbookNotificationOther
extoleRunbookNotificationTrafficDecrease
```

Search for then runbook:
```
curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/conversations/messages' -d '{"scenario":"extoleRunbook", "prompt": "find the best runbook for: test, ai" }' | jq -r '.response'
{ "runbook": "extoleRunbookAiTest" }
```

Test the runbook:
```
curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/conversations/messages' -d '{"scenario":"extoleRunbookAiTest", "prompt": "Rabbit" }' | jq -r '.response'

Why did the rabbit go to the barber?


Because he had too many split hares! 🐰✂️"
```

## Extole Report Tools

It is also possible to configure Report Tools using YAML, by putting .yml files in the resource directory [extole/runbook](https://github.com/mcyster/ai/tree/main/sage-extole/src/main/resources/extole/reports), with the following attributes:
- name - name of tool and report, needs to be snake case 
- description - description of tool for ai to know to use
- reportName - name of report type
- rowLimit (optional, defaults to 10)
- parameters (a map of attributes)

If you have a report that lists consumer input event names, you could test it with:
```
curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/conversations/messages' -d '{"scenario":"extoleSupportHelp", "prompt": "For the client nuts.com get a list of all the consumer input event names" }' | jq .
```

## jira-app

Listens on a webhook for ticket creation and runs the 'extoleSupportTicket' scenario.


# Setup

EXTOLE_GITHUB_AII_KEY=username:password

Test with
```
git clone "https://$(echo $EXTOLE_GITHUB_API_KEY | awk -F: '{print $1}'):$(echo $EXTOLE_GITHUB_API_KEY | awk -F: '{print $2}')@github.com/extole/java-api.git"
```

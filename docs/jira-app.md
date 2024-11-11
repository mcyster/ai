
# Deployment

Deploy the jira-app to production with (you will need ssh credentials on prod-ai):
```
jira-app-deploy
```

# Setup

### Setup OpenAI API Key
Define 
  - JIRA_APP_DOMAIN=beep-boop.extole.com   # or localhost for dev

 Define
  - OPENAI_API_KEY

### Jira API Key
  - Define JIRA_API_KEY # $email:base64($token)
  - Define JIRA_WEBHOOK_SECRET   # e.g.: head /dev/urandom | tr -dc A-Za-z0-9 | head -c 20

### Setup outbound Webhook
  - Goto Jira, e.g. [Extole Jira](https://extole.atlassian.net/)
  - Click Cog icon (top right)
  - System
  - Webhooks
  - Select / Create Webhook
    - Specify URL to which events should be posted
      - http://beep-boop.extole.com:8090/tickets?secret=$JIRA_WEBHOOK_SECRET
      - if local dev use ngrok, see below
    - Events
      - JQL: All Issues
      - Issue: Created
      - Comment: Created

### Google key
  - Goto [Goole Cloud](https://console.cloud.google.com)
  - Create a project: BeepBoop
    - OAuth Consent Screen
      - Home Page: http://beep-boop.extole.com:8090
      - Privacy: http://beep-boop.extole.com:8090/privacy.html
      - Terms: http://beep-boop.extole.com:8090/terms.html
      - Roles: Identity
      - Users: Limit to extole employees
  - Create Credentials
    - Define:
      - GOOGLE_CLIENT_ID
      - GOOGLE_CLIENT_SECRET

### Github setup

Setup an ssh key for github, generate a key and define in ~/.ssh/config something like:
```
Host github.com
    User git
    IdentityFile ~/.ssh/id_rsa
```

Test with
```
ssh -T git@github.com
```


#### Google from the Command line
  - Goto [Goole Cloud](https://console.cloud.google.com)
    - Goto IAM & Admin > Service Accounts
      - Goto Service Sccounts
        - Create Service Account
          - Role: Basic/Browser
          - Grant access: to your user
       - create and download key as json
         - store json key at something like ~/.beep-boop/aws-credentials.json
- export GOOGLE_APPLICATION_CREDENTIALS=$HOME/.beep-boop/beepboop-435003-6bd5ff8547e9.json
-  token="$(oauth2l fetch --scope userinfo.email,userinfo.profile")
-  curl -H "Authorization: Bearer $token" "http://localhost:8090/scenarios"

### Github Key
  - export EXTOLE_GITHUB_API_KEY

### Extole Key:
  - define EXTOLE_SUPER_USER_API_KEY

## Public IP for Jira Webhook

An easy way to get a publicly accessible endpoint for development is ngrok
- https://ngrok.com/

To start the jira-app in development:
In your Jira account, you will need to setup a webhook
- https://extole.atlassian.net/plugins/servlet/webhooks
  - url: $NGROK_URL/tickets?secret=$JIRA_WEBHOOK_SECRET
  - issue requests for: create, comment create


# Using

## Runbooks
Execute the best runbook against a support ticket
```
curl -s  -H 'Content-Type: application/json' 'http://beep-boop.extole.com:8090/conversations/messages' -d '{"scenario":"ExtoleSupportTicket", "parameters": { "ticketNumber": "SUP-NNNNN" }}' | jq .
```

Determine the best Runbook given a set of keywords
```
curl -s  -H 'Content-Type: application/json' 'http://beep-boop.extole.com:8090/conversations/messages' -d '{"scenario":"ExtoleRunbookSelector", "prompt": "word1 word2" }' | jq .
```

Execute a specific Runbook Scenario (intended for testing purposes only):
```
curl -s  -H 'Content-Type: application/json' 'http://beep-boop.extole.com:8090/conversations/messages' -d '{"scenario":"ExtoleRunbookJoke", "parameters": {"ticketNumber": "SUP-NNNNN", "runbookName": "ExtoleRunbookJoke", "clientId": "NNNNNNNNNNN", "clientShortName": "NAME" }}' | jq .
```

## Activities
Determine the best activity given a ticket number
```
curl -s  -H 'Content-Type: application/json' 'http://beep-boop.extole.com:8090/conversations/messages' -d '{"scenario":"ExtoleSupportTicketActivity", "parameters": {"ticketNumber": "SUP-NNNNN" }}' | jq .
```

Determine best support activity given a set of keywords
```
curl -s  -H 'Content-Type: application/json' 'http://beep-boop.extole.com:8090/conversations/messages' -d '{"scenario":"ExtoleSupportActivity", "prompt": "word1 word2" }' | jq .
```

## Scenarios

Avaliable scenaios
```
curl -s  -H 'Content-Type: application/json' 'http://beep-boop.extole.com:8090/scenarios' | jq -r '.[].name'
```

Get the schama of a Report Run
```
curl -s -H "Authorization: Bearer $token" -H 'Content-Type: application/json' 'http://beep-boop.extole.com:8080/conversations/messages' -d '{ "scenario": "ExtoleSupportHelp", "prompt": "Can you write a typescript class for a row of the report id: s4y0iq3ses720z9dt8mf in client id: 1890234003 reportRunType is report_runner"}' | jq -r '.response.content'
```

## Support tickets

Get support tickets
```
curl -s 'http://beep-boop.extole.com:8090/support/tickets' | jq .
```

## References
- https://developer.atlassian.com/server/jira/platform/webhooks/
- https://ngrok.com/

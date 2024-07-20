



# Deployment

Deploy the jira-app to production with (you will need ssh credentials on prod-ai):
```
jira-app-deploy
```

# Setup

## Setup outbound Webhook
Goto Jira, e.g. [Extole Jira](https://extole.atlassian.net/)
  - Click Cog icon (top right)
  - System
  - Webhooks
  - Select / Create Webhook
    - Specify URL to which events should be posted
      - I'm using an ngrok url that relays requests to the jira-app server
    - Events
      - JQL: All Issues
      - Issue: Created
      - Comment: Created

## Public IP for Jira Webhook

An easy way to get a publicly accessible endpoint for development is ngrok
- https://ngrok.com/

To start the jira-app in development:
In your Jira account, you will need to setup a webhook
- https://extole.atlassian.net/plugins/servlet/webhooks
  - url: $NGROK_URL/tickets
  - issue requests for: create, comment create


# Using

Get the scehma of a Report Runne
```
curl -s -H "Authorization: Bearer $token" -H 'Content-Type: application/json' 'http://localhost:8080/conversations/messages' -d '{ "scenario": "ExtoleSupportHelp", "prompt": "Can you write a typescript class for a row of the report id: s4y0iq3ses720z9dt8mf in client id: 1890234003 reportRunType is report_runner"}' | jq -r '.response.content'
```

## References
- https://developer.atlassian.com/server/jira/platform/webhooks/
- https://ngrok.com/


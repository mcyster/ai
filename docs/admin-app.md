
# Deployment

Deploy the jira-app to production with (you will need ssh credentials on prod-ai):
```
admin-app-deploy
```

# Setup

### Setup OpenAI API Key
Define 
  - JIRA_APP_DOMAIN=beep-boop.extole.com   # or localhost for dev

 Define
  - OPENAI_API_KEY


## Runbooks
Get a list of supported scenarios
```
curl -s 'http://localhost:9000/scenarios' | jq .
```

Translate some text
```
curl -s -H 'Content-Type: application/json' 'http://localhost:9000/conversations/messages' -d '{ "scenario": "Translater", "prompt": "Welcome my son", "parameters": { "language": "en", "target_language": "fr" }}'
```

Get Help with your extole account, for example get your client short name
```
token=XXXXXX # from extole
curl -s -H "Authorization: Bearer $token" -H 'Content-Type: application/json'  'http://localhost:9000/conversations/messages' -d '{ "scenario": "ExtoleHelp", "prompt": "What is my client shortname"}'
```


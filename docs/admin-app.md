
# Deployment

Deploy the jira-app to production with (you will need ssh credentials on prod-ai):
```
admin-app-deploy
```

# Setup

### Setup OpenAI API Key
 Define
  - OPENAI_API_KEY

### Setup an ssh key to github

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

## Testing
Get a list of supported scenarios
```
curl -s 'http://localhost:9000/scenarios' | jq .
```

Translate some text
```
curl -s -H 'Content-Type: application/json' 'http://localhost:9000/conversations/messages' -d '{ "scenario": "Translate", "prompt": "Welcome my son", "parameters": { "language": "en", "target_language": "fr" }}'
```

Get Help with your Extole account, for example get your client short name
```
token=XXXXXX # from Extole
curl -s -H "Authorization: Bearer $token" -H 'Content-Type: application/json'  'http://localhost:9000/conversations/messages' -d '{ "scenario": "ExtoleHelp", "prompt": "What is my client shortname"}'
```


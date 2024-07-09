# ai
Restful ai app


# Development

Build everything:
```
cd $AI_HOME
./gradlew build
```

To start, for example, the weave-app:
```
./gradlew :weave-app:bootRun
```

Or just:
```
./gradlew
```

Debug the weave-app:
```
./gradlew :weave-app:bootRun --debug-jvm
```

# weave-app

To start the weave-app in development:
```
cd $AI_HOME
./gradlew :weave-app:bootRun
```

Check the weave-app is up with:
```
curl -s 'http://localhost:8080/' 
```

List scenarios
```
curl -s 'http://localhost:8080/scenarios'  | jq -r '.[].name'
```

Synchronously run a scenario:
```
curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/conversations/messages' -d '{"scenario":"translate", "prompt": "Hello World", "parameters": { "language": "en", "target_language": "fr" }}' | jq .
```

Synchronously run a scenario with details of what happened:
```
curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/conversations/messages?level=Debug' -d '{"scenario":"translate", "prompt": "Hello World", "parameters": { "language": "en", "target_language": "fr" }}' | jq .
```

# jira-app
Detail documentation can be found here: [jira-app](https://github.com/mcyster/ai/blob/main/docs/jira-app.md)

The jira-app waits for ticket creation events from the Jira webhook and attempts to apply the appropriate Runbook.

To build locally
```
cd $AI_HOME
./gradlew :jira-app:bootRun
```

Test helping support on a ticket with
```
curl -s  -H 'Content-Type: application/json' 'http://localhost:8090/conversations/messages' -d '{"scenario":"extoleSupportTicket", "parameters": { "ticketNumber": "SUP-NNNN" }}' | jq .
```

Test mapping a ticket to a runbook
```
curl -s  -H 'Content-Type: application/json' 'http://localhost:8090/conversations/messages' -d '{"scenario":"extoleTicketRunbook", "parameters": { "ticketNumber": "SUP-NNNN" }}' | jq .
```

# Development Environment Setup

Clone this github repository:
```
git clone git@github.com:mcyster/ai.git ai
```

Setup the development environment, for example in your .bashrc, put something like:
```
ai_up() {
  (

    export AI_HOME=$HOME/ai
    export OPENAI_API_KEY="XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" 
    export BRANDFETCH_API_KEY="XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
    export JIRA_API_KEY="XXXXXXXXXXXXXXXXXXXXXXXXXXXX"
    export EXTOLE_SUPER_USER_API_KEY="XXXXXXXXXXXXXXXXXXXXXX"  # if using extole scenarios

    cd $AI_HOME
    nix-shell --command 'alias "cd-"="cd $AI_HOME"; PS1="\[\033[$PROMPT_COLOR\]\[\e]0;\u@\h>\w\a\]\u@\h:\w#>\[\033[0m\] "; return'
  )
}

alias "ai-up"=ai_up
```

After editing, source your .bashrc:
```
. .bashrc
```

To setup the development envionment
```
ai-up
```



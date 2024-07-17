

# Minor Changes

- load vstore lazily, load old version, then update in background
- prehandler use SearchTool
- cleanup vectorstores, files
- Scenario parameters ignored
- change parameters -> data for scenario

# Major Changes

## Web Weave

- pull page title for list
- support tags
- support archive
- allow ai to do all operations, copy, save as, ...
- don't allow managed pages to be edited, change pattern to copy then edit

## Extole Weave

- expose report api
- create page endpoint to create an initial listing page to develop from

## Cyster Weave

- move session store to weave-rest

## Jira App

From ticket expose
- client id
- comments
- support comment to self, to re-evaluate in future (or do in another service)

## AI Weave Concepts

Do we need, both

Scenario parameters generific, could generify result as well
- Scenario
  - Describes an api and context for having a Conversation
  - Use by the rest api to describe the questions it can answer and the context that needs to be provided
- Tool
  - Describe an api for asking a question (One response not a Conversation like a Scenaio)
  - Currently have a decorator to turn a scenario into a Tool

ScenarioSet doesn't really need to exist, just conveninent for Spring integration

log level
  - should include all aspects of conversation.
  - If nested conversations, it should be possible to navigate and see them.  To properly support this, feel I might need to move to an event based architecture.
  - tbd how to expose in api so that conversations can nest

## Weave++ Concepts

- Schedule in external service
- 

## Conversation

- Support an ascynronous api
- Support rich conversation logs
- Seperate memory from logs

## Documentation

Mechanism to crawl / upload documentation

QDRant

### Public Code

Upload code directly from github

## Deployment / systemd

Commit basic deployment scripts

Update scripts to work with systemd or perhaps goto Lamdbas?

Leverage secret store for keys


# Modules

Use modules to declare public classes etc rather than separate builds

Testing here
- https://github.com/mcyster/jpms




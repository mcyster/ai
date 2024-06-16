

# Grapher

## Extole Grapher

```
cd $AI_HOME
./gradlew :sage-app:bootRun
```

Generate a graph
```
curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/conversations/messages' -d '{"scenario":"extoleGrapher", "parameters": { "reportId": "seswys3ctzl8wy0y8s8t" }}' | jq .
curl -s  -H 'Content-Type: application/json' 'http://localhost:8080/conversations/messages' -d '{"scenario":"extoleGrapher", "prompt": "Can you display the report as text in json", "parameters": { "reportId": "seswys3ctzl8wy0y8s8t" }}' | jq .
```

package com.cyster.ai.weave.impl.openai;

import java.net.http.HttpClient;
import java.time.Duration;

import io.github.stefanbratanov.jvm.openai.OpenAI;

public class OpenAiService {
  
    private String apiKey;
    
    public OpenAiService() {
        this(System.getenv("OPENAI_API_KEY"));            
    }
    
    public OpenAiService(String apiKey) {
        this.apiKey = apiKey;
    }

    public OpenAiClient createClient() {
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();
             
        return new OpenAiClient(OpenAI.newBuilder(apiKey)
            .httpClient(new HttpClientLogger(httpClient))
            .build());             
    }
}

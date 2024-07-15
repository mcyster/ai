package com.cyster.sage.impl.advisors;

import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.advisor.AdvisorService;
import com.cyster.ai.weave.service.advisor.Tool;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@Component
public class WeatherAdvisor implements Advisor<Void> {
    public final String NAME = "weather-advisor";

    private AdvisorService advisorService;
    private Optional<Advisor<Void>> advisor = Optional.empty();
    
    public WeatherAdvisor(AdvisorService advisorService) {
      this.advisorService = advisorService;
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ConversationBuilder<Void> createConversation() {
        if (this.advisor.isEmpty()) {
            AdvisorBuilder<Void> builder = this.advisorService.getOrCreateAdvisor(NAME);
                
            builder
                .setInstructions("Get the current weather of a location")
                .withTool(new WeatherTool());
                
            this.advisor = Optional.of(builder.getOrCreate());
        }
        return this.advisor.get().createConversation();
    }
    
    
    public static class WeatherTool implements Tool<Weather, Void> {
        
        WeatherTool() {       
        }
        
        @Override
        public String getName() {
            return "get_weather";
        }

        @Override
        public String getDescription() {
            return "Get the current weather of a location";
        }

        @Override
        public Class<Weather> getParameterClass() {
            return Weather.class;
        }

        @Override
        public Object execute(Weather weather, Void context) {
            WeatherUnit unit = WeatherUnit.CELSIUS;
            if (weather.unit != null) {
                unit = weather.unit;
            }
            return new WeatherResponse(weather.location, unit, new Random().nextInt(50), "sunny");  
        }

    }
    
    public static class Weather {
        @JsonPropertyDescription("City and state, for example: Perth, Western Australia")
        @JsonProperty(required = true)
        public String location;

        @JsonPropertyDescription("The temperature unit, can be 'Celsius' or 'Fahrenheit'")
        public WeatherUnit unit;
    }

    public enum WeatherUnit {
        CELSIUS, FAHRENHEIT;
    }

    public static class WeatherResponse {
        public String location;
        public WeatherUnit unit;
        public int temperature;
        public String description;

        public WeatherResponse(String location, WeatherUnit unit, int temperature, String description) {
            this.location = location;
            this.unit = unit;
            this.temperature = temperature;
            this.description = description;
        }
    }

}

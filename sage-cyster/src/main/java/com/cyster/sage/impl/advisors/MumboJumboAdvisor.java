package com.cyster.sage.impl.advisors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.advisor.AdvisorBuilder;
import com.cyster.sherpa.service.advisor.AdvisorService;
import com.theokanning.openai.service.OpenAiService;

@Component
public class MumboJumboAdvisor implements Advisor<Void> {
    public final String NAME = "mumbo-jumbo-advisor";
    
    OpenAiService openAiService;
    private AdvisorService advisorService;
    private Optional<Advisor<Void>> advisor = Optional.empty();
    
    public MumboJumboAdvisor(AdvisorService advisorService) {
        this.advisorService = advisorService;
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ConversationBuilder<Void> createConversation() {
        var dictionaryPath = createDictionary();
        
        if (this.advisor.isEmpty()) {
            AdvisorBuilder<Void> builder = this.advisorService.getOrCreateAdvisor(NAME);
            builder
                .setInstructions("You are and advisor of nonsensical terms")
                //.withTool()
                .withFile(dictionaryPath);
                
            this.advisor = Optional.of(builder.getOrCreate());
        }
        
        try {
            Files.delete(dictionaryPath);
        } catch (IOException e) {
            throw new RuntimeException("unable to delete temporay dictionary file");
        }

        return this.advisor.get().createConversation();
    }

    private Path createDictionary() {
        String definitions = """
hsntde - a nonsexical acronym that has been choosen because it appears not to be commonly used\n
nhswlt - a second nonsexical acronym"
""";
    
        Path filePath;
        try {
            filePath =  Files.createTempFile("dictionary", ".txt");
            Files.write(filePath, definitions.getBytes());
            
        } catch (IOException exception) {
            throw new RuntimeException("problem creating dictionary file");
        }
        
        return filePath;
    }
}

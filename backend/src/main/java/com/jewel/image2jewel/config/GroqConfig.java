package com.jewel.image2jewel.config;

import com.jewel.image2jewel.service.LLMService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GroqConfig {

    @Value("${langchain4j.groq.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.groq.chat-model.model-name}")
    private String modelName;

    @Bean
    public ChatLanguageModel groqChatModel() {
        return OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    @Bean
    public LLMService llmService(ChatLanguageModel chatLanguageModel) {
        return AiServices.builder(LLMService.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }
}

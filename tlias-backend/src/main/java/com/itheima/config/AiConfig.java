package com.itheima.config;

import com.itheima.ai.AiAssistant;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AiConfig {

    @Value("${ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${langchain4j.open-ai.chat-model.base-url:}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:gpt-3.5-turbo}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.temperature:0.7}")
    private Double temperature;

    @Value("${langchain4j.open-ai.chat-model.max-tokens:2000}")
    private Integer maxTokens;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        if (!aiEnabled) {
            log.warn("AI服务未启用");
            return null;
        }
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("API Key未配置");
            return null;
        }
        try {
            ChatLanguageModel model = OpenAiChatModel.builder()
                    .baseUrl(baseUrl)
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .build();
            log.info("AI ChatLanguageModel 初始化成功: model={}, baseUrl={}", modelName, baseUrl);
            return model;
        } catch (Exception e) {
            log.error("AI ChatLanguageModel 初始化失败", e);
            return null;
        }
    }

    @Bean
    public AiAssistant aiAssistant(ChatLanguageModel chatLanguageModel) {
        if (!aiEnabled || chatLanguageModel == null) {
            log.warn("AiAssistant 未初始化");
            return null;
        }
        try {
            AiAssistant assistant = AiServices.create(AiAssistant.class, chatLanguageModel);
            log.info("AiAssistant 初始化成功");
            return assistant;
        } catch (Exception e) {
            log.error("AiAssistant 初始化失败", e);
            return null;
        }
    }
}

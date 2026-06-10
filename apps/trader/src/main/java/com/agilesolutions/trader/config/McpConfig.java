package com.agilesolutions.trader.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    ChatClient chatClient(ChatModel chatModel, SyncMcpToolCallbackProvider toolCallbackProvider) {

        return ChatClient
                .builder(chatModel)
                .defaultTools(toolCallbackProvider.getToolCallbacks())
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();

    }

}

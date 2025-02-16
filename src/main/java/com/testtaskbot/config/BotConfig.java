package com.testtaskbot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
public class BotConfig {
    private String botName = "wefmwpbot";
    private String botToken = "7673200194:AAGPYxAVD4LnUHPomKOF4MUQtFfC09yJg3w";

}

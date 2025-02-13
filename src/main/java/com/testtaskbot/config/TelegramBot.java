package com.testtaskbot.config;

import lombok.AllArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.BotOptions;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

import static jdk.javadoc.internal.tool.Main.execute;


@Component
@AllArgsConstructor
public class TelegramBot implements     LongPollingBot {

    private final BotConfig botConfig;


    @Override
    public void onUpdateReceived(Update update) {

    }

    @Override
    public BotOptions getOptions() {
        return null;
    }

    @Override
    public void clearWebhook() throws TelegramApiRequestException {

    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }


    private void sendMessage(String chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);

        execute(message);


    }
}

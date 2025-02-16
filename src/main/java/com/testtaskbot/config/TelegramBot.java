package com.testtaskbot.config;

import com.testtaskbot.model.entity.AppUser;
import com.testtaskbot.model.enums.ChatState;
import com.testtaskbot.model.enums.Sex;
import com.testtaskbot.service.AppUserService;
import com.testtaskbot.service.DocumentService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@AllArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private static final Map<Long, AppUser> cachedAppUsers = new HashMap<>();
    private static final Map<Long, ChatState> chatStates = new HashMap<>();
    private final BotConfig botConfig;

    @Autowired
    private DocumentService documentService;
    @Autowired
    private AppUserService appUserService;

    @Override
    public void clearWebhook() throws TelegramApiRequestException {}

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            long chatId = update.getMessage().getChatId();
            if (update.getMessage().hasText()) {
                String message = update.getMessage().getText();
                if (chatStates.containsKey(chatId)) {
                    handleStatefulMessage(update);
                } else {
                    if ("/start".equals(message)) {
                        chatStates.put(chatId, ChatState.CONFIRMATION_DATA_PROCESSING);
                        startCommandReceived(update);
                    } else {
                        sendMessage(chatId, "Неизвестная команда");
                    }
                }
            }
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }


    private void handleCallbackQuery(Update update) {
        String callBackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        switch (callBackData) {
            case "AGREE" :
                buttonAgreePressed(chatId);
                break;
            case "MALE":

                setSex(chatId, Sex.male);
                chatStates.put(chatId, ChatState.SAVE_USER);
                break;

            case "FEMALE":

                setSex(chatId, Sex.female);
                chatStates.put(chatId, ChatState.SAVE_USER);
                break;
        }
    }

    private void setSex(long chatId, Sex sex) {
        cachedAppUsers.computeIfPresent(chatId, (id, user) -> {
            user.setSex(sex);
            return user;
        });
        appUserService.save(cachedAppUsers.get(chatId));
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        InputFile file = new InputFile(new File(String.valueOf(documentService.generateDocWithUserInfo(cachedAppUsers.get(chatId)))));
        sendDocument.setDocument(file);

        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        chatStates.remove(chatId);;
    }

    private void buttonAgreePressed(long chatId) {
        AppUser newUser = new AppUser();
        cachedAppUsers.put(chatId, newUser);

        chatStates.put(chatId, ChatState.ENTER_NAME);
        sendMessage(chatId, "Вы подтвердили обработку персональных данных");
        sendMessage(chatId, "Пришлите ваше имя");
    }

    private void startCommandReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Подтвердите обработку персональных данных");
        sendMessage.setReplyMarkup(getAgreementButtons());

        chatStates.put(chatId, ChatState.ENTER_NAME);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup getAgreementButtons() {
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        InlineKeyboardButton agreementButton = new InlineKeyboardButton();
        agreementButton.setText("Соглашение");
        agreementButton.setUrl("https://www.google.com");
        buttons.add(agreementButton);

        InlineKeyboardButton agreeButton = new InlineKeyboardButton();
        agreeButton.setText("Согласиться");
        agreeButton.setCallbackData("AGREE");
        buttons.add(agreeButton);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(buttons);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    public void handleStatefulMessage(Update update) {
        long chatId = update.getMessage().getChatId();
        ChatState chatState = chatStates.get(chatId);
        String messageText = update.getMessage().getText();

        switch (chatState) {
            case ENTER_NAME -> {
                cachedAppUsers.computeIfPresent(chatId, (id, user) -> {
                    user.setName(messageText);
                    return user;
                });
                chatStates.put(chatId, ChatState.ENTER_SURNAME);
                sendMessage(chatId, "Пришлите вашу фамилию");
            }
            case ENTER_SURNAME -> {
                cachedAppUsers.computeIfPresent(chatId, (id, user) -> {
                    user.setSurname(messageText);
                    return user;
                });
                chatStates.put(chatId, ChatState.ENTER_BIRTH_DATE);
                sendMessage(chatId, "Пришлите вашу дату рождения в формате 'дд.мм.гггг'");
            }
            case ENTER_BIRTH_DATE -> {
                try {
                    LocalDate birthDate = LocalDate.parse(messageText, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    cachedAppUsers.computeIfPresent(chatId, (id, user) -> {
                        user.setBirthday(birthDate);
                        return user;
                    });
                    chatStates.put(chatId, ChatState.ENTER_PHOTO);
                    InlineKeyboardMarkup sexButtons = getSexButtons();
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("Выберите пол");
                    sendMessage.setReplyMarkup(sexButtons);
                    execute(sendMessage);
                } catch (Exception e) {
                    sendMessage(chatId, "Некорректный формат даты");
                }
            }

        }
    }


    private InlineKeyboardMarkup getSexButtons() {
        HashMap<String, String> buttons = new HashMap<>();
        String maleButton = "M";
        String maleCallBackData = "MALE";
        buttons.put(maleButton, maleCallBackData);
        String femaleButton = "F";
        String femaleCallBackData = "FEMALE";
        buttons.put(femaleButton, femaleCallBackData);


        return getInlineKeyboard(buttons, 1);
    }
    private static InlineKeyboardMarkup getInlineKeyboard (HashMap <String, String> buttons, int maxButtonsPerRow){
        List<List<InlineKeyboardButton>> buttonRows = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow = new ArrayList<>();
        List<String> buttonNames = new ArrayList<>(buttons.keySet());
        int cnt = 0;

        for (int i = 0; i < buttons.size(); i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttonNames.get(i));
            button.setCallbackData(buttons.get(buttonNames.get(i)));
            buttonsRow.add(button);
            cnt++;

            if (cnt == maxButtonsPerRow) {
                buttonRows.add(buttonsRow);
                buttonsRow = new ArrayList<>();
                cnt = 0;
            }
        }

        if (!buttonsRow.isEmpty()) {
            buttonRows.add(buttonsRow);
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(buttonRows);
        return inlineKeyboardMarkup;
    }




    private void sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

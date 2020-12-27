package fr.slapker.hangmanbot.helper;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class SendMessageHelper {


    public static SendMessage getTextMessageHtml(Update update, String textMessage) {
        SendMessage telegramMessage = new SendMessage(update.getMessage().getChatId().toString(), textMessage);
        telegramMessage.setParseMode("html");
        return telegramMessage;
    }
}

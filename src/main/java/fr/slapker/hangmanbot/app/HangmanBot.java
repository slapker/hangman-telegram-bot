package fr.slapker.hangmanbot.app;

import fr.slapker.hangmanbot.bo.UserBO;
import fr.slapker.hangmanbot.config.BotConfigBean;
import fr.slapker.hangmanbot.helper.SendMessageHelper;
import fr.slapker.hangmanbot.service.AdminService;
import fr.slapker.hangmanbot.service.GameService;
import fr.slapker.hangmanbot.service.UserService;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;
import java.util.Locale;
import java.util.stream.Stream;

@Log4j2
@Component
@NoArgsConstructor
public class HangmanBot extends TelegramLongPollingBot {

    @Autowired
    private BotConfigBean botConfig;

    @Autowired
    private GameService gameService;

    @Autowired
    private MessageSource messages;

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    @PostConstruct
    private void postConstruct() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
        } catch (TelegramApiException tlEx) {
            tlEx.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        UserBO userBO = userService.getUser(update.getMessage().getFrom());
        if (update.getMessage() != null && update.getMessage().getText() != null) {
            try {
                this.handleTextMessage(update, userBO);
            } catch (TelegramApiException tlgEx) {
                log.error(tlgEx);
            }
        }
    }

    /**
     * Handle text message received from telegram Chat
     *
     * @param update The update from telegram
     * @throws TelegramApiException
     */
    private void handleTextMessage(Update update, UserBO userBO) throws TelegramApiException {
        String incomingMessage = update.getMessage().getText();
        SendMessage tlgMessageToSend = null;

        if (EntriesEnum.QUOTE.getEntry().equalsIgnoreCase(incomingMessage)) {
            gameService.startQuoteGame(update, userBO);
        } else if (EntriesEnum.STOP.getEntry().equalsIgnoreCase(incomingMessage)) {
            tlgMessageToSend = gameService.stopQuoteGame(update);
        } else if (EntriesEnum.SCORE.getEntry().equalsIgnoreCase(incomingMessage)) {
            tlgMessageToSend = gameService.getGameScore(update);
        } else if (EntriesEnum.RESET.getEntry().equalsIgnoreCase(incomingMessage)) {
            tlgMessageToSend = gameService.resetScore(update);
        } else if (EntriesEnum.START.getEntry().equalsIgnoreCase(incomingMessage) || EntriesEnum.HELP.getEntry().equalsIgnoreCase(incomingMessage)) {
            tlgMessageToSend = getHelpBotMessage(update);
        } else if (EntriesEnum.ADMIN.getEntry().equalsIgnoreCase(incomingMessage) && update.getMessage().getFrom().getId().toString().equals(botConfig.getAdminId())) {
            tlgMessageToSend = adminService.getAppStats(update);
        } else {
            gameService.playGame(update);
        }

        if (tlgMessageToSend != null) {
            this.execute(tlgMessageToSend);
        }
    }

    public void sendMessage(Update update, String textMessage) {
        if (textMessage != null && !textMessage.equals("")) {
            SendMessage message = new SendMessage(update.getMessage().getChatId().toString(), textMessage);
            message.setParseMode("html");
            try {
                this.execute(message);
            } catch (TelegramApiException e) {
                log.error(e);
            }
        }
    }

    public SendMessage getHelpBotMessage(Update update) {
        StringBuilder messageStrBuilder = new StringBuilder();
        messageStrBuilder.append(messages.getMessage("help.message.head", null, Locale.FRANCE));
        Stream.of(EntriesEnum.values()).forEach(botEntry -> {
            if (botEntry != EntriesEnum.START && botEntry != EntriesEnum.ADMIN) {
                messageStrBuilder.append(messages.getMessage("help.message.function", new Object[]{botEntry.getEntry(), botEntry.getDesc()}, Locale.FRANCE));
            }
        });
        messageStrBuilder.append(messages.getMessage("help.message.footer", null, Locale.FRANCE));
        return SendMessageHelper.getTextMessageHtml(update, messageStrBuilder.toString());
    }

    @Override
    public String getBotUsername() {

        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {

        return botConfig.getBotToken();
    }
}

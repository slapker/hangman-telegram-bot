package fr.slapker.hangmanbot.service;

import fr.slapker.hangmanbot.helper.SendMessageHelper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Locale;

@Service
@Getter
public class AdminService {

    @Autowired
    UserService userService;

    @Autowired
    GameService gameService;

    @Autowired
    MessageSource messages;

    @Autowired
    QuoteService quoteService;

    public SendMessage getAppStats(Update update) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(messages.getMessage("admin.stat.header",null, Locale.FRANCE));
        strBuilder.append(messages.getMessage("admin.stat.nbUser",new Object[] { userService.getNbUsers()}, Locale.FRANCE));
        strBuilder.append(messages.getMessage("admin.stat.nbRoom",new Object[] { gameService.getNbGame()}, Locale.FRANCE));
        strBuilder.append(messages.getMessage("admin.stat.nbActiveRoom",new Object[] { gameService.getNbActiveGame()}, Locale.FRANCE));
        strBuilder.append(messages.getMessage("admin.stat.nbQuoteDb",new Object[] {quoteService.getNbQuoteBdd()}, Locale.FRANCE));
        strBuilder.append(messages.getMessage("admin.stat.gameStarted",new Object[] {gameService.getTotalGameStarted()}, Locale.FRANCE));

        return SendMessageHelper.getTextMessageHtml(update,strBuilder.toString());
    }
}

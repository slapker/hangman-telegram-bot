package fr.slapker.hangmanbot.service;

import fr.slapker.hangmanbot.app.HangmanBot;
import fr.slapker.hangmanbot.bo.UserBO;
import fr.slapker.hangmanbot.game.GameStateEnum;
import fr.slapker.hangmanbot.game.PlayResult;
import fr.slapker.hangmanbot.game.QuoteGame;
import fr.slapker.hangmanbot.helper.SendMessageHelper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Getter
public class GameService {

    @Autowired
    UserService userService;

    @Autowired
    QuoteService quoteService;

    @Autowired
    MessageSource messages;

    @Autowired
    HangmanBot hangmanBot;

    private int totalGameStarted = 0;

    private List<QuoteGame> gameList = new ArrayList();

    public void startQuoteGame(Update update, UserBO userBO) {
        totalGameStarted++;
        QuoteGame actualGame = getActualGame(update);

        if (actualGame == null) {
            actualGame = new QuoteGame(update.getMessage().getChatId(), quoteService, userService, messages);
            gameList.add(actualGame);
        }
        actualGame.setGameState(GameStateEnum.CHOOSE_ROUND);
        this.playGame(update);
    }

    public QuoteGame getActualGame(Update update) {
        return gameList.stream().filter(game -> game.getChatId() == update.getMessage().getChatId()).findFirst().orElse(null);
    }

    public SendMessage stopQuoteGame(Update update) {
        String textMessageToSend;
        QuoteGame actualGame = getActualGame(update);
        if (actualGame != null) {
            textMessageToSend = actualGame.stopGame();
        } else {
            textMessageToSend = messages.getMessage("action.stop.alreadyStopped", null, Locale.FRANCE);
        }
        return SendMessageHelper.getTextMessageHtml(update, textMessageToSend);
    }

    public SendMessage getGameScore(Update update) {
        String textMessageToSend;
        QuoteGame actualGame = getActualGame(update);
        if (actualGame != null) {
            textMessageToSend = actualGame.getScore();
        } else {
            textMessageToSend = messages.getMessage("game.score.noScore", null, Locale.FRANCE);
            ;
        }
        return SendMessageHelper.getTextMessageHtml(update, textMessageToSend);
    }

    public void playGame(Update update) {
        UserBO userBO = userService.getUser(update.getMessage().getFrom());
        QuoteGame actualGame = getActualGame(update);
        if (actualGame != null) {
            PlayResult result = actualGame.play(update, userBO);
            hangmanBot.sendMessage(update, result.getTextToSend());
            AtomicReference<PlayResult> atomicResult = new AtomicReference<>(result);
            if (result.isChainNextStep()) {
                if (atomicResult.get().getDelayBeforeNextStep() > 0) {
                    playWithDelay(update, actualGame, atomicResult);
                } else {
                    playGame(update);
                }
            }
        }
    }

    private void playWithDelay(Update update, QuoteGame actualGame, AtomicReference<PlayResult> atomicResult) {
        LocalDateTime startTime = LocalDateTime.now().plusSeconds(atomicResult.get().getDelayBeforeNextStep());
        Thread t1 = new Thread(() -> {
            boolean loop = true;
            while (loop) {
                if (LocalDateTime.now().isAfter(startTime)) {
                    actualGame.setWaitingMode(false);
                    playGame(update);
                    loop = false;
                }
            }
        });
        t1.start();
    }


    /**
     * Reset all score for the game in the chat
     *
     * @param update
     * @return
     */
    public SendMessage resetScore(Update update) {
        QuoteGame actualGame = getActualGame(update);
        if (actualGame != null) {
            return SendMessageHelper.getTextMessageHtml(update, actualGame.resetGameScore());
        } else {
            return SendMessageHelper.getTextMessageHtml(update, messages.getMessage("game.score.noScore", null, Locale.FRANCE));
        }
    }

    /**
     * Get all chat where a game has been launched
     *
     * @return
     */
    public int getNbGame() {
        return gameList.size();
    }

    /**
     * Get chat number, where a game have been launched in the last 7 days
     *
     * @return
     */
    public long getNbActiveGame() {
        return gameList.stream().filter(game -> game.getLastGameDate().isAfter(LocalDate.now().minus(7, ChronoUnit.DAYS))).count();
    }
}

package fr.slapker.hangmanbot.game;


import fr.slapker.hangmanbot.bo.QuoteBO;
import fr.slapker.hangmanbot.bo.UserBO;
import fr.slapker.hangmanbot.helper.GameHelper;
import fr.slapker.hangmanbot.service.QuoteService;
import fr.slapker.hangmanbot.service.UserService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Getter
@Setter
public class QuoteGame {

    private static final int BONUS_FASTEST_ANSWER = 3;
    private static final int WAITING_SECONDS = 4;
    private static final int FIRST_TRY_BONUS = 3;

    private QuoteService quoteService;
    private UserService userService;
    private MessageSource messages;
    private long chatId;
    private GameStateEnum gameState;
    private QuoteBO actualQuote;
    private String wordToFind;
    private String wordHidden;
    private String quoteWithHiddenWord;
    private Map<Integer, Integer> usersScore = new HashMap<>();
    private int tryingAttempts;
    private int nbStartedGames;
    private LocalDate lastGameDate = LocalDate.now();
    private int roundNumber;

    private boolean isWaitingMode;
    private LocalDateTime startTime;
    private long bestTime=0l;
    private UserBO fastestUser;

    public QuoteGame(Long pChatId, QuoteService pQuoteService, UserService pUserService, MessageSource pMessages) {
        userService = pUserService;
        quoteService = pQuoteService;
        messages = pMessages;
        chatId = pChatId;
        gameState = GameStateEnum.START;
    }

    public PlayResult play(Update update, UserBO user) {

        lastGameDate = LocalDate.now();
        checkUser(user);
        PlayResult result = new PlayResult();
        if (!isWaitingMode) {
            if (gameState.equals(GameStateEnum.CHOOSE_ROUND)) {
                this.resetGameScore();
                result.setTextToSend(messages.getMessage("game.play.chooseRound", null, Locale.FRANCE));
                gameState = GameStateEnum.GET_ROUND;
            } else if (gameState.equals(GameStateEnum.GET_ROUND)) {
                try {
                    roundNumber = Integer.parseInt(update.getMessage().getText()) - 1;
                    if (roundNumber < 0 || roundNumber > 1000) {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    result.setTextToSend(messages.getMessage("game.play.invalidRoundNumber", null, Locale.FRANCE));
                    return result;
                }
                result.setTextToSend(messages.getMessage("game.play.pleaseWait", new Object[]{(roundNumber + 1), WAITING_SECONDS}, Locale.FRANCE));
                result.setChainNextStep(true);
                result.setDelayBeforeNextStep(WAITING_SECONDS);
                gameState = GameStateEnum.START;
                isWaitingMode = true;
                return result;
            } else if (gameState.equals(GameStateEnum.BE_READY_NEXT_ROUND)) {
                StringBuilder textToSendStrB=new StringBuilder();
                textToSendStrB.append(messages.getMessage("game.play.readyForNext", new Object[]{WAITING_SECONDS}, Locale.FRANCE));
                if (roundNumber == 0) {
                    textToSendStrB.append(messages.getMessage("game.play.lastRound", null, Locale.FRANCE));
                }
                else {
                    textToSendStrB.append(messages.getMessage("game.play.roundLeft", new Object[]{(roundNumber+1)}, Locale.FRANCE));
                }

                result.setTextToSend(textToSendStrB.toString());
                result.setChainNextStep(true);
                result.setDelayBeforeNextStep(WAITING_SECONDS);
                gameState = GameStateEnum.START;
                isWaitingMode = true;
            } else if (this.gameState.equals(GameStateEnum.START)) {
                initializeQuote(result);
                startTime = LocalDateTime.now();
                result.setChainNextStep(false);
            } else if (this.gameState.equals(GameStateEnum.IN_GAME)) {
                String answer = update.getMessage().getText();
                boolean resultCheck = checkAnswer(answer);
                if (!resultCheck && !wordHidden.contains("_")) {
                    result.setTextToSend(messages.getMessage("game.play.loose", new Object[]{actualQuote.getQuoteText(), actualQuote.getQuoteAuthor()}, Locale.FRANCE));
                    gameState = GameStateEnum.FINSHED_ROUND;
                } else if (resultCheck) {
                    StringBuilder textToSendTsrb = new StringBuilder();

                    int nbPointsWon = wordToFind.length() - tryingAttempts;
                    textToSendTsrb.append(messages.getMessage("game.play.win", new Object[]{user.getVisibleName(), nbPointsWon, actualQuote.getQuoteText(), actualQuote.getQuoteAuthor()}, Locale.FRANCE));
                    if (tryingAttempts == 0) {
                        textToSendTsrb.append(messages.getMessage("game.play.winBonusFirstTry", new Object[]{FIRST_TRY_BONUS}, Locale.FRANCE));
                        addPointToUser(user, FIRST_TRY_BONUS);
                    }
                    textToSendTsrb.append(messages.getMessage("game.play.winQuote", new Object[]{actualQuote.getQuoteText(), actualQuote.getQuoteAuthor()}, Locale.FRANCE));
                    result.setTextToSend(textToSendTsrb.toString());
                    long millis = ChronoUnit.MILLIS.between(startTime,LocalDateTime.now());
                    if (bestTime == 0 || millis < bestTime) {
                        fastestUser = user;
                        bestTime=millis;
                    }
                    addPointToUser(user, nbPointsWon);
                    gameState = GameStateEnum.FINSHED_ROUND;
                } else {
                    result.setTextToSend(actualQuote.getQuoteText().replace(wordToFind, wordHidden));
                    result.setChainNextStep(false);
                }
                tryingAttempts++;
            } else if (gameState.equals(GameStateEnum.SHOW_SCORE)) {
                result.setTextToSend(this.getScore());
                result.setChainNextStep(false);
                gameState = GameStateEnum.FINISHED;
            }

            if (gameState.equals(GameStateEnum.FINSHED_ROUND)) {
                if (roundNumber > 0) {
                    roundNumber--;
                    gameState = GameStateEnum.BE_READY_NEXT_ROUND;
                    result.setChainNextStep(true);
                } else {
                    addPointToUser(fastestUser,BONUS_FASTEST_ANSWER);
                    gameState = GameStateEnum.SHOW_SCORE;
                    result.setChainNextStep(true);
                }
            }
        }

        return result;
    }

    /**
     * Initialize first Quote with hidden Word
     * @param result
     */
    private void initializeQuote(PlayResult result) {
        actualQuote = quoteService.getRandomQuote();
        tryingAttempts = 0;
        nbStartedGames++;
        if (actualQuote == null) {
            result.setTextToSend(messages.getMessage("game.play.errorGettingQuote", null, Locale.FRANCE));
            gameState = GameStateEnum.FINISHED;
        } else {
            wordToFind = GameHelper.createWordToFind(actualQuote);
            wordHidden = GameHelper.formatHiddenWord(wordToFind, true);
            quoteWithHiddenWord = actualQuote.getQuoteText().replace(wordToFind, wordHidden);
            gameState = GameStateEnum.IN_GAME;
            result.setTextToSend(quoteWithHiddenWord);
        }
    }

    /**
     * Stop game if not already stopped
     *
     * @return message about stopping action
     */
    public String stopGame() {
        String answer;
        if (gameState != GameStateEnum.FINISHED) {
            gameState = GameStateEnum.FINISHED;
            actualQuote = null;
            wordHidden = null;
            wordToFind = null;
            quoteWithHiddenWord = null;
            roundNumber=0;
            fastestUser=null;
            startTime=null;
            answer = "See ya !";
        } else {
            answer = "Nothing to stop";
        }
        return answer;
    }

    public String resetGameScore() {
        nbStartedGames = 0;
        fastestUser=null;
        startTime=null;
        usersScore = new HashMap<>();
        return messages.getMessage("game.score.reset", null, Locale.FRANCE);
    }

    /**
     * Retrieve score list of the current game
     *
     * @return score list
     */
    public String getScore() {
        StringBuilder answerStrBuilder = new StringBuilder();

        if (usersScore.size() == 0) {
            answerStrBuilder.append(messages.getMessage("game.score.noScore", null, Locale.FRANCE));
        } else {
            answerStrBuilder.append(messages.getMessage("game.score.head", null, Locale.FRANCE));

            int bestScore = 0;
            Integer idTlgUserWon = null;
            for (Map.Entry<Integer, Integer> mapentry : usersScore.entrySet()) {
                if (mapentry.getValue() > bestScore) {
                    bestScore = mapentry.getValue();
                    idTlgUserWon = mapentry.getKey();
                }
            }

            int worstScore = 10000;
            Integer idTlgUserLoose = null;
            for (Map.Entry<Integer, Integer> mapentry : usersScore.entrySet()) {
                if (mapentry.getValue() < worstScore) {
                    worstScore = mapentry.getValue();
                    idTlgUserLoose = mapentry.getKey();
                }
            }

            for (Map.Entry<Integer, Integer> mapentry : usersScore.entrySet()) {
                String emoji = "";
                if (mapentry.getKey().equals(idTlgUserWon)) {
                    emoji = "\uD83C\uDFC5";
                } else if (mapentry.getKey().equals(idTlgUserLoose)) {
                    emoji = "\uD83D\uDCA9";
                }

                UserBO userTmp = userService.getUserByTelegramId(mapentry.getKey());
                answerStrBuilder.append(messages.getMessage("game.score.user", new Object[]{userTmp.getVisibleName(), mapentry.getValue(), emoji}, Locale.FRANCE));
            }

            if (fastestUser != null) {
                answerStrBuilder.append(messages.getMessage("game.score.fastest", new Object[]{fastestUser.getVisibleName(),BONUS_FASTEST_ANSWER}, Locale.FRANCE));
            }
            answerStrBuilder.append(messages.getMessage("game.score.gamePlayed", new Object[]{nbStartedGames}, Locale.FRANCE));

        }
        return answerStrBuilder.toString();
    }

    public boolean checkAnswer(String answer) {
        if (answer.equalsIgnoreCase(this.wordToFind)) {
            return true;
        } else {
            int sizeCut;
            if (wordToFind.length() > 9) {
                sizeCut = 5;
            } else {
                sizeCut = 4;
            }

            String hiddenWordTemp = this.wordHidden.substring(0, this.wordHidden.length() - sizeCut);
            hiddenWordTemp = hiddenWordTemp.replace(" ", "");
            if (hiddenWordTemp.contains("_")) {
                int i = RandomUtils.nextInt(0, wordToFind.length());
                while (!hiddenWordTemp.substring(i, i + 1).equals("_")) {
                    i = RandomUtils.nextInt(0, wordToFind.length());
                }
                hiddenWordTemp = hiddenWordTemp.substring(0, i) + wordToFind.substring(i, i + 1) + hiddenWordTemp.substring(i + 1);
            }

            wordHidden = GameHelper.formatHiddenWord(hiddenWordTemp, false);
            return false;
        }
    }

    public void checkUser(UserBO user) {
        if (!usersScore.containsKey(user.getTelegramId())) {
            usersScore.put(user.getTelegramId(), 0);
        }
    }

    public void addPointToUser(UserBO user, int nbPoints) {
        if (user != null) {
            Integer newScore = usersScore.get(user.getTelegramId()) + nbPoints;
            usersScore.put(user.getTelegramId(), newScore);
        }
    }

}

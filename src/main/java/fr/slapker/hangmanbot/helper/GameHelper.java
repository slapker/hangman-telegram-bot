package fr.slapker.hangmanbot.helper;

import fr.slapker.hangmanbot.bo.QuoteBO;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Log4j2
@NoArgsConstructor
public class GameHelper {

    public static String createWordToFind(QuoteBO quote) {
        log.info(quote.getQuoteText());
        String quoteText = quote.getQuoteText();
        quoteText = quoteText.replace(".", "").replace(",", "").replace("!", "").replace("?", "").replace(":","").replaceAll("\n", "").replaceAll("\r", "");
        String[] wordTab = quoteText.split(" ");
        List<String> wordList = new ArrayList<>();
        for (int i=0; i<wordTab.length;i++) {
            if (wordTab[i].length() > 3 && !wordTab[i].contains("'")) {
                wordList.add(wordTab[i]);
            }
        }

        Random r = new Random();
        int wordNumber = r.nextInt(wordList.size()-1);
        String myWord = wordList.get(wordNumber);

/*        if (myWord.contains("'")) {
            String[] myWordsTab = myWord.split("'");
            myWord = myWordsTab[myWordsTab.length - 1];
        }*/

        return myWord;
    }

    public static String formatHiddenWord(String hiddenWordToFormat, boolean firstTime) {
        log.debug("hiddenWordToFormat : " + hiddenWordToFormat);
        String myWordHidden = " ";

        for (int i = 0; i < hiddenWordToFormat.length(); ++i) {
            if (!firstTime) {
                myWordHidden = myWordHidden + hiddenWordToFormat.substring(i, i + 1) + " ";
            } else {
                myWordHidden = myWordHidden + "_ ";
            }
        }

        myWordHidden = myWordHidden + " (" + hiddenWordToFormat.length() + ") ";
        log.debug("myWordHidden :" + myWordHidden);
        return myWordHidden;
    }
}


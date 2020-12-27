package fr.slapker.hangmanbot.gameTest;

import ch.qos.logback.core.CoreConstants;
import fr.slapker.hangmanbot.bo.QuoteBO;
import fr.slapker.hangmanbot.helper.GameHelper;
import org.junit.Assert;
import org.junit.Test;

public class GameTest {

    @Test
    public void createWordToFindTest() {
        QuoteBO quote = new QuoteBO();
        quote.setQuoteText("It is never too late. Even if you are going to die tomorrow, keep yourself straight and clear and be a happy human being today.");

        String word = GameHelper.createWordToFind(quote);
        System.out.println(word);
        Assert.assertTrue(word.length() > 4);
    }
}

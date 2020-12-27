package fr.slapker.hangmanbot.service;


import com.google.gson.Gson;
import fr.slapker.hangmanbot.bo.QuoteBO;
import fr.slapker.hangmanbot.model.Quote;
import fr.slapker.hangmanbot.repository.QuoteRepository;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

@NoArgsConstructor
@Service
@Slf4j
public class QuoteService {

    @Autowired
    QuoteRepository quoteRepo;

    private Client client;
    private WebTarget target;

    public QuoteBO getRandomQuote() {
        Gson gson = new Gson();
        QuoteBO myQuote=null;
        try {
            client = ClientBuilder.newClient();
            target = client.target("http://api.forismatic.com/api/1.0/?method=getQuote&lang=en&format=json");
            String myQuoteJson = target.request(new String[]{"application/json"}).get(String.class);
            myQuote = gson.fromJson(myQuoteJson,QuoteBO.class);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        if (myQuote != null) {
            persistQuoteAsynchron(myQuote);
        }

        return myQuote;
    }

    public void persistQuoteAsynchron(QuoteBO myQuote) {
        Thread t1 = new Thread(() -> {
                int nbExistingQuote = quoteRepo.getQuoteByLink(myQuote.getQuoteLink());
                if (nbExistingQuote == 0) {
                    Quote quote = new Quote();
                    quote.setText(myQuote.getQuoteText());
                    quote.setAuthor(myQuote.getQuoteAuthor());
                    quote.setLink(myQuote.getQuoteLink());
                    quoteRepo.save(quote);
                    log.info("Quote persisted");
                }
                else {
                    log.info(("Quote existing already. No need to store"));
                }
            });
        t1.start();
    }

    public long getNbQuoteBdd() {
        return quoteRepo.count();
    }
}

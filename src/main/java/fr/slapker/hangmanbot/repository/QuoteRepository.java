package fr.slapker.hangmanbot.repository;

import fr.slapker.hangmanbot.model.Quote;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteRepository extends CrudRepository<Quote, Long> {

    @Query("SELECT count(q.id) FROM Quote q WHERE q.link = ?1")
    int getQuoteByLink(String link);
}

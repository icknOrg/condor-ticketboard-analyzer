package org.coins1920.group05;

import org.coins1920.group05.fetcher.CondorCsvMarshaller;
import org.coins1920.group05.fetcher.DefaultCondorCsvMarshaller;
import org.coins1920.group05.fetcher.TicketBoard;
import org.coins1920.group05.fetcher.TrelloBoardFetcher;
import org.coins1920.group05.fetcher.model.condor.Person;
import org.coins1920.group05.fetcher.model.condor.Ticket;
import org.coins1920.group05.fetcher.model.trello.Card;
import org.coins1920.group05.fetcher.model.trello.Member;
import org.coins1920.group05.fetcher.util.Pair;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultTicketBoardCondorizer implements TicketBoardCondorizer {

    @Override
    public Pair<File, File> ticketBoardToCsvFiles(TicketBoard ticketBoardType, String boardId, String outputDir) {
        switch (ticketBoardType) {
            case TRELLO:
                return fetchTrelloBoard(boardId, outputDir);

            case JIRA:
                throw new UnsupportedOperationException();

            default:
                throw new IllegalArgumentException("Ticket board type wasn't recognized!");
        }
    }

    private Pair<File, File> fetchTrelloBoard(String boardId, String outputDir) {
        final String apiKey = System.getenv("TRELLO_API_KEY");
        final String oauthToken = System.getenv("TRELLO_OAUTH_KEY");
        final TrelloBoardFetcher fetcher = new TrelloBoardFetcher(apiKey, oauthToken);

        final List<Member> trelloBoardMembers = fetcher.fetchBoardMembers(boardId);
        final List<Person> persons = trelloMembersToCondorPersons(trelloBoardMembers);

        final List<Card> trelloCards = fetcher.fetchTickets(boardId);
        final List<Ticket> tickets = trelloCardsToCondorTickets(trelloCards);

        final CondorCsvMarshaller condorCsvMarshaller = new DefaultCondorCsvMarshaller();
        return condorCsvMarshaller.write(persons, tickets, outputDir);
    }

    private List<Person> trelloMembersToCondorPersons(List<Member> members) {
        return members
                .stream()
                .map(m -> new Person(m.getId(), m.getFullName(), "")) // TODO: calculate "starttime"!
                .collect(Collectors.toList());
    }

    private List<Ticket> trelloCardsToCondorTickets(List<Card> cards) {
        return cards
                .stream()
                .map(c -> new Ticket(c.getName(), c.getId(), "", "",
                        "", "", "", "", // TODO: map other stuff as well!
                        "", "", ""))
                .collect(Collectors.toList());
    }

}

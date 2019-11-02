package org.coins1920.group05;

import org.apache.commons.lang3.SerializationUtils;
import org.coins1920.group05.fetcher.CondorCsvMarshaller;
import org.coins1920.group05.fetcher.DefaultCondorCsvMarshaller;
import org.coins1920.group05.fetcher.TicketBoard;
import org.coins1920.group05.fetcher.TrelloBoardFetcher;
import org.coins1920.group05.fetcher.model.condor.Person;
import org.coins1920.group05.fetcher.model.condor.Ticket;
import org.coins1920.group05.fetcher.model.trello.Action;
import org.coins1920.group05.fetcher.model.trello.Card;
import org.coins1920.group05.fetcher.model.trello.Member;
import org.coins1920.group05.fetcher.util.Pair;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        // fetch all cards for the given board:
        final Stream<Card> trelloCards = fetcher.fetchTickets(boardId)
                .stream()
                .map(c -> addAuthor(c, fetcher.fetchActionsForTicket(c.getId())));

        // the final data set should be "rectangular", i.e. a ticket/card tuple is duplicated
        // for _every_ member that changed it, wrote a comment, etc.:
        final List<Card> trelloCardsForAllAuthors = trelloCards
                .map(c -> duplicateCardForAllAuthors(c, toList(c.getMembers()))) // for a separate call, use: ..., fetcher.fetchMembersForTicket(c.getId()))) instead!
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        final List<Ticket> tickets = trelloCardsToCondorTickets(trelloCardsForAllAuthors);

        // write the CSV files to disc:
        final CondorCsvMarshaller condorCsvMarshaller = new DefaultCondorCsvMarshaller();
        return condorCsvMarshaller.write(persons, tickets, outputDir);
    }

    private Card addAuthor(Card card, List<Action> trelloActions) {
        final String cardCreatedTypeString = "createCard";
        final Action createCardAction = trelloActions
                .stream()
                .filter(a -> a.getType().equalsIgnoreCase(cardCreatedTypeString))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No action with type '" + cardCreatedTypeString + "' found!"));
        card.setCreator(createCardAction.getMemberCreator().getFullName());
        return card;
    }

    private List<Card> duplicateCardForAllAuthors(Card card, List<Member> members) {
        if (members.isEmpty()) {
            final List<Card> l = new LinkedList<>();
            l.add(card);
            return l;
        } else {
            return members.stream()
                    .map(m -> {
                        final Member[] ma = {m};
                        final Card cardClone = SerializationUtils.clone(card);
                        cardClone.setMembers(ma);
                        return cardClone;
                    })
                    .collect(Collectors.toList());
        }
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
                .map(cardToTicket)
                .collect(Collectors.toList());
    }

    private <V> List<V> toList(V[] a) {
        if (a == null) {
            return new LinkedList<V>();
        } else {
            return Arrays.asList(a);
        }
    }

    private Function<Card, Ticket> cardToTicket = c -> {
        return new Ticket(c.getName(), c.getId(), c.getCreator(), "",
                "", "", "", "", // TODO: map other stuff as well!
                "", "", "");
    };

}

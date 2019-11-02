package org.coins1920.group05;

import org.coins1920.group05.fetcher.*;
import org.coins1920.group05.fetcher.model.condor.Person;
import org.coins1920.group05.fetcher.model.condor.Ticket;
import org.coins1920.group05.fetcher.model.trello.Member;
import org.coins1920.group05.fetcher.util.Pair;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultTicketBoardCondorizer implements TicketBoardCondorizer {

    @Override
    public Pair<File, File> ticketBoardToCsvFiles(TicketBoard ticketBoardType, String boardId, String outputDir) {
        final TicketBoardFetcher fetcher;
        switch (ticketBoardType) {
            case TRELLO:
                final String apiKey = System.getenv("TRELLO_API_KEY");
                final String oauthToken = System.getenv("TRELLO_OAUTH_KEY");
                fetcher = new TrelloBoardFetcher(ticketBoardType, apiKey, oauthToken);
                break;

            case JIRA:
                throw new UnsupportedOperationException();

            default:
                throw new IllegalArgumentException("Ticket board type wasn't recognized!");
        }

        final List<Member> members = fetcher.fetchBoardMembers(boardId);
        final List<Person> persons = trelloMembersToCondorPersons(members);

        List<Ticket> tickets = new LinkedList<>(); // TODO: map!

        final CondorCsvMarshaller condorCsvMarshaller = new DefaultCondorCsvMarshaller();
        return condorCsvMarshaller.write(persons, tickets, outputDir);
    }

    private List<Person> trelloMembersToCondorPersons(List<Member> members) {
        return members
                .stream()
                .map(m -> new Person(m.getId(), m.getFullName(), "")) // TODO: calculate starttime!
                .collect(Collectors.toList());
    }

}

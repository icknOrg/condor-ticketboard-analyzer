package org.coins1920.group05;

import org.apache.commons.lang3.SerializationUtils;
import org.coins1920.group05.fetcher.*;
import org.coins1920.group05.fetcher.model.condor.Person;
import org.coins1920.group05.fetcher.model.condor.Ticket;
import org.coins1920.group05.fetcher.model.general.AbstractMember;
import org.coins1920.group05.fetcher.model.general.AbstractTicket;
import org.coins1920.group05.fetcher.model.github.Issue;
import org.coins1920.group05.fetcher.model.github.User;
import org.coins1920.group05.fetcher.model.trello.Action;
import org.coins1920.group05.fetcher.model.trello.Card;
import org.coins1920.group05.fetcher.model.trello.Member;
import org.coins1920.group05.fetcher.util.Pair;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultTicketBoardCondorizer implements TicketBoardCondorizer {

    @Override
    public Pair<File, File> ticketBoardToCsvFiles(TicketBoard ticketBoardType, String owner, String board, String outputDir) {
        switch (ticketBoardType) {
            case TRELLO:
                return fetchTrelloBoard(board, outputDir);

            case JIRA:
                throw new UnsupportedOperationException();

            case GITHUB:
                return fetchGitHubIssues(owner, board, outputDir);

            default:
                throw new IllegalArgumentException("Ticket board type wasn't recognized!");
        }
    }

    private Pair<File, File> fetchTrelloBoard(String boardId, String outputDir) {
        final String apiKey = System.getenv("TRELLO_API_KEY");
        final String oauthToken = System.getenv("TRELLO_OAUTH_KEY");
        final TrelloBoardFetcher fetcher = new TrelloBoardFetcher(apiKey, oauthToken);

        // fetch all board members:
        final List<Member> trelloBoardMembers = fetcher.fetchBoardMembers(null, boardId);

        // fetch all cards for the given board:
        final Stream<Card> trelloCards = fetcher
                .fetchTickets(null, boardId)
                .stream()
                .map(c -> addAuthor(c, fetcher.fetchActionsForTicket(c.getId())));

        // the final data set should be "rectangular", i.e. a ticket/card tuple is duplicated
        // for _every_ member that changed it, wrote a comment, etc.:
        final List<Card> trelloCardsForAllAuthors = trelloCards
                .map(c -> duplicateCardForAllAuthors(c, fetcher.fetchMembersForTicket(null, null, c.getId())))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        // map to edges (tickets) and persons (nodes), then write to CSV files:
        return mapAndWriteToCsvFiles(
                trelloBoardMembers,
                trelloCardsForAllAuthors,
                this::trelloMembersToCondorPersons,
                this::trelloCardsToCondorTickets,
                outputDir
        );
    }

    private Pair<File, File> fetchGitHubIssues(String owner, String board, String outputDir) {
        final String apiKey = System.getenv("GITHUB_API_KEY");
        final String oauthToken = System.getenv("GITHUB_OAUTH_KEY");
        final GitHubIssueFetcher fetcher = new GitHubIssueFetcher(apiKey, oauthToken);

        // fetch all repo contributors:
        final List<User> githubRepoContributors = fetcher.fetchBoardMembers(owner, board);

        // fetch all issues of the given repo:
        final Stream<Issue> githubIssues = fetcher
                .fetchTickets(owner, board)
                .stream();

        // the final data set should be "rectangular", i.e. a ticket/card tuple is duplicated
        // for _every_ member that changed it, wrote a comment, etc.:
        final List<Issue> githubIssuesForAllAuthors = githubIssues
                .map(i -> duplicateIssueForAllContributors(i, fetcher.fetchMembersForTicket(owner, board, i.getId())))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        // map to edges (tickets) and persons (nodes), then write to CSV files:
        return mapAndWriteToCsvFiles(
                githubRepoContributors,
                githubIssuesForAllAuthors,
                this::githubContributorsToCondorPersons,
                this::githubIssuesToCondorTickets,
                outputDir
        );
    }

    private Card addAuthor(Card card, List<Action> trelloActions) {
        final String cardCreatedTypeString = "createCard";
        final Action createCardAction = trelloActions
                .stream()
                .filter(a -> a.getType().equalsIgnoreCase(cardCreatedTypeString))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No action with type '" + cardCreatedTypeString + "' found!"));
        card.setCreator(createCardAction.getMemberCreator().getId());
        return card;
    }

    private List<Card> duplicateCardForAllAuthors(Card card, List<Member> members) {
        if (members.isEmpty()) {
            // TODO: this is NOT correct! it will only generate an edge for the creation (from the creator
            // TODO: to himself) if there are no other authors!
            // TODO: ALWAYS add an (n+1)th card with author := creator !
            final Member[] ma = {};
            card.setMembers(ma);
            card.setAuthor(card.getCreator());
            final List<Card> l = new LinkedList<>();
            l.add(card);
            return l;
        } else {
            return members.stream()
                    .map(m -> {
                        final Member[] ma = {m};
                        final Card cardClone = SerializationUtils.clone(card);
                        cardClone.setMembers(ma);
                        cardClone.setAuthor(m.getId());
                        return cardClone;
                    })
                    .collect(Collectors.toList());
        }
    }

    private List<Issue> duplicateIssueForAllContributors(Issue issue, List<User> contributors) {
        return new LinkedList<>(); // TODO: ...
    }

    private List<Person> trelloMembersToCondorPersons(List<Member> members) {
        final String fakeStartDate = "2010-09-12T04:00:00+00:00"; // TODO: calculate "starttime"!
        return members
                .stream()
                .map(m -> new Person(m.getId(), m.getFullName(), fakeStartDate))
                .collect(Collectors.toList());
    }

    private List<Ticket> trelloCardsToCondorTickets(List<Card> cards) {
        Function<Card, Ticket> cardToTicket = c -> {
            final String fakeStartDate = "2010-09-12T04:00:00+00:00"; // TODO: map other stuff as well!
            return new Ticket(c.getName(), c.getId(), c.getCreator(), c.getAuthor(),
                    fakeStartDate, fakeStartDate, "", "",
                    "", "", "");
        };

        return cards
                .stream()
                .map(cardToTicket)
                .collect(Collectors.toList());
    }

    private List<Person> githubContributorsToCondorPersons(List<User> githubRepoUsers) {
        return new LinkedList<>(); // TODO: ...
    }

    private List<Ticket> githubIssuesToCondorTickets(List<Issue> issues) {
        return new LinkedList<>(); // TODO: ...
    }

    private <M extends AbstractMember, T extends AbstractTicket> Pair<File, File> mapAndWriteToCsvFiles(
            List<M> trelloBoardMembers,
            List<T> trelloCardsForAllAuthors,
            Function<List<M>, List<Person>> toPersons,
            Function<List<T>, List<Ticket>> toTickets, String outputDir) {
        // map to persons and tickets:
        final List<Person> persons = toPersons.apply(trelloBoardMembers);
        final List<Ticket> tickets = toTickets.apply(trelloCardsForAllAuthors);

        // write the CSV files to disc:
        final CondorCsvMarshaller condorCsvMarshaller = new DefaultCondorCsvMarshaller();
        return condorCsvMarshaller.write(persons, tickets, outputDir);
    }

}

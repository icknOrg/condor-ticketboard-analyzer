package org.coins1920.group05.condorizor;

import org.apache.commons.lang3.SerializationUtils;
import org.coins1920.group05.fetcher.TrelloBoardFetcher;
import org.coins1920.group05.fetcher.model.condor.Actor;
import org.coins1920.group05.fetcher.model.condor.Edge;
import org.coins1920.group05.fetcher.model.condor.EdgeType;
import org.coins1920.group05.fetcher.model.general.AbstractMember;
import org.coins1920.group05.fetcher.model.general.CategorizedBoardMembers;
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

public class TrelloBoardCondorizor {

    public Pair<File, File> fetchTrelloBoard(String boardId, String outputDir) {
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
                .map(c -> duplicateCardForAllAuthors(c, fetcher.fetchMembersForTicket(c)))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        // map to edges (tickets) and persons (nodes), then write to CSV files:
        return CondorizorUtils.mapAndWriteToCsvFilez( // TODO: use the GitHub-version mapAndWrite() method!
                trelloBoardMembers,
                trelloCardsForAllAuthors,
                this::trelloMembersToCondorActors,
                this::trelloCardsToCondorEdges,
                outputDir
        );
    }

    private List<Actor> trelloMembersToCondorActors(List<Member> members) {
        final String fakeStartDate = "2010-09-12T04:00:00+00:00"; // TODO: calculate "starttime"!

        // eliminate duplicate users:
//        final Stream<Member> distinctBoardMembers = io.vavr.collection.List
//                .ofAll(members.getAssignees())
//                .appendAll(members.getCommentators())
//                .distinctBy(Member::getId)
//                .toJavaStream();

        // map users to actors:
        return members
                .stream()
                .map(m -> new Actor(m.getId(), m.getFullName(), fakeStartDate))
                .collect(Collectors.toList());
    }

    private List<Edge> trelloCardsToCondorEdges(List<Card> cards) {
        Function<Card, Edge> cardToTicket = c -> {
            final String fakeStartDate = "2010-09-12T04:00:00+00:00"; // TODO: map other stuff as well!
            return new Edge(c.getName(), c.getId(), c.getCreator(), c.getAuthor(),
                    fakeStartDate, fakeStartDate, "", "",
                    "", "", "", EdgeType.ASSIGNING); // TODO: use the real edge type!
        };

        return cards
                .stream()
                .map(cardToTicket)
                .collect(Collectors.toList());
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
}

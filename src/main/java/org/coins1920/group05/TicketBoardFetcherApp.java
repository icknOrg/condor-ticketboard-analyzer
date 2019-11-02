package org.coins1920.group05;

import org.coins1920.group05.fetcher.*;

import java.io.IOException;

/**
 * A ticket board fetcher for Condor.
 */
public class TicketBoardFetcherApp {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Not enough arguments given!");
        }
        final String ticketBoardString = args[0];
        final String boardId = args[1];

        final TicketBoard tbt;
        switch (ticketBoardString.toLowerCase()) {
            case "trello":
                tbt = TicketBoard.TRELLO;
                break;
            case "jira":
                tbt = TicketBoard.JIRA;
                break;
            default:
                throw new IllegalArgumentException("Could not recognize ticket board type!");
        }

        final String apiKey = System.getenv("TRELLO_API_KEY");
        final String oauthToken = System.getenv("TRELLO_OAUTH_KEY");

        final TicketBoardFetcher fetcher = new TicketBoardFetcherImpl(tbt, apiKey, oauthToken);
        fetcher.fetchBoardMembers(boardId);

        final CondorCsvMarshaller condorCsvMarshaller = new DefaultCondorCsvMarshaller();
        // TODO: condorCsvMarshaller.write(...);
    }

}

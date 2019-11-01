package org.coins1920.group05;

import org.coins1920.group05.fetcher.TicketBoard;
import org.coins1920.group05.fetcher.TicketBoardFetcher;
import org.coins1920.group05.fetcher.TicketBoardFetcherImpl;

/**
 * A ticket board fetcher for Condor.
 */
public class TicketBoardFetcherApp {

    public static void main(String[] args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("Not enough arguments given!");
        }
        final String ticketBoardString = args[0];
        final String apiKey = args[1];
        final String oauthToken = args[2];

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

        System.out.println(" API key = " + apiKey + " , OAuth token = " + oauthToken);
        final TicketBoardFetcher f = new TicketBoardFetcherImpl(tbt, apiKey, oauthToken);
        f.fetchBoards();
    }

}

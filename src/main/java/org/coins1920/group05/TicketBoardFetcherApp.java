package org.coins1920.group05;

import org.coins1920.group05.fetcher.*;
import org.coins1920.group05.fetcher.model.trello.Board;

import java.io.IOException;
import java.util.List;

/**
 * A ticket board fetcher for Condor.
 */
public class TicketBoardFetcherApp {

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            throw new IllegalArgumentException("Not enough arguments given!");
        }
        final String ticketBoardString = args[0];
        final String apiKey = args[1];
        final String oauthToken = args[2];
        System.out.println(" API key = " + apiKey + " , OAuth token = " + oauthToken);

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

        final TicketBoardFetcher f = new TicketBoardFetcherImpl(tbt, apiKey, oauthToken);
        final List<Board> boards = f.fetchBoards();

        final CondorCsvMarshaller condorCsvMarshaller = new DefaultCondorCsvMarshaller();
        // TODO: condorCsvMarshaller.write(...);
    }

}

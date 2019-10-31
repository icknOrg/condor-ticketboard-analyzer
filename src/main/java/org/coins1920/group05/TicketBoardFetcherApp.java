package org.coins1920.group05;

import org.coins1920.group05.fetcher.TicketBoard;
import org.coins1920.group05.fetcher.TicketBoardFetcher;
import org.coins1920.group05.fetcher.TicketBoardFetcherImpl;

/**
 * A ticket board fetcher for Condor.
 */
public class TicketBoardFetcherApp {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        if (args.length < 1) {
            throw new IllegalArgumentException("Not enough arguments given!");
        }

        final TicketBoard tbt;
        switch (args[0].toLowerCase()) {
            case "trello": tbt = TicketBoard.TRELLO; break;
            case "jira": tbt = TicketBoard.JIRA; break;
            default: throw new IllegalArgumentException("Could not recognize ticket board type!");
        }

        final TicketBoardFetcher f = new TicketBoardFetcherImpl("", ""); // TODO: load from application.yml
        f.fetchBoards();
    }

}

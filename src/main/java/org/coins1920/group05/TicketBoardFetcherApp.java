package org.coins1920.group05;

import org.coins1920.group05.fetcher.TicketBoard;

import java.io.IOException;

/**
 * A ticket board fetcher for Condor.
 */
public class TicketBoardFetcherApp {

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            throw new IllegalArgumentException("Not enough arguments given!");
        }
        final String ticketBoardTypeString = args[0];
        final String boardId = args[1];
        final String outputDir = args[2];

        final TicketBoard tbt;
        switch (ticketBoardTypeString.toLowerCase()) {
            case "trello":
                tbt = TicketBoard.TRELLO;
                break;
            case "jira":
                tbt = TicketBoard.JIRA;
                break;
            default:
                throw new IllegalArgumentException("Could not recognize ticket board type!");
        }

        final TicketBoardCondorizer condorizer = new DefaultTicketBoardCondorizer();
        condorizer.ticketBoardToCsvFiles(
                tbt,
                boardId,
                outputDir
        );
    }

}

package org.coins1920.group05;

import org.coins1920.group05.fetcher.TicketBoard;

import java.io.IOException;

/**
 * A ticket board fetcher for Condor.
 */
public class TicketBoardFetcherApp {

    private static final TicketBoardCondorizer condorizer = new DefaultTicketBoardCondorizer();

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (args.length < 3) {
            throw new IllegalArgumentException("Not enough arguments given!");
        }
        final String ticketBoardTypeString = args[0];

        final String board;
        final String repoOwner;
        final String outputDir;
        final TicketBoard tbt;

        switch (ticketBoardTypeString.toLowerCase()) {
            case "trello":
                tbt = TicketBoard.TRELLO;
                repoOwner = null;
                board = args[1];
                outputDir = args[2];
                break;

            case "jira":
                tbt = TicketBoard.JIRA;
                repoOwner = null;
                board = args[1];
                outputDir = args[2];
                break;

            case "github":
                if (args.length < 4) {
                    throw new IllegalArgumentException("Not enough arguments given!");
                }
                tbt = TicketBoard.GITHUB;
                repoOwner = args[1];
                board = args[2];
                outputDir = args[3];
                break;

            default:
                throw new IllegalArgumentException("Could not recognize ticket board type!");
        }

        condorizer.ticketBoardToCsvFiles(
                tbt,
                repoOwner,
                board,
                outputDir
        );
    }

}

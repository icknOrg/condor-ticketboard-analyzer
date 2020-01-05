package org.coins1920.group05;

import org.coins1920.group05.condorizor.GitHubRepoCondorizor;
import org.coins1920.group05.condorizor.TrelloBoardCondorizor;
import org.coins1920.group05.fetcher.TicketBoard;
import org.coins1920.group05.util.Pair;

import java.io.File;
import java.io.IOException;

public class DefaultTicketBoardCondorizer implements TicketBoardCondorizer {

    @Override
    public Pair<File, File> ticketBoardToCsvFiles(
            TicketBoard ticketBoardType,
            String owner,
            String board,
            String outputDir
    ) throws IOException, ClassNotFoundException {
        switch (ticketBoardType) {
            case TRELLO:
                return new TrelloBoardCondorizor().fetchTrelloBoard(board, outputDir);

            case JIRA:
                throw new UnsupportedOperationException();

            case GITHUB:
                final boolean paginate = true;
                final boolean fetchClosedTickets = true;
                return new GitHubRepoCondorizor(paginate)
                        .fetchGitHubIssues(owner, board, fetchClosedTickets, outputDir)
                        .getOrElseThrow(f -> new RuntimeException(
                                "Couldn't fetch everything, the partial result is: " + f
                        ));

            default:
                throw new IllegalArgumentException("Ticket board type wasn't recognized!");
        }
    }
}

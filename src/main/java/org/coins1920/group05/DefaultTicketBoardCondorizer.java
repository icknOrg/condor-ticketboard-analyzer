package org.coins1920.group05;

import org.coins1920.group05.condorizor.GitHubRepoCondorizor;
import org.coins1920.group05.condorizor.TrelloBoardCondorizor;
import org.coins1920.group05.fetcher.TicketBoard;
import org.coins1920.group05.util.Pair;

import java.io.File;

public class DefaultTicketBoardCondorizer implements TicketBoardCondorizer {

    @Override
    public Pair<File, File> ticketBoardToCsvFiles(TicketBoard ticketBoardType, String owner, String board, String outputDir) {
        switch (ticketBoardType) {
            case TRELLO:
                return new TrelloBoardCondorizor().fetchTrelloBoard(board, outputDir);

            case JIRA:
                throw new UnsupportedOperationException();

            case GITHUB:
                final boolean paginate = true;
                return new GitHubRepoCondorizor(paginate).fetchGitHubIssues(owner, board, outputDir);

            default:
                throw new IllegalArgumentException("Ticket board type wasn't recognized!");
        }
    }

}

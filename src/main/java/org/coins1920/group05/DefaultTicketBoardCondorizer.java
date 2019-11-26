package org.coins1920.group05;

import org.coins1920.group05.condorizor.GitHubRepoCondorizor;
import org.coins1920.group05.condorizor.GitHubGQLRepoCondorizor;
import org.coins1920.group05.condorizor.TrelloBoardCondorizor;
import org.coins1920.group05.fetcher.TicketBoard;
import org.coins1920.group05.fetcher.util.Pair;

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
                //return new GitHubRepoCondorizor().fetchGitHubIssues(owner, board, outputDir);
                return new GitHubGQLRepoCondorizor().fetchGitHubIssues(owner, board, outputDir);

            default:
                throw new IllegalArgumentException("Ticket board type wasn't recognized!");
        }
    }

}

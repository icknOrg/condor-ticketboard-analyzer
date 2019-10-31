package org.coins1920.group05.fetcher;

import org.coins1920.group05.fetcher.model.trello.Board;

import java.util.List;

public interface TicketBoardFetcher {
    List<Board> fetchBoards();
}

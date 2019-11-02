package org.coins1920.group05.fetcher;

import org.coins1920.group05.fetcher.model.trello.Board;
import org.coins1920.group05.fetcher.model.trello.Member;

import java.util.List;

public interface TicketBoardFetcher {
    List<Board> fetchBoards();

    /**
     * Fetches a single board for a given boardId. The boardId is the ID string seen in Trello's
     * website URLs (e.g. "lgaJQMYA" in "https://trello.com/b/lgaJQMYA/team5coin").
     *
     * @param boardId the board's ID
     * @return a board POJO containing all info needed for a Condor import
     */
    Board fetchBoard(String boardId);

    /**
     * Fetches all members that have access to a given board. The board is identified via the ID string seen in
     * Trello's website URLs (e.g. "lgaJQMYA" in "https://trello.com/b/lgaJQMYA/team5coin").
     *
     * @param boardId the board's ID
     * @return the board's members
     */
    List<Member> fetchBoardMembers(String boardId);
}

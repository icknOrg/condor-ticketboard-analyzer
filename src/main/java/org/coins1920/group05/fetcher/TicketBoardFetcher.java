package org.coins1920.group05.fetcher;

import org.coins1920.group05.fetcher.model.general.AbstractAction;
import org.coins1920.group05.fetcher.model.general.AbstractBoard;
import org.coins1920.group05.fetcher.model.general.AbstractMember;
import org.coins1920.group05.fetcher.model.general.AbstractTicket;

import java.util.List;

/**
 * A generic ticket board fetcher interface. Abstracts from the actual ticket board
 * product (Trello, Jira, ...).
 */
public interface TicketBoardFetcher<B extends AbstractBoard,
        M extends AbstractMember,
        T extends AbstractTicket,
        A extends AbstractAction> {

    List<B> fetchBoards();

    /**
     * Fetches a single board for a given boardId.
     *
     * @param boardId the board's ID. For Trello boards this is the ID found in Trello's
     *                website URLs (e.g. "lgaJQMYA" in "https://trello.com/b/lgaJQMYA/team5coin")
     * @return a board POJO containing all info needed for a Condor import
     */
    B fetchBoard(String boardId);

    /**
     * Fetches all members that have access to a given board.
     *
     * @param boardId the board's ID. For Trello boards this is the ID found in Trello's
     *                website URLs (e.g. "lgaJQMYA" in "https://trello.com/b/lgaJQMYA/team5coin")
     * @return the board's members
     */
    List<M> fetchBoardMembers(String boardId);

    List<T> fetchTickets(String boardId);

    List<A> fetchActionsForTicket(String ticketId);

    List<M> fetchMembersForTicket(String ticketId);
}

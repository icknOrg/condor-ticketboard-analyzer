package org.coins1920.group05.fetcher;

import org.coins1920.group05.model.general.*;

import java.util.List;

/**
 * A generic ticket board fetcher interface. Abstracts from the actual ticket board
 * product (Trello, Jira, ...).
 */
public interface TicketBoardFetcher<B extends AbstractBoard,
        M extends AbstractMember,
        T extends AbstractTicket,
        A extends AbstractAction,
        C extends AbstractComment> {

    List<B> fetchBoards();

    /**
     * Fetches a single board for a given boardId.
     *
     * @param owner the board/repo owner's name
     * @param board the board name or ID. For Trello boards this is the ID found in Trello's
     *              website URLs (e.g. "lgaJQMYA" in "https://trello.com/b/lgaJQMYA/team5coin")
     * @return a board POJO containing all info needed for a Condor import
     */
    B fetchBoard(String owner, String board);

    /**
     * Fetches all members that have access to a given board.
     *
     * @param owner the board/repo owner's name
     * @param board the board name or ID. For Trello boards this is the ID found in Trello's
     *              website URLs (e.g. "lgaJQMYA" in "https://trello.com/b/lgaJQMYA/team5coin")
     * @return the board's members
     */
    List<M> fetchBoardMembers(String owner, String board);

    List<T> fetchTickets(String owner, String board, boolean fetchClosedTickets);

    List<A> fetchActionsForTicket(String ticketId);

    List<M> fetchMembersForTicket(T ticket);

    List<M> fetchAssigneesForTicket(T ticket);

    List<M> fetchCommentatorsForTicket(T ticket);

    List<C> fetchCommentsForTicket(T ticket);
}

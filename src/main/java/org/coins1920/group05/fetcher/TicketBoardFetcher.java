package org.coins1920.group05.fetcher;

import org.coins1920.group05.model.general.*;

import java.util.List;
import java.util.Optional;

/**
 * A generic ticket board fetcher interface. Abstracts from the actual ticket board
 * product (Trello, Jira, ...).
 *
 * @author Patrick Preuß (patrickp89)
 * @author Julian Cornea (buggitheclown)
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

    /**
     * Fetches all issues/tickets for a given repo.
     *
     * @param owner              the board/repo owner's name
     * @param board              the board name or ID
     * @param fetchClosedTickets whether to fetch closed tickets as well or not
     * @param visitedUrls        a list of URLs that have already been (successfully) visited before
     * @return a FetchingResult that contains the fetched entities as well as meta info on URLs visited
     */
    FetchingResult<T> fetchTickets(String owner, String board, boolean fetchClosedTickets, List<String> visitedUrls);

    List<A> fetchActionsForTicket(String ticketId);

    List<M> fetchMembersForTicket(T ticket);

    List<M> fetchAssigneesForTicket(T ticket);

    List<M> fetchCommentatorsForTicket(T ticket);

    FetchingResult<C> fetchCommentsForTicket(T ticket, List<String> visitedUrls);

    Optional<M> fetchAllInfoForUser(M user);
}

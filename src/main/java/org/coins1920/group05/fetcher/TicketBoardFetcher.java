package org.coins1920.group05.fetcher;

import org.coins1920.group05.fetcher.model.general.AbstractAction;
import org.coins1920.group05.fetcher.model.general.AbstractBoard;
import org.coins1920.group05.fetcher.model.general.AbstractMember;
import org.coins1920.group05.fetcher.model.general.AbstractTicket;
import org.coins1920.group05.fetcher.model.trello.Board;
import org.coins1920.group05.fetcher.model.trello.Member;

import java.util.List;

/**
 * A generic ticket board fetcher interface. Abstracts from the actual ticket board
 * product (Trello, Jira, ...).
 */
public interface TicketBoardFetcher {

    List<? extends AbstractBoard> fetchBoards();

    /**
     * Fetches a single board for a given boardId.
     *
     * @param boardId the board's ID. For Trello boards this is the ID found in Trello's
     *                website URLs (e.g. "lgaJQMYA" in "https://trello.com/b/lgaJQMYA/team5coin")
     * @return a board POJO containing all info needed for a Condor import
     */
    <T extends AbstractBoard> T fetchBoard(String boardId);

    /**
     * Fetches all members that have access to a given board.
     *
     * @param boardId the board's ID. For Trello boards this is the ID found in Trello's
     *                website URLs (e.g. "lgaJQMYA" in "https://trello.com/b/lgaJQMYA/team5coin")
     * @return the board's members
     */
    List<? extends AbstractMember> fetchBoardMembers(String boardId);

    List<? extends AbstractTicket> fetchTickets(String boardId);

    List<? extends AbstractAction> fetchActionsForTicket(String ticketId);

    List<? extends AbstractMember> fetchMembersForTicket(String ticketId);
}

package org.coins1920.group05;

import org.coins1920.group05.fetcher.TicketBoard;
import org.coins1920.group05.util.Pair;

import java.io.File;
import java.io.IOException;

public interface TicketBoardCondorizer {
    Pair<File, File> ticketBoardToCsvFiles(TicketBoard ticketBoardType, String owner, String board, String outputDir) throws IOException;
}

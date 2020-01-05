package org.coins1920.group05.fetcher;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.coins1920.group05.model.general.AbstractComment;
import org.coins1920.group05.model.general.AbstractMember;
import org.coins1920.group05.model.general.AbstractTicket;
import org.coins1920.group05.util.Pair;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Data
@AllArgsConstructor
public class PartialFetchingResult<T extends AbstractTicket,
        M extends AbstractMember,
        C extends AbstractComment> implements Serializable {

    private FetchingResult<T> issueFetchingResult;
    private List<Pair<T, FetchingResult<C>>> commentsFetchingResults;

    public PartialFetchingResult() {
        this.issueFetchingResult = new FetchingResult<>();
        this.commentsFetchingResults = new LinkedList<>();
    }
}

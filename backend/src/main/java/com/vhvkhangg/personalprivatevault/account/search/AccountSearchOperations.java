package com.vhvkhangg.personalprivatevault.account.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AccountSearchOperations {
    List<AccountSearchHit> search(AccountSearchQuery query);

    Map<Long, AccountSearchDocument> lookupDocuments(Set<Long> vaultEntryIds);
}

package co.rsk.db;

import co.rsk.core.Rsk;
import co.rsk.core.RskAddress;
import co.rsk.trie.Trie;
import org.ethereum.core.AccountState;

import static org.ethereum.util.ByteUtil.EMPTY_BYTE_ARRAY;

/**
 * Created by ajlopez on 13/10/2018.
 */
public class WorldRepository {
    private Trie trie;

    public WorldRepository(Trie trie) {
        this.trie = trie;
    }

    public AccountState getAccountState(RskAddress address) {
        byte[] accountData = this.trie.get(address.getBytes());

        if (accountData == null) {
            return null;
        }

        return new AccountState(accountData);
    }

    public byte[] getCode(RskAddress address) {
        return EMPTY_BYTE_ARRAY;
    }
}

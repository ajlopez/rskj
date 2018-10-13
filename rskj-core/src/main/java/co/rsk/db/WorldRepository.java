package co.rsk.db;

import co.rsk.core.RskAddress;
import co.rsk.trie.Trie;
import co.rsk.trie.TrieStore;
import org.ethereum.core.AccountState;
import org.ethereum.crypto.HashUtil;
import org.ethereum.vm.DataWord;

import java.util.Arrays;

import static org.ethereum.crypto.HashUtil.EMPTY_TRIE_HASH;

/**
 * Created by ajlopez on 13/10/2018.
 */
public class WorldRepository {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final byte[] EMPTY_DATA_HASH = HashUtil.keccak256(EMPTY_BYTE_ARRAY);

    private Trie trie;
    private TrieStore trieStore;

    public WorldRepository(Trie trie, TrieStore trieStore) {
        this.trie = trie;
        this.trieStore = trieStore;
    }

    public AccountState getAccountState(RskAddress address) {
        byte[] accountData = this.trie.get(address.getBytes());

        if (accountData == null) {
            return null;
        }

        return new AccountState(accountData);
    }

    public byte[] getCode(RskAddress address) {
        AccountState accountState = this.getAccountState(address);

        if (accountState == null || accountState.isHibernated()) {
            return EMPTY_BYTE_ARRAY;
        }

        byte[] codeHash = accountState.getCodeHash();

        if (Arrays.equals(codeHash, EMPTY_DATA_HASH)) {
            return EMPTY_BYTE_ARRAY;
        }

        byte[] code = this.trie.get(codeHash);

        return code;
    }

    public DataWord getStorageValue(RskAddress address, DataWord key) {
        AccountState accountState = this.getAccountState(address);

        if (accountState == null || Arrays.equals(EMPTY_TRIE_HASH, accountState.getStateRoot())) {
            return null;
        }

        return new ContractStorage(this.trieStore.retrieve(accountState.getStateRoot())).getValue(key);
    }
}

package co.rsk.db;

import co.rsk.core.RskAddress;
import co.rsk.trie.Trie;
import org.ethereum.core.AccountState;
import org.ethereum.crypto.HashUtil;

import java.util.Arrays;

/**
 * Created by ajlopez on 13/10/2018.
 */
public class WorldRepository {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final byte[] EMPTY_DATA_HASH = HashUtil.keccak256(EMPTY_BYTE_ARRAY);

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
        AccountState accountState = this.getAccountState(address);

        if (accountState == null || accountState.isHibernated()) {
            return EMPTY_BYTE_ARRAY;
        }

        byte[] codeHash = accountState.getCodeHash();

        if (Arrays.equals(codeHash, EMPTY_DATA_HASH)) {
            return EMPTY_BYTE_ARRAY;
        }

        byte[] code = this.trie.get(codeHash);

        if (code == null) {
            return EMPTY_BYTE_ARRAY;
        }

        return code;
    }
}

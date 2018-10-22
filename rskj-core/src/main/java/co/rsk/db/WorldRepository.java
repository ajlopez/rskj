package co.rsk.db;

import co.rsk.core.Coin;
import co.rsk.core.Rsk;
import co.rsk.core.RskAddress;
import co.rsk.trie.Trie;
import co.rsk.trie.TrieStore;
import org.ethereum.core.Account;
import org.ethereum.core.AccountState;
import org.ethereum.crypto.HashUtil;
import org.ethereum.vm.DataWord;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.ethereum.crypto.HashUtil.EMPTY_TRIE_HASH;

/**
 * Created by ajlopez on 13/10/2018.
 */
public class WorldRepository implements NewRepository {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final byte[] EMPTY_DATA_HASH = HashUtil.keccak256(EMPTY_BYTE_ARRAY);

    private Trie trie;
    private TrieStore trieStore;

    private Map<RskAddress, AccountState> accountStates = new HashMap<>();

    public WorldRepository(Trie trie, TrieStore trieStore) {
        this.trie = trie;
        this.trieStore = trieStore;
    }

    public Coin getAccountBalance(RskAddress address) {
        AccountState accountState = this.getAccountState(address);

        return accountState.getBalance();
    }

    public BigInteger getAccountNonce(RskAddress address) {
        AccountState accountState = this.getAccountState(address);

        return accountState.getNonce();
    }

    public byte[] getAccountCode(RskAddress address) {
        AccountState accountState = this.getAccountState(address);

        if (accountState.isHibernated()) {
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

        if (Arrays.equals(EMPTY_TRIE_HASH, accountState.getStateRoot())) {
            return null;
        }

        return new ContractStorage(this.trieStore.retrieve(accountState.getStateRoot())).getValue(key);
    }

    public byte[] getStorageBytes(RskAddress address, DataWord key) {
        AccountState accountState = this.getAccountState(address);

        if (Arrays.equals(EMPTY_TRIE_HASH, accountState.getStateRoot())) {
            return null;
        }

        return new ContractStorage(this.trieStore.retrieve(accountState.getStateRoot())).getBytes(key);
    }

    public BigInteger incrementAccountNonce(RskAddress accountAddress) {
        AccountState accountState = this.retrieveAccountState(accountAddress);

        accountState.incrementNonce();

        return accountState.getNonce();
    }

    public Coin addToAccountBalance(RskAddress accountAddress, Coin amount) {
        AccountState accountState = this.retrieveAccountState(accountAddress);

        return accountState.addToBalance(amount);
    }

    protected AccountState getAccountState(RskAddress accountAddress) {
        if (this.accountStates.containsKey(accountAddress)) {
            return this.accountStates.get(accountAddress);
        }

        byte[] accountData = this.trie.get(accountAddress.getBytes());

        if (accountData == null) {
            return new AccountState();
        }

        return new AccountState(accountData);
    }

    private AccountState retrieveAccountState(RskAddress accountAddress) {
        if (this.accountStates.containsKey(accountAddress)) {
            return this.accountStates.get(accountAddress);
        }

        byte[] accountData = this.trie.get(accountAddress.getBytes());
        AccountState accountState;

        if (accountData == null) {
            accountState = new AccountState();
        }
        else {
            accountState = new AccountState(accountData);
        }

        this.accountStates.put(accountAddress, accountState);

        return accountState;
    }
}

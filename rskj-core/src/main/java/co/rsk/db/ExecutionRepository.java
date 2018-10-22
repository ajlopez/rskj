package co.rsk.db;

import co.rsk.core.RskAddress;
import co.rsk.trie.Trie;
import org.ethereum.core.AccountState;
import org.ethereum.crypto.HashUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ajlopez on 13/10/2018.
 */
public class ExecutionRepository extends WorldRepository {
    private Map<RskAddress, AccountState> accountStates = new HashMap<>();
    private Map<RskAddress, byte[]> accountCodes = new HashMap<>();

    public ExecutionRepository(Trie trie) {
        super(trie, null);
    }

    public AccountState getAccountState(RskAddress accountAddress) {
        if (this.accountStates.containsKey(accountAddress)) {
            return this.accountStates.get(accountAddress);
        }

        AccountState accountState = super.getAccountState(accountAddress);

        this.accountStates.put(accountAddress, accountState);

        return accountState;
    }

    public void setAccountState(RskAddress address, AccountState accountState) {
        this.accountStates.put(address, accountState);
    }

    public byte[] getCode(RskAddress address) {
        if (this.accountCodes.containsKey(address)) {
            return this.accountCodes.get(address);
        }

        byte[] code = super.getAccountCode(address);

        this.accountCodes.put(address, code);

        return code;
    }

    public void setCode(RskAddress address, byte[] code) {
        AccountState accountState = this.getOrCreateAccountState(address);

        accountCodes.put(address, code);
        byte[] codeHash = HashUtil.keccak256(code);
        accountState.setCodeHash(codeHash);
    }

    private AccountState getOrCreateAccountState(RskAddress address) {
        AccountState accountState = this.getAccountState(address);

        if (accountState == null) {
            accountState = new AccountState();
            this.accountStates.put(address, accountState);
        }

        return accountState;
    }
}

package co.rsk.db;

import co.rsk.core.Coin;
import co.rsk.core.RskAddress;
import co.rsk.trie.Trie;
import co.rsk.trie.TrieImpl;
import org.ethereum.core.AccountState;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

/**
 * Created by ajlopez on 13/10/2018.
 */
public class ExecutionRepositoryTest {
    @Test
    public void getUnknownAccountStateAsNull() {
        Trie trie = new TrieImpl();
        ExecutionRepository repository = new ExecutionRepository(trie);

        AccountState accountState = repository.getAccountState(new RskAddress("0000000000000000000000000000000000001234"));

        Assert.assertNull(accountState);
    }

    @Test
    public void setAndGetAccountState() {
        RskAddress accountAddress = new RskAddress("0000000000000000000000000000000000001234");
        AccountState accountState = new AccountState(BigInteger.ONE, Coin.ZERO);
        Trie trie = new TrieImpl();

        ExecutionRepository repository = new ExecutionRepository(trie);

        repository.setAccountState(accountAddress, accountState);

        AccountState result = repository.getAccountState(accountAddress);

        Assert.assertNotNull(result);
        Assert.assertEquals(accountState.getBalance(), result.getBalance());
        Assert.assertEquals(accountState.getNonce(), result.getNonce());
    }

    @Test
    public void getNewAccountCode() {
        RskAddress accountAddress = new RskAddress("0000000000000000000000000000000000001234");
        AccountState accountState = new AccountState(BigInteger.ONE, Coin.ZERO);
        Trie trie = new TrieImpl();

        ExecutionRepository repository = new ExecutionRepository(trie);

        repository.setAccountState(accountAddress, accountState);

        byte[] code = repository.getCode(accountAddress);

        Assert.assertNotNull(code);
        Assert.assertEquals(0, code.length);
    }

    @Test
    public void setAndGetAccountCode() {
        byte[] code = new byte[] { 0x01, 0x02, 0x03 };
        RskAddress accountAddress = new RskAddress("0000000000000000000000000000000000001234");
        Trie trie = new TrieImpl();

        ExecutionRepository repository = new ExecutionRepository(trie);

        repository.setCode(accountAddress, code);

        byte[] result = repository.getCode(accountAddress);

        Assert.assertNotNull(result);
        Assert.assertArrayEquals(code, result);
    }
}

package co.rsk.db;

import co.rsk.core.Coin;
import co.rsk.core.RskAddress;
import co.rsk.trie.Trie;
import co.rsk.trie.TrieImpl;
import co.rsk.trie.TrieStore;
import co.rsk.trie.TrieStoreImpl;
import org.ethereum.core.AccountState;
import org.ethereum.datasource.HashMapDB;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

/**
 * Created by ajlopez on 13/10/2018.
 */
public class WorldRepositoryTest {
    @Test
    public void getUnknownAccountStateAsNull() {
        Trie trie = new TrieImpl();
        WorldRepository repository = new WorldRepository(trie);

        AccountState accountState = repository.getAccountState(new RskAddress("0000000000000000000000000000000000001234"));

        Assert.assertNull(accountState);
    }

    @Test
    public void getAccountState() {
        RskAddress accountAddress = new RskAddress("0000000000000000000000000000000000001234");
        AccountState accountState = new AccountState(BigInteger.ONE, Coin.ZERO);
        Trie trie = new TrieImpl();
        trie = trie.put(accountAddress.getBytes(), accountState.getEncoded());

        WorldRepository repository = new WorldRepository(trie);

        AccountState result = repository.getAccountState(accountAddress);

        Assert.assertNotNull(result);
        Assert.assertEquals(accountState.getBalance(), result.getBalance());
        Assert.assertEquals(accountState.getNonce(), result.getNonce());
    }
}

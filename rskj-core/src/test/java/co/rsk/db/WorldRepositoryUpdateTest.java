package co.rsk.db;

import co.rsk.core.Coin;
import co.rsk.core.RskAddress;
import co.rsk.trie.Trie;
import co.rsk.trie.TrieImpl;
import co.rsk.trie.TrieStore;
import co.rsk.trie.TrieStoreImpl;
import org.ethereum.datasource.HashMapDB;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class WorldRepositoryUpdateTest {
    private static RskAddress accountAddress = new RskAddress("0000000000000000000000000000000000001234");
    private static Coin amountOne = new Coin(BigInteger.ONE);
    private static Coin amountTwo = new Coin(BigInteger.ONE.add(BigInteger.ONE));
    private static Coin amountTen = new Coin(BigInteger.TEN);

    @Test
    public void incrementNonce() {
        WorldRepository repository = createRepository();

        BigInteger result = repository.incrementAccountNonce(accountAddress);

        Assert.assertNotNull(result);
        Assert.assertEquals(BigInteger.ONE, result);

        BigInteger nonce = repository.getAccountNonce(accountAddress);

        Assert.assertNotNull(nonce);
        Assert.assertEquals(BigInteger.ONE, nonce);
    }

    @Test
    public void addToAccountBalance() {
        WorldRepository repository = createRepository();

        Coin result = repository.addToAccountBalance(accountAddress, amountTen);

        Assert.assertNotNull(result);
        Assert.assertEquals(amountTen, result);

        Coin balance = repository.getAccountBalance(accountAddress);

        Assert.assertNotNull(balance);
        Assert.assertEquals(amountTen, balance);
    }

    @Test
    public void addTwiceToAccountBalance() {
        WorldRepository repository = createRepository();

        repository.addToAccountBalance(accountAddress, amountOne);
        Coin result = repository.addToAccountBalance(accountAddress, amountOne);

        Assert.assertNotNull(result);
        Assert.assertEquals(amountTwo, result);

        Coin balance = repository.getAccountBalance(accountAddress);

        Assert.assertNotNull(balance);
        Assert.assertEquals(amountTwo, balance);
    }

    private static WorldRepository createRepository() {
        TrieStore trieStore = new TrieStoreImpl(new HashMapDB());
        Trie trie = new TrieImpl(trieStore, true);
        return new WorldRepository(trie, trieStore);
    }
}

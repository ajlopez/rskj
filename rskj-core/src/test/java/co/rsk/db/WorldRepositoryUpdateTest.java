package co.rsk.db;

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

    @Test
    public void incrementNonce() {
        TrieStore trieStore = new TrieStoreImpl(new HashMapDB());
        Trie trie = new TrieImpl(trieStore, true);
        WorldRepository repository = new WorldRepository(trie, trieStore);

        BigInteger result = repository.incrementAccountNonce(accountAddress);

        Assert.assertNotNull(result);
        Assert.assertEquals(BigInteger.ONE, result);

        BigInteger nonce = repository.getAccountNonce(accountAddress);

        Assert.assertNotNull(nonce);
        Assert.assertEquals(BigInteger.ONE, nonce);
    }
}

package co.rsk.db;

import co.rsk.core.Coin;
import co.rsk.core.RskAddress;
import co.rsk.trie.Trie;
import co.rsk.trie.TrieImpl;
import co.rsk.trie.TrieStore;
import co.rsk.trie.TrieStoreImpl;
import org.ethereum.core.AccountState;
import org.ethereum.crypto.HashUtil;
import org.ethereum.datasource.HashMapDB;
import org.ethereum.vm.DataWord;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

/**
 * Created by ajlopez on 13/10/2018.
 */
public class WorldRepositoryTest {
    @Test
    public void getUnknownAccountStateAsNull() {
        TrieStore trieStore = new TrieStoreImpl(new HashMapDB());
        Trie trie = new TrieImpl(trieStore, true);
        WorldRepository repository = new WorldRepository(trie, trieStore);

        AccountState accountState = repository.getAccountState(new RskAddress("0000000000000000000000000000000000001234"));

        Assert.assertNull(accountState);
    }

    @Test
    public void getAccountState() {
        RskAddress accountAddress = new RskAddress("0000000000000000000000000000000000001234");
        AccountState accountState = new AccountState(BigInteger.ONE, Coin.ZERO);

        TrieStore trieStore = new TrieStoreImpl(new HashMapDB());
        Trie trie = new TrieImpl(trieStore, true);
        trie = trie.put(accountAddress.getBytes(), accountState.getEncoded());

        WorldRepository repository = new WorldRepository(trie, trieStore);

        AccountState result = repository.getAccountState(accountAddress);

        Assert.assertNotNull(result);
        Assert.assertEquals(accountState.getBalance(), result.getBalance());
        Assert.assertEquals(accountState.getNonce(), result.getNonce());
    }

    @Test
    public void getUnknownAccountCodeAsEmptyByteArray() {
        TrieStore trieStore = new TrieStoreImpl(new HashMapDB());
        Trie trie = new TrieImpl(trieStore, true);
        WorldRepository repository = new WorldRepository(trie, trieStore);

        byte[] code = repository.getCode(new RskAddress("0000000000000000000000000000000000001234"));

        Assert.assertNotNull(code);
        Assert.assertEquals(0, code.length);
    }

    @Test
    public void getNewAccountCodeAsEmptyByteArray() {
        RskAddress accountAddress = new RskAddress("0000000000000000000000000000000000001234");
        AccountState accountState = new AccountState(BigInteger.ONE, Coin.ZERO);
        TrieStore trieStore = new TrieStoreImpl(new HashMapDB());
        Trie trie = new TrieImpl(trieStore, true);
        trie = trie.put(accountAddress.getBytes(), accountState.getEncoded());
        WorldRepository repository = new WorldRepository(trie, trieStore);

        byte[] code = repository.getCode(new RskAddress("0000000000000000000000000000000000001234"));

        Assert.assertNotNull(code);
        Assert.assertEquals(0, code.length);
    }

    @Test
    public void getAccountCode() {
        byte[] code = new byte[] { 0x01, 0x02, 0x03 };
        byte[] codeHash = HashUtil.keccak256(code);
        RskAddress accountAddress = new RskAddress("0000000000000000000000000000000000001234");
        AccountState accountState = new AccountState(BigInteger.ONE, Coin.ZERO);
        accountState.setCodeHash(codeHash);

        TrieStore trieStore = new TrieStoreImpl(new HashMapDB());
        Trie trie = new TrieImpl(trieStore, true);
        trie = trie.put(accountAddress.getBytes(), accountState.getEncoded());
        trie = trie.put(codeHash, code);

        WorldRepository repository = new WorldRepository(trie, trieStore);

        byte[] result = repository.getCode(new RskAddress("0000000000000000000000000000000000001234"));

        Assert.assertNotNull(result);
        Assert.assertArrayEquals(code, result);
    }

    @Test
    public void getHibernatedAccountCode() {
        byte[] code = new byte[] { 0x01, 0x02, 0x03 };
        byte[] codeHash = HashUtil.keccak256(code);
        RskAddress accountAddress = new RskAddress("0000000000000000000000000000000000001234");
        AccountState accountState = new AccountState(BigInteger.ONE, Coin.ZERO);
        accountState.setCodeHash(codeHash);
        accountState.hibernate();

        TrieStore trieStore = new TrieStoreImpl(new HashMapDB());
        Trie trie = new TrieImpl(trieStore, true);
        trie = trie.put(accountAddress.getBytes(), accountState.getEncoded());
        trie = trie.put(codeHash, code);

        WorldRepository repository = new WorldRepository(trie, trieStore);

        byte[] result = repository.getCode(new RskAddress("0000000000000000000000000000000000001234"));

        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.length);
    }

    @Test
    public void getUnknownAccountStorageValueAsNull() {
        TrieStore trieStore = new TrieStoreImpl(new HashMapDB());
        Trie trie = new TrieImpl(trieStore, true);
        WorldRepository repository = new WorldRepository(trie, trieStore);

        DataWord value = repository.getStorageValue(new RskAddress("0000000000000000000000000000000000001234"), DataWord.ONE);

        Assert.assertNull(value);
    }

    @Test
    public void getNewAccountStorageValueAsNull() {
        RskAddress accountAddress = new RskAddress("0000000000000000000000000000000000001234");
        AccountState accountState = new AccountState(BigInteger.ONE, Coin.ZERO);
        TrieStore trieStore = new TrieStoreImpl(new HashMapDB());
        Trie trie = new TrieImpl(trieStore, true);
        trie = trie.put(accountAddress.getBytes(), accountState.getEncoded());
        WorldRepository repository = new WorldRepository(trie, trieStore);

        DataWord value = repository.getStorageValue(new RskAddress("0000000000000000000000000000000000001234"), DataWord.ONE);

        Assert.assertNull(value);
    }

    @Test
    public void getAccountStorageValue() {
        DataWord key = DataWord.ONE;
        RskAddress accountAddress = new RskAddress("0000000000000000000000000000000000001234");
        TrieStore trieStore = new TrieStoreImpl(new HashMapDB());
        Trie storageTrie = new TrieImpl(trieStore, true);
        storageTrie = storageTrie.put(key.getData(), new DataWord(10).getNoLeadZeroesData());

        AccountState accountState = new AccountState(BigInteger.ONE, Coin.ZERO);

        storageTrie.save();
        accountState.setStateRoot(storageTrie.getHash().getBytes());

        Assert.assertNotNull(trieStore.retrieve(storageTrie.getHash().getBytes()));

        Trie worldTrie = new TrieImpl(trieStore, true);
        worldTrie = worldTrie.put(accountAddress.getBytes(), accountState.getEncoded());

        WorldRepository repository = new WorldRepository(worldTrie, trieStore);

        DataWord value = repository.getStorageValue(accountAddress, DataWord.ONE);

        Assert.assertNotNull(value);
        Assert.assertEquals(new DataWord(10), value);
    }
}

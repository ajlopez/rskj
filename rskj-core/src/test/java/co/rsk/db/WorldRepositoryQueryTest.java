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
public class WorldRepositoryQueryTest {
    private static RskAddress accountAddress = new RskAddress("0000000000000000000000000000000000001234");
    private static Coin amountTen = new Coin(BigInteger.TEN);

    @Test
    public void getUnknownAccountCodeAsEmptyByteArray() {
        WorldRepository repository = createRepository();

        byte[] code = repository.getAccountCode(new RskAddress("0000000000000000000000000000000000001234"));

        Assert.assertNotNull(code);
        Assert.assertEquals(0, code.length);
    }

    @Test
    public void getNewAccountCodeAsEmptyByteArray() {
        WorldRepository repository = createRepositoryWithNewAccount();

        byte[] code = repository.getAccountCode(new RskAddress("0000000000000000000000000000000000001234"));

        Assert.assertNotNull(code);
        Assert.assertEquals(0, code.length);
    }

    @Test
    public void getAccountCode() {
        byte[] code = new byte[] { 0x01, 0x02, 0x03 };
        byte[] codeHash = HashUtil.keccak256(code);
        AccountState accountState = new AccountState(BigInteger.ONE, Coin.ZERO);
        accountState.setCodeHash(codeHash);

        TrieStore trieStore = new TrieStoreImpl(new HashMapDB());
        Trie trie = new TrieImpl(trieStore, true);
        trie = trie.put(accountAddress.getBytes(), accountState.getEncoded());
        trie = trie.put(codeHash, code);

        WorldRepository repository = new WorldRepository(trie, trieStore);

        byte[] result = repository.getAccountCode(new RskAddress("0000000000000000000000000000000000001234"));

        Assert.assertNotNull(result);
        Assert.assertArrayEquals(code, result);
    }

    @Test
    public void getHibernatedAccountCode() {
        byte[] code = new byte[] { 0x01, 0x02, 0x03 };
        byte[] codeHash = HashUtil.keccak256(code);
        AccountState accountState = new AccountState(BigInteger.ONE, Coin.ZERO);
        accountState.setCodeHash(codeHash);
        accountState.hibernate();

        TrieStore trieStore = new TrieStoreImpl(new HashMapDB());
        Trie trie = new TrieImpl(trieStore, true);
        trie = trie.put(accountAddress.getBytes(), accountState.getEncoded());
        trie = trie.put(codeHash, code);

        WorldRepository repository = new WorldRepository(trie, trieStore);

        byte[] result = repository.getAccountCode(new RskAddress("0000000000000000000000000000000000001234"));

        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.length);
    }

    @Test
    public void getUnknownAccountStorageValueAsNull() {
        WorldRepository repository = createRepository();

        DataWord value = repository.getStorageValue(new RskAddress("0000000000000000000000000000000000001234"), DataWord.ONE);

        Assert.assertNull(value);
    }

    @Test
    public void getNewAccountStorageValueAsNull() {
        WorldRepository repository = createRepositoryWithNewAccount();

        DataWord value = repository.getStorageValue(accountAddress, DataWord.ONE);

        Assert.assertNull(value);
    }

    @Test
    public void getAccountStorageValue() {
        DataWord key = DataWord.ONE;
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

    @Test
    public void getNewAccountStorageBytesAsNull() {
        WorldRepository repository = createRepositoryWithNewAccount();

        byte[] bytes = repository.getStorageBytes(accountAddress, DataWord.ONE);

        Assert.assertNull(bytes);
    }

    @Test
    public void getAccountStorageBytes() {
        byte[] bytes = new byte[] { 0x01, 0x02, 0x03 };
        DataWord key = DataWord.ONE;
        TrieStore trieStore = new TrieStoreImpl(new HashMapDB());
        Trie storageTrie = new TrieImpl(trieStore, true);
        storageTrie = storageTrie.put(key.getData(), bytes);

        AccountState accountState = new AccountState(BigInteger.ONE, Coin.ZERO);

        storageTrie.save();
        accountState.setStateRoot(storageTrie.getHash().getBytes());

        Assert.assertNotNull(trieStore.retrieve(storageTrie.getHash().getBytes()));

        Trie worldTrie = new TrieImpl(trieStore, true);
        worldTrie = worldTrie.put(accountAddress.getBytes(), accountState.getEncoded());

        WorldRepository repository = new WorldRepository(worldTrie, trieStore);

        byte[] result = repository.getStorageBytes(accountAddress, DataWord.ONE);

        Assert.assertNotNull(result);
        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void getUnknownAccountNonceAsZero() {
        WorldRepository repository = createRepository();

        BigInteger nonce = repository.getAccountNonce(accountAddress);

        Assert.assertNotNull(nonce);
        Assert.assertEquals(BigInteger.ZERO, nonce);
    }

    @Test
    public void getNewAccountNonceAsZero() {
        WorldRepository repository = createRepositoryWithNewAccount();

        BigInteger nonce = repository.getAccountNonce(accountAddress);

        Assert.assertNotNull(nonce);
        Assert.assertEquals(BigInteger.ZERO, nonce);
    }

    @Test
    public void getAccountNonce() {
        WorldRepository repository = createRepositoryWithAccount(BigInteger.ONE, amountTen);

        BigInteger nonce = repository.getAccountNonce(accountAddress);

        Assert.assertNotNull(nonce);
        Assert.assertEquals(BigInteger.ONE, nonce);
    }

    @Test
    public void getUnknownAccountBalanceAsZero() {
        WorldRepository repository = createRepository();

        Coin balance = repository.getAccountBalance(accountAddress);

        Assert.assertNotNull(balance);
        Assert.assertEquals(Coin.ZERO, balance);
    }

    @Test
    public void getNewAccountBalanceAsZero() {
        WorldRepository repository = createRepositoryWithNewAccount();

        Coin balance = repository.getAccountBalance(accountAddress);

        Assert.assertNotNull(balance);
        Assert.assertEquals(Coin.ZERO, balance);
    }

    @Test
    public void getAccountBalance() {
        WorldRepository repository = createRepositoryWithAccount(BigInteger.ONE, amountTen);

        Coin balance = repository.getAccountBalance(accountAddress);

        Assert.assertNotNull(balance);
        Assert.assertEquals(amountTen, balance);
    }

    private static WorldRepository createRepository() {
        TrieStore trieStore = new TrieStoreImpl(new HashMapDB());
        Trie trie = new TrieImpl(trieStore, true);
        return new WorldRepository(trie, trieStore);
    }

    private static WorldRepository createRepositoryWithNewAccount() {
        AccountState accountState = new AccountState();
        TrieStore trieStore = new TrieStoreImpl(new HashMapDB());
        Trie trie = new TrieImpl(trieStore, true);
        trie = trie.put(accountAddress.getBytes(), accountState.getEncoded());
        return new WorldRepository(trie, trieStore);
    }

    private static WorldRepository createRepositoryWithAccount(BigInteger nonce, Coin balance) {
        AccountState accountState = new AccountState(nonce, balance);
        TrieStore trieStore = new TrieStoreImpl(new HashMapDB());
        Trie trie = new TrieImpl(trieStore, true);
        trie = trie.put(accountAddress.getBytes(), accountState.getEncoded());
        return new WorldRepository(trie, trieStore);
    }
}

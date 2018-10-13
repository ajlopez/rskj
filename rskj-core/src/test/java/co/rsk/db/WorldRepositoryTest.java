package co.rsk.db;

import co.rsk.core.Coin;
import co.rsk.core.RskAddress;
import co.rsk.trie.Trie;
import co.rsk.trie.TrieImpl;
import org.ethereum.core.AccountState;
import org.ethereum.crypto.HashUtil;
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
    public void getUnknownAccountCodeAsEmptyByteArray() {
        Trie trie = new TrieImpl();
        WorldRepository repository = new WorldRepository(trie);

        byte[] code = repository.getCode(new RskAddress("0000000000000000000000000000000000001234"));

        Assert.assertNotNull(code);
        Assert.assertEquals(0, code.length);
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

    @Test
    public void getNewAccountCodeAsEmptyByteArray() {
        RskAddress accountAddress = new RskAddress("0000000000000000000000000000000000001234");
        AccountState accountState = new AccountState(BigInteger.ONE, Coin.ZERO);
        Trie trie = new TrieImpl();
        trie = trie.put(accountAddress.getBytes(), accountState.getEncoded());
        WorldRepository repository = new WorldRepository(trie);

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

        Trie trie = new TrieImpl();
        trie = trie.put(accountAddress.getBytes(), accountState.getEncoded());
        trie = trie.put(codeHash, code);

        WorldRepository repository = new WorldRepository(trie);

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

        Trie trie = new TrieImpl();
        trie = trie.put(accountAddress.getBytes(), accountState.getEncoded());
        trie = trie.put(codeHash, code);

        WorldRepository repository = new WorldRepository(trie);

        byte[] result = repository.getCode(new RskAddress("0000000000000000000000000000000000001234"));

        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.length);
    }
}

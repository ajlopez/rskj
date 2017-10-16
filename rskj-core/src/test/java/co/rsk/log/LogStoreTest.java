package co.rsk.log;

import org.junit.Assert;
import org.junit.Test;

import java.util.BitSet;

/**
 * Created by ajlopez on 16/10/2017.
 */
public class LogStoreTest {
    @Test
    public void getEmptyBlockSetForFistBit() {
        LogStore store = new LogStore();

        BitSet set = store.getBlocksSet(0);

        Assert.assertNotNull(set);
        Assert.assertTrue(set.isEmpty());
    }

    @Test
    public void saveBloomFilterForBlockWithOneBit() {
        LogStore store = new LogStore();

        BitSet bloom = BitSet.valueOf(new byte[] { 0x02 });

        store.saveBloom(10, bloom);

        BitSet set = store.getBlocksSet(0);
        Assert.assertNotNull(set);
        Assert.assertTrue(set.isEmpty());

        set = store.getBlocksSet(1);
        Assert.assertNotNull(set);
        Assert.assertFalse(set.isEmpty());
        Assert.assertEquals(1, set.stream().count());
        Assert.assertTrue(set.get(10));
    }
}

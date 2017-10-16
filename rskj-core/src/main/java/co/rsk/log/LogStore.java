package co.rsk.log;

import java.util.BitSet;

/**
 * Created by ajlopez on 16/10/2017.
 */
public class LogStore {
    private BitSet[] blocksByBit = new BitSet[256];

    public BitSet getBlocksSet(int nbit) {
        if (blocksByBit[nbit] == null)
            return new BitSet();

        return blocksByBit[nbit];
    }

    public void saveBloom(int blockNumber, BitSet bloom) {
        bloom.stream().forEach(n -> {
            if (blocksByBit[n] == null)
                blocksByBit[n] = new BitSet(blockNumber + 1000);

            blocksByBit[n].set(blockNumber);
        });
    }
}

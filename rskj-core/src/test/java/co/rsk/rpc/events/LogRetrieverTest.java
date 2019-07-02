/*
 * This file is part of RskJ
 * Copyright (C) 2018 RSK Labs Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package co.rsk.rpc.events;

import co.rsk.logfilter.BlocksBloomStore;
import co.rsk.test.builders.BlockChainBuilder;
import org.ethereum.core.Blockchain;
import org.ethereum.util.RskTestContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by ajlopez on 26/06/2019.
 */
public class LogRetrieverTest {
    @Test
    public void retrieveLogsFromEmptyBlockchain() throws Exception {
        BlocksBloomStore blocksBloomStore = new BlocksBloomStore(2, 0);
        Blockchain blockchain = BlockChainBuilder.ofSize(0);
        LogRetriever logRetriever = new LogRetriever(blockchain, blocksBloomStore);
        LogFilterRequest logFilterRequest = new LogFilterRequest();

        logFilterRequest.fromBlock = "earliest";
        logFilterRequest.toBlock = "latest";

        LogFilter logFilter = LogFilter.fromLogFilterRequest(logFilterRequest, blockchain);

        logRetriever.retrieveHistoricalData(logFilterRequest, logFilter);

        Assert.assertFalse(blocksBloomStore.hasBlockNumber(1));
    }

    @Test
    public void retrieveLogsFromBlockchainWithFourBlocks() throws Exception {
        int nblocks = 10;
        BlocksBloomStore blocksBloomStore = new BlocksBloomStore(2, 0);
        Blockchain blockchain = BlockChainBuilder.ofSize(nblocks);
        Assert.assertEquals(10, blockchain.getBestBlock().getNumber());
        LogRetriever logRetriever = new LogRetriever(blockchain, blocksBloomStore);
        LogFilterRequest logFilterRequest = new LogFilterRequest();

        logFilterRequest.fromBlock = "earliest";
        logFilterRequest.toBlock = "latest";

        LogFilter logFilter = LogFilter.fromLogFilterRequest(logFilterRequest, blockchain);

        logRetriever.retrieveHistoricalData(logFilterRequest, logFilter);

        for (int nblock = 0; nblock < nblocks; nblock++)
            Assert.assertTrue(blocksBloomStore.hasBlockNumber(nblock));

        Assert.assertFalse(blocksBloomStore.hasBlockNumber(nblocks + 1));
    }
}

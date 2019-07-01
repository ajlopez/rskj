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
import org.ethereum.core.Blockchain;
import org.ethereum.util.RskTestContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Angel on 6/26/2019.
 */
public class LogRetrieverTest {
    private Blockchain blockChain;
    private BlocksBloomStore blocksBloomStore;

    @Before
    public void setUp() {
        RskTestContext context = new RskTestContext(new String[0]);
        this.blockChain = context.getBlockchain();
        this.blocksBloomStore = new BlocksBloomStore(2, 0);
    }

    @Test
    public void retrieveLogsFromEmptyBlockchain() throws Exception {
        LogRetriever logRetriever = new LogRetriever(this.blockChain, this.blocksBloomStore);
        LogFilterRequest logFilterRequest = new LogFilterRequest();

        logFilterRequest.fromBlock = "earliest";
        logFilterRequest.toBlock = "latest";

        LogFilter logFilter = LogFilter.fromLogFilterRequest(logFilterRequest, this.blockChain);

        logRetriever.retrieveHistoricalData(logFilterRequest, logFilter);

        Assert.assertFalse(blocksBloomStore.hasBlockNumber(1));
    }
}

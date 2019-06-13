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

import co.rsk.core.bc.BlockChainImpl;
import co.rsk.test.builders.BlockBuilder;
import co.rsk.test.builders.BlockChainBuilder;
import org.ethereum.core.Block;
import org.ethereum.core.Genesis;
import org.ethereum.listener.EthereumListener;
import org.ethereum.rpc.Simples.SimpleEthereum;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ajlopez on 13/06/2019.
 */
public class FilterManagerTest {
    @Test
    public void addListener() {
        SimpleEthereum simpleEthereum = new SimpleEthereum();

        Assert.assertNull(simpleEthereum.getListener());

        FilterManager filterManager = new FilterManager(simpleEthereum);

        Assert.assertNull(filterManager.getFilterEvents(1, false));

        EthereumListener ethereumListener = simpleEthereum.getListener();

        Assert.assertNotNull(ethereumListener);
    }

    @Test
    public void retrieveUnknownFilterAsNull() {
        SimpleEthereum simpleEthereum = new SimpleEthereum();
        FilterManager filterManager = new FilterManager(simpleEthereum);

        Assert.assertNull(filterManager.getFilterEvents(1, false));
    }

    @Test
    public void registerFilterAndRetrieveNoEvents() {
        SimpleEthereum simpleEthereum = new SimpleEthereum();
        FilterManager filterManager = new FilterManager(simpleEthereum);

        NewBlockFilter newBlockFilter = new NewBlockFilter();

        int id = filterManager.registerFilter(newBlockFilter);

        Assert.assertEquals(1, id);

        Object[] events = filterManager.getFilterEvents(1, false);

        Assert.assertNotNull(events);
        Assert.assertEquals(0, events.length);
    }

    @Test
    public void registerAndRemoveFilter() {
        SimpleEthereum simpleEthereum = new SimpleEthereum();
        FilterManager filterManager = new FilterManager(simpleEthereum);

        NewBlockFilter newBlockFilter = new NewBlockFilter();

        int id = filterManager.registerFilter(newBlockFilter);
        filterManager.removeFilter(id);

        Assert.assertNull(filterManager.getFilterEvents(1, false));
    }

    @Test
    public void processNewBlock() {
        SimpleEthereum simpleEthereum = new SimpleEthereum();
        FilterManager filterManager = new FilterManager(simpleEthereum);

        NewBlockFilter newBlockFilter = new NewBlockFilter();

        int id = filterManager.registerFilter(newBlockFilter);

        BlockChainImpl blockChain = new BlockChainBuilder().build();
        Genesis genesis = (Genesis) blockChain.getBestBlock();
        Block block = new BlockBuilder().parent(genesis).build();

        filterManager.newBlockReceived(block);

        Object[] events = filterManager.getFilterEvents(id, false);

        Assert.assertNotNull(events);
        Assert.assertEquals(1, events.length);
        Assert.assertTrue(events[0] instanceof String);

        Assert.assertEquals("0x" + block.getHash().toHexString(), events[0]);
    }
}

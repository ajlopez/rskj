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
}

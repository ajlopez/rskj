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

import co.rsk.core.RskAddress;
import co.rsk.test.builders.AccountBuilder;
import org.ethereum.core.Account;
import org.ethereum.core.Bloom;
import org.ethereum.vm.DataWord;
import org.ethereum.vm.LogInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by ajlopez on 18/01/2018.
 */
public class AddressesTopicsFilterTest {
    @Test
    public void matchAddressExactly() {
        RskAddress address = new AccountBuilder().name("account").build().getAddress();
        RskAddress address2 = new AccountBuilder().name("account2").build().getAddress();

        AddressesTopicsFilter filter = new AddressesTopicsFilter(new RskAddress[] { address }, null);

        LogInfo logInfo = new LogInfo(address.getBytes(), null, null);
        LogInfo logInfo2 = new LogInfo(address2.getBytes(), null, null);

        Assert.assertTrue(filter.matchesExactly(logInfo));
        Assert.assertFalse(filter.matchesExactly(logInfo2));
    }

    @Test
    public void matchEmptyBloomWithAllFilter() {
        AddressesTopicsFilter filter = new AddressesTopicsFilter(new RskAddress[0], null);

        Assert.assertTrue(filter.matchBloom(new Bloom()));
    }

    @Test
    public void noMatchEmptyBloomWithFilterWithAccount() {
        Account account = new AccountBuilder().name("account").build();
        RskAddress address = account.getAddress();

        AddressesTopicsFilter filter = new AddressesTopicsFilter(new RskAddress[] { address }, null);

        Assert.assertFalse(filter.matchBloom(new Bloom()));
    }

    @Test
    public void noMatchEmptyBloomWithFilterWithTopic() {
        Topic topic = createTopic();

        AddressesTopicsFilter filter = new AddressesTopicsFilter(new RskAddress[0], new Topic[][] {{ topic }});

        Assert.assertFalse(filter.matchBloom(new Bloom()));
    }

    @Test
    public void matchAllBloomWithFilterWithTopic() {
        Topic topic = createTopic();

        AddressesTopicsFilter filter = new AddressesTopicsFilter(new RskAddress[0], new Topic[][] {{ topic }});

        Assert.assertTrue(filter.matchBloom(getAllBloom()));
    }

    @Test
    public void matchAllBloomWithFilterWithAccount() {
        Account account = new AccountBuilder().name("account").build();
        RskAddress address = account.getAddress();

        AddressesTopicsFilter filter = new AddressesTopicsFilter(new RskAddress[] { address }, null);

        Assert.assertTrue(filter.matchBloom(getAllBloom()));
    }

    private static Topic createTopic() {
        byte[] bytes = new byte[32];
        Random random = new Random();

        random.nextBytes(bytes);

        return new Topic(bytes);
    }

    private static Bloom getAllBloom() {
        byte[] bytes = new byte[256];

        for (int k = 0; k < bytes.length; k++)
            bytes[k] = (byte)0xff;

        return new Bloom(bytes);
    }

    @Test
    public void firstTopicMatchExactly() {
        Account account = new AccountBuilder().name("account").build();
        Topic topic = createTopic();

        AddressesTopicsFilter filter = new AddressesTopicsFilter(new RskAddress[0], new Topic[][] {{ topic }});

        DataWord dataWordTopic = DataWord.valueOf(topic.getBytes());
        List<DataWord> dataWordTopics = new ArrayList<>();
        dataWordTopics.add(dataWordTopic);

        LogInfo logInfo = new LogInfo(account.getAddress().getBytes(), dataWordTopics, null);

        Assert.assertTrue(filter.matchesExactly(logInfo));
    }

    @Test
    public void firstTopicWithSiblingMatchExactly() {
        Account account = new AccountBuilder().name("account").build();
        Topic topic = createTopic();
        Topic topicb = createTopic();

        AddressesTopicsFilter filter = new AddressesTopicsFilter(new RskAddress[0], new Topic[][] {{ topicb, topic }});

        DataWord dataWordTopic = DataWord.valueOf(topic.getBytes());
        List<DataWord> dataWordTopics = new ArrayList<>();
        dataWordTopics.add(dataWordTopic);

        LogInfo logInfo = new LogInfo(account.getAddress().getBytes(), dataWordTopics, null);

        Assert.assertTrue(filter.matchesExactly(logInfo));
    }

    @Test
    public void secondTopicMatchExactly() {
        Account account = new AccountBuilder().name("account").build();
        Topic topic1 = createTopic();
        Topic topic2 = createTopic();

        AddressesTopicsFilter filter = new AddressesTopicsFilter(new RskAddress[0], new Topic[][] {{ topic1 }, { topic2 }});

        DataWord dataWordTopic1 = DataWord.valueOf(topic1.getBytes());
        DataWord dataWordTopic2 = DataWord.valueOf(topic2.getBytes());

        List<DataWord> dataWordTopics = new ArrayList<>();
        dataWordTopics.add(dataWordTopic1);
        dataWordTopics.add(dataWordTopic2);

        LogInfo logInfo = new LogInfo(account.getAddress().getBytes(), dataWordTopics, null);

        Assert.assertTrue(filter.matchesExactly(logInfo));
    }

    @Test
    public void firstTopicDoNotMatchExactly() {
        Account account = new AccountBuilder().name("account").build();
        Topic topic1 = createTopic();
        Topic topic2 = createTopic();

        AddressesTopicsFilter filter = new AddressesTopicsFilter(new RskAddress[0], new Topic[][] {{ topic1 }, { topic2 }});

        DataWord dataWordTopic2 = DataWord.valueOf(topic2.getBytes());

        List<DataWord> dataWordTopics = new ArrayList<>();
        dataWordTopics.add(dataWordTopic2);

        LogInfo logInfo = new LogInfo(account.getAddress().getBytes(), dataWordTopics, null);

        Assert.assertFalse(filter.matchesExactly(logInfo));
    }

    @Test
    public void accountAddressMatchExactly() {
        Account account = new AccountBuilder().name("account").build();

        AddressesTopicsFilter filter = new AddressesTopicsFilter(new RskAddress[] { account.getAddress() }, new Topic[][] {{}, {}});

        List<DataWord> dataWordTopics = new ArrayList<>();

        LogInfo logInfo = new LogInfo(account.getAddress().getBytes(), dataWordTopics, null);

        Assert.assertTrue(filter.matchesExactly(logInfo));
    }

    @Test
    public void accountAddressDoNotMatchExactly() {
        Account account = new AccountBuilder().name("account").build();
        Account accountb = new AccountBuilder().name("accountb").build();

        AddressesTopicsFilter filter = new AddressesTopicsFilter(new RskAddress[] { account.getAddress() }, new Topic[][] {{}, {}});

        List<DataWord> dataWordTopics = new ArrayList<>();

        LogInfo logInfo = new LogInfo(accountb.getAddress().getBytes(), dataWordTopics, null);

        Assert.assertFalse(filter.matchesExactly(logInfo));
    }
}

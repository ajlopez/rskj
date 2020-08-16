/*
 * This file is part of RskJ
 * Copyright (C) 2017 RSK Labs Ltd.
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
package co.rsk.cli.tools;

import co.rsk.RskContext;
import co.rsk.core.Coin;
import co.rsk.core.types.ints.Uint24;
import co.rsk.trie.NodeReference;
import co.rsk.trie.Trie;
import co.rsk.trie.TrieStore;
import org.ethereum.core.AccountState;
import org.ethereum.core.Block;
import org.ethereum.db.BlockStore;

import java.io.PrintStream;
import java.util.Optional;

/**
 * The entry point for get supply CLI tool
 * This is an experimental/unsupported tool
 */
public class GetSupply {
    public static void main(String[] args) {
        RskContext ctx = new RskContext(args);
        BlockStore blockStore = ctx.getBlockStore();
        TrieStore trieStore = ctx.getTrieStore();

        execute(args, blockStore, trieStore, System.out);
    }

    public static void execute(String[] args, BlockStore blockStore, TrieStore trieStore, PrintStream writer) {
        long blockNumber = Long.parseLong(args[0]);

        Block block = blockStore.getChainBlockByNumber(blockNumber);

        Optional<Trie> otrie = trieStore.retrieve(block.getStateRoot());

        if (!otrie.isPresent()) {
            return;
        }

        Trie trie = otrie.get();

        Coin supply = processTrie(trie);

        writer.println(supply.toString());
    }

    private static Coin processTrie(Trie trie) {
        byte[] value = trie.getValue();

        if (value != null) {
            AccountState accountState = new AccountState(value);

            return accountState.getBalance();
        }

        Coin supply = Coin.ZERO;

        NodeReference leftReference = trie.getLeft();

        if (!leftReference.isEmpty()) {
            Optional<Trie> left = leftReference.getNode();

            if (left.isPresent()) {
                Trie leftTrie = left.get();

                if (!leftReference.isEmbeddable()) {
                    supply = supply.add(processTrie(leftTrie));
                }
            }
        }

        NodeReference rightReference = trie.getRight();

        if (!rightReference.isEmpty()) {
            Optional<Trie> right = rightReference.getNode();

            if (right.isPresent()) {
                Trie rightTrie = right.get();

                if (!rightReference.isEmbeddable()) {
                    supply = supply.add(processTrie(rightTrie));
                }
            }
        }

        return supply;
    }
}

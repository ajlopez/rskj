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

import co.rsk.logfilter.BlocksBloom;
import co.rsk.logfilter.BlocksBloomStore;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.core.Bloom;
import org.ethereum.rpc.Web3Impl;

/**
 * Created by ajlopez on 24/04/2019.
 */
public class LogRetriever {
    private LogRetriever() {

    }

    public static void retrieveHistoricalData(LogFilterRequest fr, Blockchain blockchain, LogFilter filter, BlocksBloomStore blocksBloomStore) throws Exception {
        Block blockFrom = isBlockWord(fr.fromBlock) ? null : Web3Impl.getBlockByNumberOrStr(fr.fromBlock, blockchain);
        Block blockTo = isBlockWord(fr.toBlock) ? null : Web3Impl.getBlockByNumberOrStr(fr.toBlock, blockchain);

        if (blockFrom == null && "earliest".equalsIgnoreCase(fr.fromBlock)) {
            blockFrom = blockchain.getBlockByNumber(0);
        }

        if (blockFrom != null) {
            // need to add historical data
            blockTo = blockTo == null ? blockchain.getBestBlock() : blockTo;

            processBlocks(blockFrom.getNumber(), blockTo.getNumber(), filter, blockchain, blocksBloomStore);
        }
        else if ("latest".equalsIgnoreCase(fr.fromBlock)) {
            filter.onBlock(blockchain.getBestBlock());
        }
    }

    private static void processBlocks(long fromBlockNumber, long toBlockNumber, LogFilter filter, Blockchain blockchain, BlocksBloomStore blocksBloomStore) {
        BlocksBloom auxiliaryBlocksBloom = null;
        long bestBlockNumber = blockchain.getBestBlock().getNumber();

        for (long blockNum = fromBlockNumber; blockNum <= toBlockNumber; blockNum++) {
            boolean isConfirmedBlock = blockNum <= bestBlockNumber - blocksBloomStore.getNoConfirmations();

            if (isConfirmedBlock) {
                if (blocksBloomStore.firstNumberInRange(blockNum) == blockNum) {
                    if (blocksBloomStore.hasBlockNumber(blockNum)) {
                        BlocksBloom blocksBloom = blocksBloomStore.getBlocksBloomByNumber(blockNum);

                        if (!filter.matchBloom(blocksBloom.getBloom())) {
                            blockNum = blocksBloomStore.lastNumberInRange(blockNum);
                            continue;
                        }
                    }

                    auxiliaryBlocksBloom = new BlocksBloom();
                }

                Block block = blockchain.getBlockByNumber(blockNum);

                if (auxiliaryBlocksBloom != null) {
                    auxiliaryBlocksBloom.addBlockBloom(blockNum, new Bloom(block.getLogBloom()));
                }

                if (auxiliaryBlocksBloom != null && blocksBloomStore.lastNumberInRange(blockNum) == blockNum) {
                    blocksBloomStore.setBlocksBloom(auxiliaryBlocksBloom);
                }

                filter.onBlock(block);
            }
            else {
                filter.onBlock(blockchain.getBlockByNumber(blockNum));
            }
        }
    }

    private static boolean isBlockWord(String id) {
        return "latest".equalsIgnoreCase(id) || "pending".equalsIgnoreCase(id) || "earliest".equalsIgnoreCase(id);
    }
}

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

package co.rsk;

import co.rsk.config.MiningConfig;
import co.rsk.config.RskSystemProperties;
import co.rsk.core.NetworkStateExporter;
import co.rsk.core.Rsk;
import co.rsk.core.RskFactory;
import co.rsk.core.bc.BlockValidatorImpl;
import co.rsk.metrics.HashRateCalculator;
import co.rsk.mine.MinerClient;
import co.rsk.mine.MinerServer;
import co.rsk.mine.TxBuilder;
import co.rsk.mine.TxBuilderEx;
import co.rsk.net.*;
import co.rsk.net.discovery.UDPServer;
import co.rsk.net.sync.SyncConfiguration;
import co.rsk.rpc.CorsConfiguration;
import co.rsk.validators.BlockParentDependantValidationRule;
import co.rsk.validators.BlockValidationRule;
import co.rsk.validators.BlockValidator;
import org.ethereum.cli.CLIInterface;
import org.ethereum.config.CommonConfig;
import org.ethereum.config.DefaultConfig;
import org.ethereum.core.*;
import org.ethereum.db.BlockStore;
import org.ethereum.db.ReceiptStore;
import org.ethereum.facade.Ethereum;
import org.ethereum.listener.CompositeEthereumListener;
import org.ethereum.listener.EthereumListener;
import org.ethereum.manager.AdminInfo;
import org.ethereum.manager.WorldManager;
import org.ethereum.manager.WorldManagerImpl;
import org.ethereum.net.client.ConfigCapabilities;
import org.ethereum.net.client.ConfigCapabilitiesImpl;
import org.ethereum.net.server.ChannelManager;
import org.ethereum.net.server.ChannelManagerImpl;
import org.ethereum.rpc.JsonRpcNettyServer;
import org.ethereum.rpc.JsonRpcWeb3FilterHandler;
import org.ethereum.rpc.JsonRpcWeb3ServerHandler;
import org.ethereum.rpc.Web3;
import org.ethereum.sync.SyncPool;
import org.ethereum.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Start {
    private static Logger logger = LoggerFactory.getLogger("start");

    private final Rsk rsk;

    private WorldManager worldManager;
    private Blockchain blockchain;
    private ChannelManager channelManager;
    private CompositeEthereumListener ethereumListener;
    private NodeBlockProcessor blockProcessor;

    private UDPServer udpServer;
    private MinerServer minerServer;
    private MinerClient minerClient;
    private Web3Factory web3Factory;
    private RskSystemProperties rskSystemProperties = RskSystemProperties.CONFIG;

    public static void main(String[] args) throws Exception {
        Start start = new Start();
        start.startNode(args);
    }

    private void createObjects(RskFactory rskFactory, CommonConfig commonConfig, DefaultConfig defaultConfig) {
        this.ethereumListener = new CompositeEthereumListener();

        this.blockchain = createBlockchain(rskFactory, commonConfig, defaultConfig, this.ethereumListener, this.rskSystemProperties);
        BlockStore blockStore = this.blockchain.getBlockStore();
        PendingState pendingState = this.blockchain.getPendingState();
        Repository repository = this.blockchain.getRepository();
        NetworkStateExporter networkStateExporter = defaultConfig.networkStateExporter(repository);
        MiningConfig miningConfig = defaultConfig.miningConfig(this.rskSystemProperties);
        HashRateCalculator hashRateCalculator = defaultConfig.hashRateCalculator(this.rskSystemProperties, blockStore, miningConfig);
        ConfigCapabilitiesImpl configCapabilities = new ConfigCapabilitiesImpl();
        configCapabilities.init();

        SyncPool syncPool = rskFactory.getSyncPool(
                this.ethereumListener,
                this.blockchain,
                this.rskSystemProperties,
                null, // node manager
                null // sync pool peer client factory
                );

        this.channelManager = createChannelManager(syncPool);

        BlockNodeInformation blockNodeInformation = new BlockNodeInformation();
        SyncConfiguration syncConfiguration = rskFactory.getSyncConfiguration(this.rskSystemProperties);

        co.rsk.net.BlockStore orphansBlockStore = new co.rsk.net.BlockStore();

        BlockSyncService blockSyncService = rskFactory.getBlockSyncService(
                blockchain,
                orphansBlockStore,
                blockNodeInformation,
                syncConfiguration
        );

        this.blockProcessor = rskFactory.getNodeBlockProcessor(
                blockchain,
                orphansBlockStore,
                blockNodeInformation,
                blockSyncService,
                syncConfiguration
        );

        this.worldManager = new WorldManagerImpl(
                this.blockchain,
                blockStore,
                pendingState,
                repository,
                networkStateExporter,
                hashRateCalculator,
                configCapabilities,
                this.rskSystemProperties,
                this.channelManager,
                this.ethereumListener,
                this.blockProcessor
        );
    }

    private static Blockchain createBlockchain(RskFactory rskFactory, CommonConfig commonConfig, DefaultConfig defaultConfig, EthereumListener ethereumListener, RskSystemProperties rskSystemProperties) {
        Repository repository = commonConfig.repository();
        BlockStore blockStore = defaultConfig.blockStore();
        ReceiptStore receiptStore = defaultConfig.receiptStore();

        AdminInfo adminInfo = new AdminInfo();
        adminInfo.init();

        BlockParentDependantValidationRule blockParentValidationRule = null;
        BlockValidationRule blockValidationRule = null;

        BlockValidator blockValidator = new BlockValidatorImpl(blockStore, blockParentValidationRule, blockValidationRule);

        return rskFactory.getBlockchain(
                repository,
                blockStore,
                receiptStore,
                ethereumListener,
                adminInfo,
                blockValidator,
                rskSystemProperties
        );
    }

    private static ChannelManager createChannelManager(SyncPool syncPool) {
        ChannelManagerImpl channelManager = new ChannelManagerImpl(syncPool);

        channelManager.init();

        return channelManager;
    }

    public Start() {
        RskFactory rskFactory = new RskFactory();
        CommonConfig commonConfig = new CommonConfig();
        DefaultConfig defaultConfig = new DefaultConfig();
        defaultConfig.init();

        this.createObjects(rskFactory, commonConfig, defaultConfig);
        this.rsk = rskFactory.getRsk(
                this.worldManager,
                this.blockchain,
                this.channelManager,
                null, // peer server
                new ProgramInvokeFactoryImpl(),
                this.blockchain.getPendingState(),
                this.rskSystemProperties,
                this.ethereumListener,
                this.blockchain.getReceiptStore(),
                null, // peer scoring manager
                this.blockProcessor,
                null, // node message handler
                this.rskSystemProperties,
                this.blockchain.getRepository()
        );
    }

    public void startNode(String[] args) throws Exception {
        logger.info("Starting RSK");

        CLIInterface.call(RskSystemProperties.CONFIG, args);

        if (!"".equals(RskSystemProperties.CONFIG.blocksLoader())) {
            RskSystemProperties.CONFIG.setSyncEnabled(Boolean.FALSE);
            RskSystemProperties.CONFIG.setDiscoveryEnabled(Boolean.FALSE);
        }

        Metrics.registerNodeID(RskSystemProperties.CONFIG.nodeId());

        if (RskSystemProperties.CONFIG.simulateTxs()) {
            enableSimulateTxs(rsk);
        }

        if (RskSystemProperties.CONFIG.simulateTxsEx()) {
            enableSimulateTxsEx(rsk, worldManager);
        }

        if (RskSystemProperties.CONFIG.isRpcEnabled()) {
            logger.info("RPC enabled");
            enableRpc();
        }
        else {
            logger.info("RPC disabled");
        }

        if (RskSystemProperties.CONFIG.waitForSync()) {
            waitRskSyncDone(rsk);
        }

        if (RskSystemProperties.CONFIG.minerServerEnabled()) {
            minerServer.start();

            if (RskSystemProperties.CONFIG.minerClientEnabled()) {
                minerClient.mine();
            }
        }

        if (RskSystemProperties.CONFIG.peerDiscovery()) {
            enablePeerDiscovery();
        }
    }

    private void enablePeerDiscovery() {
        udpServer.start();
    }

    private void enableRpc() throws InterruptedException {
        Web3 web3Service = web3Factory.newInstance();
        JsonRpcWeb3ServerHandler serverHandler = new JsonRpcWeb3ServerHandler(web3Service, rskSystemProperties.getRpcModules());
        JsonRpcWeb3FilterHandler filterHandler = new JsonRpcWeb3FilterHandler(rskSystemProperties.corsDomains());
        new JsonRpcNettyServer(
            rskSystemProperties.rpcPort(),
            rskSystemProperties.soLingerTime(),
            true,
            new CorsConfiguration(rskSystemProperties.corsDomains()),
            filterHandler,
            serverHandler
        ).start();
    }

    private void enableSimulateTxs(Rsk rsk) {
        new TxBuilder(rsk, this.worldManager.getNodeBlockProcessor(), this.blockchain.getRepository()).simulateTxs();
    }

    private void enableSimulateTxsEx(Rsk rsk, WorldManager worldManager) {
        new TxBuilderEx().simulateTxs(rsk, worldManager, this.rskSystemProperties, this.blockchain.getRepository());
    }

    private void waitRskSyncDone(Rsk rsk) throws InterruptedException {
        while (rsk.isBlockchainEmpty() || rsk.hasBetterBlockToSync() || rsk.isPlayingBlocks()) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e1) {
                logger.trace("Wait sync done was interrupted", e1);
                throw e1;
            }
        }
    }

    public interface Web3Factory {
        Web3 newInstance();
    }
}

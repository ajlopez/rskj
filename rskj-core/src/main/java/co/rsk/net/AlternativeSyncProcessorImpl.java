package co.rsk.net;

import co.rsk.core.bc.BlockChainStatus;
import co.rsk.crypto.Keccak256;
import co.rsk.net.messages.*;
import co.rsk.net.sync.PeersInformation;
import co.rsk.net.sync.SyncConfiguration;
import io.netty.util.internal.ConcurrentSet;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.BlockIdentifier;
import org.ethereum.core.Blockchain;
import org.ethereum.crypto.HashUtil;
import org.ethereum.net.server.ChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class AlternativeSyncProcessorImpl implements SyncProcessor {
    private static final Logger logger = LoggerFactory.getLogger("syncprocessor");

    private final Blockchain blockchain;
    private final PeersInformation peersInformation;
    private final ChannelManager channelManager;
    private final SyncConfiguration syncConfiguration;

    private Set<Keccak256> sent = new ConcurrentSet<>();

    private long messageId;
    private boolean paused;

    public AlternativeSyncProcessorImpl(Blockchain blockchain,
                                    SyncConfiguration syncConfiguration,
                                    ChannelManager channelManager,
                                    PeersInformation peersInformation) {
        this.blockchain = blockchain;
        this.syncConfiguration = syncConfiguration;
        this.peersInformation = peersInformation;
        this.channelManager = channelManager;
    }

    @Override
    public void processQueueSize(int qsize) {
        if (qsize > 300 && !paused) {
            paused = true;
        }

        if (qsize < 20 && paused) {
            paused = false;
        }
    }

    @Override
    public void processStatus(MessageChannel sender, Status status) {
        logger.debug("Receiving syncState from node {} block {} {}", sender.getPeerNodeID(), status.getBestBlockNumber(), HashUtil.shortHash(status.getBestBlockHash()));
        peersInformation.registerPeer(sender.getPeerNodeID()).setStatus(status);

        BlockChainStatus currentStatus = this.blockchain.getStatus();

        if (currentStatus.getTotalDifficulty().compareTo(status.getTotalDifficulty()) >=0) {
            return;
        }

        if (paused) {
            return;
        }

        long number = currentStatus.getBestBlockNumber();

        MessageWithId message = new SkeletonRequestMessage(messageId++, Math.max(1, number - syncConfiguration.getChunkSize()));

        this.channelManager.sendMessageTo(sender.getPeerNodeID(), message);
    }

    @Override
    public void processSkeletonResponse(MessageChannel peer, SkeletonResponseMessage skeletonResponse) {
        if (paused) {
            return;
        }

        int nsent = 0;

        List<BlockIdentifier> blockIdentifiers = skeletonResponse.getBlockIdentifiers();
        long number = this.blockchain.getStatus().getBestBlock().getNumber();

        for (BlockIdentifier blockIdentifier: blockIdentifiers) {
            byte[] blockHash = blockIdentifier.getHash();
            Keccak256 hash = new Keccak256(blockHash);

            if (blockIdentifier.getNumber() < number && this.blockchain.hasBlockInSomeBlockchain(blockHash)) {
                continue;
            }

            Message message = new BlockHeadersRequestMessage(this.messageId++, blockHash, this.syncConfiguration.getChunkSize());

            this.channelManager.sendMessageTo(peer.getPeerNodeID(), message);

            nsent++;

            if (nsent >= 3) {
                break;
            }
        }
    }

    @Override
    public void processBlockHashResponse(MessageChannel peer, BlockHashResponseMessage blockHashResponse) {

    }

    @Override
    public void processBlockHeadersResponse(MessageChannel peer, BlockHeadersResponseMessage blockHeadersResponse) {
        long number = this.blockchain.getStatus().getBestBlock().getNumber();
        List<BlockHeader> headers = blockHeadersResponse.getBlockHeaders();

        for (int k = headers.size(); k-- > 0;) {
            BlockHeader header = headers.get(k);

            Keccak256 hash = header.getHash();

            byte[] blockHash = hash.getBytes();

            if (header.getNumber() < number && this.blockchain.hasBlockInSomeBlockchain(blockHash)) {
                continue;
            }

            Message message = new GetBlockMessage(blockHash);

            this.channelManager.sendMessageTo(peer.getPeerNodeID(), message);
        }
    }

    @Override
    public void processBodyResponse(MessageChannel peer, BodyResponseMessage message) {

    }

    @Override
    public void processNewBlockHash(MessageChannel peer, NewBlockHashMessage message) {

    }

    @Override
    public void processBlockResponse(MessageChannel peer, BlockResponseMessage message) {

    }

    @Override
    public Set<NodeID> getKnownPeersNodeIDs() {
        return this.peersInformation.knownNodeIds();
    }

    @Override
    public void onTimePassed(Duration timePassed) {

    }

    @Override
    public void stopSyncing() {

    }
}

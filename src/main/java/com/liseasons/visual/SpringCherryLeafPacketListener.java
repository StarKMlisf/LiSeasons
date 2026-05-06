package com.liseasons.visual;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChunkData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import com.liseasons.LISeasonsPlugin;
import com.liseasons.season.Season;
import com.liseasons.season.SeasonState;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public final class SpringCherryLeafPacketListener implements PacketListener {
    private final LISeasonsPlugin plugin;
    private final WrappedBlockState cherryLeavesState;

    public SpringCherryLeafPacketListener(LISeasonsPlugin plugin) {
        this.plugin = plugin;
        this.cherryLeavesState = WrappedBlockState.getDefaultState(StateTypes.CHERRY_LEAVES);
    }

    @Override
    public void onPacketSend(@NonNull PacketSendEvent event) {
        if (!this.plugin.getLiConfig().visualEffectsConfig().springCherryLeafEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        SeasonState state = this.plugin.getSeasonManager().getState(player.getWorld());
        if (state == null || state.season() != Season.SPRING) {
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
            handleBlockChange(event);
        } else if (event.getPacketType() == PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
            handleMultiBlockChange(event);
        } else if (event.getPacketType() == PacketType.Play.Server.CHUNK_DATA) {
            handleChunkData(event);
        }
    }

    private void handleBlockChange(PacketSendEvent event) {
        WrapperPlayServerBlockChange packet = new WrapperPlayServerBlockChange(event);

        WrappedBlockState blockState = packet.getBlockState();
        if (blockState.getType() == StateTypes.OAK_LEAVES) {
            packet.setBlockState(this.cherryLeavesState);
        }
    }

    private void handleMultiBlockChange(PacketSendEvent event) {
        WrapperPlayServerMultiBlockChange packet = new WrapperPlayServerMultiBlockChange(event);

        WrapperPlayServerMultiBlockChange.EncodedBlock[] blocks = packet.getBlocks();
        if (blocks == null) {
            return;
        }

        boolean modified = false;
        for (WrapperPlayServerMultiBlockChange.EncodedBlock block : blocks) {
            WrappedBlockState blockState = block.getBlockState(
                com.github.retrooper.packetevents.PacketEvents.getAPI().getServerManager().getVersion().toClientVersion()
            );
            if (blockState.getType() == StateTypes.OAK_LEAVES) {
                block.setBlockState(this.cherryLeavesState);
                modified = true;
            }
        }

        if (modified) {
            packet.setBlocks(blocks);
        }
    }

    private void handleChunkData(PacketSendEvent event) {
        WrapperPlayServerChunkData packet = new WrapperPlayServerChunkData(event);

        Column column = packet.getColumn();

        BaseChunk[] chunks = column.getChunks();

        for (int sectionIndex = 0; sectionIndex < chunks.length; sectionIndex++) {
            BaseChunk chunk = chunks[sectionIndex];
            if (chunk == null || chunk.isEmpty()) {
                continue;
            }

            boolean modified = false;
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        WrappedBlockState blockState = chunk.get(x, y, z, false);
                        if (blockState.getType() == StateTypes.OAK_LEAVES) {
                            chunk.set(x, y, z, this.cherryLeavesState);
                            modified = true;
                        }
                    }
                }
            }

            if (modified) {
                chunks[sectionIndex] = chunk;
            }
        }
    }
}
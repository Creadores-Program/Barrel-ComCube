package org.barrelmc.barrel.network.translator;

import com.github.steveice10.mc.classic.protocol.packet.server.*;
import com.github.steveice10.packetlib.packet.Packet;
import lombok.Getter;
import org.barrelmc.barrel.network.translator.interfaces.ComcraftPacketTranslator;
import org.barrelmc.barrel.network.translator.interfaces.ClassicPacketTranslator;
import org.barrelmc.barrel.network.translator.classic.*;
import org.barrelmc.barrel.player.Player;
import com.github.steveice10.comcraft.protocol.packet.client.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PacketTranslatorManager {

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Integer.MAX_VALUE,
            60,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @Getter
    private final Map<Class<? extends Packet>, ClassicPacketTranslator> classicTranslators = new HashMap<>();
    @Getter
    private final Map<Class<? extends Packet>, ComcraftPacketTranslator> comcraftTranslators = new HashMap<>();

    private final Player player;

    public PacketTranslatorManager(Player player) {
        this.player = player;
        this.registerDefaultPackets();
    }

    public void translate(Packet pk) {
        ComcraftPacketTranslator translatorCm = comcraftTranslators.get(pk.getClass());

        if(translatorCm != null){
            threadPoolExecutor.execute(() -> translatorCm.translate(pk, player));
            return;
        }

        ClassicPacketTranslator translator = classicTranslators.get(pk.getClass());

        if (translator != null) {
            threadPoolExecutor.execute(() -> translator.translate(pk, player));
        }
    }

    private void registerDefaultPackets() {
        // Comcraft packets
        comcraftTranslators.put(ClientWorldInfoPacket.class, new org.barrelmc.barrel.network.translator.comcraft.WorldInfoPacket());
        comcraftTranslators.put(ClientPlayerDataPacket.class, new org.barrelmc.barrel.network.translator.comcraft.PlayerDataPacket());
        comcraftTranslators.put(ClientBlockChangePacket.class, new org.barrelmc.barrel.network.translator.comcraft.BlockChangePacket());
        comcraftTranslators.put(ClientChunkDataPacket.class, new org.barrelmc.barrel.network.translator.comcraft.ChunkDataPacket());

        // Classic packets
        classicTranslators.put(ServerPositionRotationPacket.class, new PositionRotationPacket());
        classicTranslators.put(ServerSetBlockPacket.class, new SetBlockPacket());
        classicTranslators.put(ServerExtEntryPacket.class, new ExtEntryPacket());
        classicTranslators.put(ServerExtInfoPacket.class, new ExtInfoPacket());
        classicTranslators.put(ServerCustomBlockSupportLevelPacket.class, new CustomBlockSupportLevelPacket());
        classicTranslators.put(ServerBulkBlockUpdatePacket.class, new BulkBlockUpdatePacket());
        classicTranslators.put(ServerDespawnPlayerPacket.class, new DespawnPlayerPacket());
        classicTranslators.put(ServerIdentificationPacket.class, new IdentificationPacket());
        classicTranslators.put(ServerLevelDataPacket.class, new LevelDataPacket());
        classicTranslators.put(ServerLevelFinalizePacket.class, new LevelFinalizePacket());
        classicTranslators.put(ServerLevelInitializePacket.class, new LevelInitializePacket());
        classicTranslators.put(ServerSpawnPlayerPacket.class, new SpawnPlayerPacket());
    }
}

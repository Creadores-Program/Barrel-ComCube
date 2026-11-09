/*
 * Copyright (c) 2021 BarrelMC Team
 * This project is licensed under the MIT License
 */

package org.barrelmc.barrel.player;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.steveice10.mc.classic.protocol.packet.server.ServerExtEntryPacket;
import com.github.steveice10.mc.classic.protocol.packet.server.ServerLevelDataPacket;
import com.github.steveice10.mc.classic.protocol.packet.server.ServerLevelFinalizePacket;
import com.github.steveice10.mc.classic.protocol.ClassicProtocol;
import com.github.steveice10.comcraft.protocol.packet.server.ServerWorldInfoPacket;
import com.github.steveice10.comcraft.protocol.packet.client.ClientLoginPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.tcp.classic.TcpSessionFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import org.barrelmc.barrel.config.Config;
import org.barrelmc.barrel.math.Vector3;
import org.barrelmc.barrel.network.ClassicPacketHandler;
import org.barrelmc.barrel.network.translator.PacketTranslatorManager;
import org.barrelmc.barrel.server.ProxyServer;
import org.barrelmc.barrel.utils.Utils;
import org.barrelmc.barrel.utils.nukkit.TextFormat;

import java.net.InetSocketAddress;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public class Player extends Vector3 {

    @Getter
    private final Session comcraftSession;
    @Getter
    private Session classicSession;
    @Getter
    private final PacketTranslatorManager packetTranslatorManager;

    @Setter
    @Getter
    private long runtimeEntityId;
    @Getter
    private String username;
    @Getter
    private String classicUsername;

    private final ScheduledExecutorService playerInputExecutor = Executors.newScheduledThreadPool(3);
    
    @Getter
    private final ExecutorService worldThread = Executors.newCachedThreadPool();


    @Getter
    private List<ServerExtEntryPacket> extensionsClassic = new ObjectArrayList<>();

    @Getter
    @Setter
    private boolean comcraftReady = false;

    @Getter
    @Setter
    private ServerLevelFinalizePacket classicFinalize = null;

    public static final int WORLDLEN = 256;

    private static final int WORLDTOTALLEN = WORLDLEN * WORLDLEN * WORLDLEN;

    @Getter
    @Setter
    private ByteBuffer mapClassic = ByteBuffer.allocateDirect(WORLDTOTALLEN);

    @Getter
    @Setter
    private StatusWorld statusWorld = StatusWorld.LOGIN;

    @Getter
    private LevelChunkProcess levelChunkProcess;

    @Getter
    @Setter
    private int customBlocksLevel = 0;

    @Getter
    @Setter
    private int extSize = 0;

    @Getter
    private Map<Long, Entity> entitysUnspawn = new ConcurrentHashMap<>();

    @Getter
    private Map<Long, Entity> entitysSpawned = new ConcurrentHashMap<>();

    @Getter
    private Map<Long, Long> entitysIndex = new ConcurrentHashMap<>();

    public Player(ClientLoginPacket loginPacket, Session comcraftSession) {
        this.packetTranslatorManager = new PacketTranslatorManager(this);
        this.comcraftSession = comcraftSession;
        this.offlineLogin(loginPacket);
    }

    public void sendWorldInfo(){
        ServerWorldInfoPacket pk = new ServerWorldInfoPacket(Math.max(this.classicFinalize.getWidth(), Math.max(this.classicFinalize.getHeight(), this.classicFinalize.getDepth())), (float) this.x, (float) this.y, (float) this.z, (float) this.x, (float) this.y, (float) this.z, this.pitch, this.yaw, ProxyServer.getInstance().getSlots(), false, new com.github.steveice10.comcraft.protocol.data.game.Player[0]);
        this.classicSession.send(pk);
    }

    public void startLevelChunkProcess(){
        this.levelChunkProcess = new LevelChunkProcess();
        levelChunkProcess.player = this;
        if(Utils.containsExt(ProxyServer.getInstance().getExtDatapacks().get(10), extensionsClassic)){
            levelChunkProcess.supportBU = true;
        }
        playerInputExecutor.scheduleAtFixedRate(levelChunkProcess, 0, 800, TimeUnit.MILLISECONDS);
    }
    
    private void offlineLogin(ClientLoginPacket comcraftLoginPacket) {
        this.username = this.classicUsername = comcraftLoginPacket.getUsername();
        Config config = ProxyServer.getInstance().getConfig();
        try {
            Client client = new Client(config.getClassicAddress(), config.getClassicPort(), new ClassicProtocol(this.username, "-", true), new TcpSessionFactory());
            this.classicSession = client.getSession();
            this.classicSession.addListener(new ClassicPacketHandler(this));
            this.classicSession.connect(true);
        } catch (Exception exception) {
            comcraftSession.disconnect("Failed to connect: " + exception);
        }
    }

    public void disconnect(String reason) {
        playerInputExecutor.shutdown();
        worldThread.shutdown();
        try {
            this.classicSession.disconnect(reason);
        } catch (Throwable ignored) {
        }
        this.comcraftSession.disconnect(reason);
        ProxyServer.getInstance().removeClassicPlayer(classicUsername);
        this.mapClassic = null;
        ProxyServer.getInstance().getLogger().info(classicUsername + " disconnected: " + reason);
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);
    }

    public void sendWorld(){
        byte[] compressedMap = new byte[0];
        try{
            ByteBuffer copyW = this.mapClassic.duplicate();
            copyW.clear();
            compressedMap = compressMap(copyW);
        }catch(IOException ex){
            ex.printStackTrace();
            return;
        }
        int offset = 0;
        while(offset < compressedMap.length){
            int length = Math.min(1024, compressedMap.length - offset);
            byte[] chunk = new byte[length];
            System.arraycopy(compressedMap, offset, chunk, 0, length);
            offset += length;
            int percent = (int) ((100L * offset) / compressedMap.length);
            this.getClassicSession().send(new ServerLevelDataPacket(chunk, percent));
            try{
                Thread.sleep(10);
            }catch(InterruptedException ex){
                Thread.currentThread().interrupt();
            }
        }
        this.getClassicSession().send(FINALIZE_WORLD_PK);
    }

    private byte[] compressMap(ByteBuffer mapData) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gos = new GZIPOutputStream(baos)) {
            DataOutputStream dos = new DataOutputStream(gos);
            dos.writeInt(WORLDTOTALLEN);
            dos.flush();
            WritableByteChannel channel = Channels.newChannel(dos);
            channel.write(mapData);
            dos.flush();
            gos.finish();
        }
        return baos.toByteArray();
    }
}
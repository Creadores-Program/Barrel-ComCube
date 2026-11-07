/*
 * Copyright (c) 2021 BarrelMC Team
 * This project is licensed under the MIT License
 */

package org.barrelmc.barrel.server;

import com.github.steveice10.comcraft.protocol.ComcraftConstants;
import com.github.steveice10.comcraft.protocol.ComcraftProtocol;
import com.github.steveice10.mc.classic.protocol.data.game.ExtNames;
import com.github.steveice10.mc.classic.protocol.packet.client.ClientExtEntryPacket;
import com.github.steveice10.mc.classic.protocol.packet.client.ClientExtInfoPacket;
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.tcp.comcraft.TcpSessionFactory;
import com.github.steveice10.packetlib.event.server.ServerAdapter;
import com.github.steveice10.packetlib.event.server.ServerClosedEvent;
import com.github.steveice10.packetlib.event.server.SessionAddedEvent;
import com.github.steveice10.packetlib.event.server.SessionRemovedEvent;
import lombok.Getter;
import org.barrelmc.barrel.config.Config;
import org.barrelmc.barrel.network.ComcraftPacketHandler;
import org.barrelmc.barrel.player.LoginDataConstants;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.utils.FileManager;
import org.barrelmc.barrel.utils.NbtBlockDefinitionRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.yaml.snakeyaml.Yaml;
import org.barrelmc.barrel.utils.Logger;
import org.barrelmc.barrel.utils.nukkit.TextFormat;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ProxyServer {

    @Getter
    private static ProxyServer instance = null;
    @Getter
    private final Map<String, Player> classicPlayers = new ConcurrentHashMap<>();

    @Getter
    private final Path dataPath;

    @Getter
    private Config config;

    @Getter
    private NbtBlockDefinitionRegistry blockDefinitions;

    @Getter
    private Logger logger;

    @Getter
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Getter
    private List<ClientExtEntryPacket> extDatapacks = new ObjectArrayList<>(){{
        add(new ClientExtEntryPacket(1, ExtNames.CUSTOMBLOCKS));//0
        add(new ClientExtEntryPacket(1, ExtNames.HACKCONTROL));//1
        add(new ClientExtEntryPacket(1, ExtNames.BULKBLOCKUPDATE));//2
    }};

    @Getter
    private ClientExtInfoPacket extInfo = new ClientExtInfoPacket("ComcraftML 0.6 | Barrel ComCube 0.1.0", extDatapacks.size());

    public ProxyServer(String dataPath) {
        instance = this;
        this.logger = new Logger(TextFormat.DARK_PURPLE.getAnsiCode()+"BarrelMC");
        this.getLogger().info("Barrel ComCube 0.1.0 Starting...");
        this.dataPath = Paths.get(dataPath);
        if (!initConfig()) {
            this.getLogger().emergency("Config file not found! Terminating...");
            System.exit(1);
        }
        loadBlockDefinitions();
        LoginDataConstants.SERVER_ADDRESS_VAL = this.config.getClassicAddress() + ":" + this.config.getClassicPort();
        startServer();
    }

    private boolean initConfig() {
        File configFile = new File(dataPath.toFile(), "config.yml");
        if (!configFile.exists()) {
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (inputStream == null) {
                    return false;
                }
                Files.createDirectories(configFile.getParentFile().toPath());
                Files.copy(inputStream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                return false;
            }
        }
        try {
            this.config = new Yaml().loadAs(Files.newBufferedReader(configFile.toPath()), Config.class);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private void loadBlockDefinitions() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("block_palette.nbt")) {
            assert inputStream != null;
            try (NBTInputStream nbtInputStream = NbtUtils.createGZIPReader(inputStream)) {
                Object object = nbtInputStream.readTag();
                if (object instanceof NbtMap) {
                    NbtMap blocksTag = (NbtMap) object;
                    blockDefinitions = new NbtBlockDefinitionRegistry(blocksTag.getList("blocks", NbtType.COMPOUND));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load block definitions", e);
        }
    }

    private void startServer() {
        Server server = new Server(this.config.getBindAddress(), this.config.getPort(), ComcraftProtocol.class, new TcpSessionFactory());
        String StopMSG = this.config.getShutdownMessage();
        server.addListener(new ServerAdapter() {
            @Override
            public void serverClosed(ServerClosedEvent event) {
                for(var entry : ProxyServer.getInstance().getClassicPlayers().entrySet()){
                    Player player = entry.getValue();
                    player.disconnect(StopMSG);
                }
                getLogger().info("Server closed.");
            }
            @Override
            public void sessionAdded(SessionAddedEvent event) {
                event.getSession().addListener(new ComcraftPacketHandler());
            }
            @Override
            public void sessionRemoved(SessionRemovedEvent event) {
                String username = event.getSession().getFlag(ComcraftConstants.USERNAME_KEY);
                if (username == null) return;
                if(isClassicPlayer(username)){
                    getPlayerByName(username).disconnect("logged out");
                }
                getLogger().info(username + " logged out");
            }
        });
        getLogger().info("Binding to " + this.config.getBindAddress() + " on port " + this.config.getPort());
        server.bind();
        getLogger().info("BarrelProxy " + TextFormat.GRAY.getAnsiCode() + "Com" + TextFormat.LIGHT_PURPLE.getAnsiCode() + "Cube" + TextFormat.RESET.getAnsiCode() + " is running on [" + this.config.getBindAddress() + ":" + this.config.getPort() + "]");
        getLogger().info("Done!");
    }

    public Player getPlayerByName(String username) {
        return this.classicPlayers.get(username);
    }

    public void addClassicPlayer(Player player) {
        this.classicPlayers.put(player.getClassicUsername(), player);
    }

    public void removeClassicPlayer(String comcraftUsername) {
        this.classicPlayers.remove(comcraftUsername);
    }

    public boolean isClassicPlayer(String username) {
        return this.classicPlayers.containsKey(username);
    }
}

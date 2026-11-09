/*
 * Copyright (c) 2021 BarrelMC Team
 * This project is licensed under the MIT License
 */

package org.barrelmc.barrel.network;

import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.PacketSentEvent;
import com.github.steveice10.packetlib.event.session.DisconnectingEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.server.ProxyServer;

public class ClassicPacketHandler extends SessionAdapter {

    private Player player;

    public ClassicPacketHandler(Player player){
        this.player = player;
    }

    @Override
    public void packetSent(PacketSentEvent event) {
        //System.out.println("Sent Java " + event.getPacket().toString());
    }

    @Override
    public void disconnecting(DisconnectingEvent event){
        if(event.getCause() == null){
            return;
        }
        ProxyServer.getInstance().getLogger().error("An error occurred!", event.getCause());
    }

    @Override
    public void packetReceived(PacketReceivedEvent event) {
        Packet packet = event.getPacket();
        //System.out.println("Received Classic " + packet.toString());
        player.getPacketTranslatorManager().translate(packet);
    }
}

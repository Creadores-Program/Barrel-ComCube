package org.barrelmc.barrel.network.translator.classic;

import com.github.steveice10.mc.classic.protocol.packet.server.ServerLevelFinalizePacket;
import com.github.steveice10.packetlib.packet.Packet;
import org.barrelmc.barrel.network.translator.interfaces.ClassicPacketTranslator;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.player.StatusWorld;

public class LevelFinalizePacket implements ClassicPacketTranslator{
    
    @Override
    public void translate(Packet pk, Player player){
        player.setClassicFinalize((ServerLevelFinalizePacket) pk);
        if(player.getStatusWorld() == StatusWorld.LOGIN && player.isComcraftReady()){
            player.sendWorldInfo();
        }
        //multiworld soon
    }
}
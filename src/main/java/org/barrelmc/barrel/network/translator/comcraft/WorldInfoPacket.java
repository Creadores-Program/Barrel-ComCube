package org.barrelmc.barrel.network.translator.comcraft;

import com.github.steveice10.packetlib.packet.Packet;
import org.barrelmc.barrel.network.translator.interfaces.ComcraftPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class WorldInfoPacket implements ComcraftPacketTranslator{
    
    @Override
    public void translate(Packet pk, Player player){
        player.setComcraftReady(true);
        if(player.getClassicFinalize() != null){
            player.sendWorldInfo();
        }
    }
}
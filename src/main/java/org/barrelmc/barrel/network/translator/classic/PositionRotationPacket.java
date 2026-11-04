package org.barrelmc.barrel.network.translator.classic;
import org.barrelmc.barrel.network.translator.interfaces.ClassicPacketTranslator;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.player.StatusWorld;
import org.barrelmc.barrel.utils.Utils;

import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.comcube.protocol.packet.client.ClientPlayerDataPacket;
import com.github.steveice10.mc.classic.protocol.packet.server.ServerPositionRotationPacket;
import com.github.steveice10.mc.classic.protocol.data.game.PlayerIds;
public class PositionRotationPacket implements ClassicPacketTranslator{
    @Override
    public void translate(Packet pk, Player player) {
        if(player.getStatusWorld() == StatusWorld.LOGIN){
            return;
        }
        ServerPositionRotationPacket packet = (ServerPositionRotationPacket) pk;
    }
}

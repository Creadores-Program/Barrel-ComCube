package org.barrelmc.barrel.network.translator.classic;
import org.barrelmc.barrel.network.translator.interfaces.ClassicPacketTranslator;
import org.barrelmc.barrel.player.Player;

import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.mc.classic.protocol.packet.client.ClientCustomBlockSupportLevelPacket;
import com.github.steveice10.mc.classic.protocol.packet.server.ServerCustomBlockSupportLevelPacket;
public class CustomBlockSupportLevelPacket implements ClassicPacketTranslator{
    @Override
    public void translate(Packet pk, Player player) {
        ServerCustomBlockSupportLevelPacket packet = (ServerCustomBlockSupportLevelPacket) pk;
        player.setCustomBlocksLevel(packet.getSupportLevel());
        player.getClassicSession().send(new ClientCustomBlockSupportLevelPacket(1));
    }
}

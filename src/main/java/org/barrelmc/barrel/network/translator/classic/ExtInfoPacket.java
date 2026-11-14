package org.barrelmc.barrel.network.translator.classic;

import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.mc.classic.protocol.packet.server.ServerExtInfoPacket;
import com.github.steveice10.mc.classic.protocol.packet.client.ClientExtEntryPacket;
import org.barrelmc.barrel.network.translator.interfaces.ClassicPacketTranslator;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.server.ProxyServer;

public class ExtInfoPacket implements ClassicPacketTranslator {

    @Override
    public void translate(Packet pk, Player player){
        ServerExtInfoPacket packet = (ServerExtInfoPacket) pk;
        player.setExtSize((int) packet.getExtensionCount());
        if(player.getExtSize() < 1){
            player.getClassicSession().send(ProxyServer.getInstance().getExtInfo());
            for(ClientExtEntryPacket ext : ProxyServer.getInstance().getExtDatapacks()){
                player.getClassicSession().send(ext);
            }
        }
    }
}

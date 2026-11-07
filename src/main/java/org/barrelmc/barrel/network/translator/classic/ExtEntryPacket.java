package org.barrelmc.barrel.network.translator.classic;

import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.mc.classic.protocol.packet.server.ServerExtEntryPacket;
import com.github.steveice10.mc.classic.protocol.packet.client.ClientExtEntryPacket;
import com.github.steveice10.mc.classic.protocol.data.game.UserType;

import java.util.List;

import org.barrelmc.barrel.network.translator.interfaces.ClassicPacketTranslator;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.server.ProxyServer;
import org.barrelmc.barrel.utils.Utils;

public class ExtEntryPacket implements ClassicPacketTranslator {

    @Override
    public void translate(Packet pk, Player player) {
        ServerExtEntryPacket packet = (ServerExtEntryPacket) pk;
        List<ServerExtEntryPacket> exts = player.getExtensionsClassic();
        synchronized(exts){
            exts.add(packet);
        }
        if(player.getExtSize() > player.getExtensionsClassic().size()){
            return;
        }
        player.getClassicSession().send(ProxyServer.getInstance().getExtInfo());
        for(ClientExtEntryPacket ext : ProxyServer.getInstance().getExtDatapacks()){
            player.getClassicSession().send(ext);
        }
    }
}

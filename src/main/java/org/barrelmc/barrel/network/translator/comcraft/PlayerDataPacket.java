package org.barrelmc.barrel.network.translator.comcraft;

import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.comcraft.protocol.packet.client.ClientPlayerDataPacket;
import com.github.steveice10.comcraft.protocol.packet.server.ServerPlayerDataPacket;
import com.github.steveice10.mc.classic.protocol.packet.client.ClientPositionRotationPacket;

import org.barrelmc.barrel.network.translator.interfaces.ComcraftPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class PlayerDataPacket implements ComcraftPacketTranslator{
    
    @Override
    public void translate(Packet pk, Player player){
        ClientPlayerDataPacket packet = (ClientPlayerDataPacket) pk;
        switch(packet.getAction()){
            case MOVE:
                float x = packet.getX();
                float y = packet.getY();
                float z = packet.getZ();
                boolean resend = false;
                if(x < 0){
                    resend = true;
                    x = 0;
                }
                if(y < 1){
                    if(!resend) resend = true;
                    y = 1;
                }
                if(z < 0){
                    if(!resend) resend = true;
                    z = 0;
                }
                player.setPosition(x, y, z);
                if(resend){
                    player.getComcraftSession().send(new ServerPlayerDataPacket(false, packet.getPlayerId(), x, y, z));
                }
                player.getClassicSession().send(new ClientPositionRotationPacket(x, y, z, player.getYaw(), player.getPitch()));
                break;
        }
    }
}
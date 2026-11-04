package org.barrelmc.barrel.network.translator.comcraft;

import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.comcraft.protocol.packet.client.ClientPlayerDataPacket;
import com.github.steveice10.mc.classic.protocol.packet.client.ClientPositionRotationPacket;

import org.barrelmc.barrel.network.translator.interfaces.ComcraftPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class PlayerDataPacket implements ComcraftPacketTranslator{
    
    @Override
    public void translate(Packet pk, Player player){
        ClientPlayerDataPacket packet = (ClientPlayerDataPacket) pk;
        switch(packet.getAction()){
            case MOVE:
                player.setPosition(packet.getX(), packet.getY(), packet.getZ());
                player.getClassicSession().send(new ClientPositionRotationPacket(packet.getX(), packet.getY(), packet.getZ(), player.getYaw(), player.getPitch()));
                break;
        }
    }
}
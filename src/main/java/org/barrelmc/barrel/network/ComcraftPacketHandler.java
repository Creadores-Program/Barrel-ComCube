package org.barrelmc.barrel.network;

import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.PacketSentEvent;
import com.github.steveice10.packetlib.event.session.DisconnectingEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.comcraft.protocol.packet.client.ClientLoginPacket;
import com.github.steveice10.comcraft.protocol.ComcraftConstants;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.server.ProxyServer;

public class ComcraftPacketHandler extends SessionAdapter {

    private Player player = null;

    @Override
    public void packetSent(PacketSentEvent event) {
        //System.out.println("Sent Comcraft " + event.getPacket().toString());
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
        Session session = event.getSession();
        Packet packet = event.getPacket();
        if (this.player == null) {
            if(packet instanceof ClientLoginPacket){
                ClientLoginPacket loginPacket = (ClientLoginPacket) packet;
                ProxyServer.getInstance().getLogger().info(session.getFlag(ComcraftConstants.USERNAME_KEY) + " logged in");
                new Player(loginPacket, session);
            }
        }else{
            player.getPacketTranslatorManager().translate(packet);
        }
    }
}

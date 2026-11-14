package org.barrelmc.barrel.player;


public class Entity {
    public final String name;
    public final long rEId;
    public final String type;
    public volatile float x, y, z;
    public volatile float yaw, pitch;
    public Entity(String name, long rEId, String type, float x, float y, float z, float pitch, float yaw){
        this.name = name;
        this.rEId = rEId;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }
}

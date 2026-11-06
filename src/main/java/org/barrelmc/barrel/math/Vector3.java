package org.barrelmc.barrel.math;

import lombok.Getter;
import lombok.Setter;

public class Vector3 {

    @Setter
    @Getter
    public double x;
    @Setter
    @Getter
    public double y;
    @Setter
    @Getter
    public double z;
    @Setter
    @Getter
    public float yaw;
    @Setter
    @Getter
    public float pitch;

    public int getFloorX() {
        return (int) Math.floor(this.x);
    }

    public int getFloorY() {
        return (int) Math.floor(this.y);
    }

    public int getFloorZ() {
        return (int) Math.floor(this.z);
    }

    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setLocation(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void setRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }
}

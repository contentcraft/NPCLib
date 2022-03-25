package net.jitse.npclib.impl.workarounds;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;

import java.util.UUID;

public class EntityPlayerAdapter extends EntityPlayer {

    private int oid;

    private UUID profileId;
    private double lx;
    private double ly;
    private double lz;
    private byte yRot;
    private byte xRot;

    public static EntityPlayerAdapter createNil() {
        return new Gson().fromJson("{}", EntityPlayerAdapter.class);
    }

    public EntityPlayerAdapter(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile) {
        super(minecraftserver, worldserver, gameprofile);
    }

    public void setOid(int oid) {
        this.oid = oid;
    }

    @Override
    public int getId() {
        return oid;
    }

    public UUID getProfileId() {
        return profileId;
    }

    public void setProfileId(UUID profileId) {
        this.profileId = profileId;
    }

    public double getLx() {
        return lx;
    }

    public void setLx(double lx) {
        this.lx = lx;
    }

    public double getLy() {
        return ly;
    }

    public void setLy(double ly) {
        this.ly = ly;
    }

    public double getLz() {
        return lz;
    }

    public void setLz(double lz) {
        this.lz = lz;
    }

    public byte getyRot() {
        return yRot;
    }

    public void setyRot(byte yRot) {
        this.yRot = yRot;
    }

    public byte getxRot() {
        return xRot;
    }

    public void setxRot(byte xRot) {
        this.xRot = xRot;
    }


}

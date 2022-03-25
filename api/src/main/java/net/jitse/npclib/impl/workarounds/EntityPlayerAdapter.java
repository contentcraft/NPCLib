package net.jitse.npclib.impl.workarounds;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3D;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Field;
import java.util.UUID;

public class EntityPlayerAdapter extends EntityPlayer {

    private static Field AVField;

    static {
        try {
            AVField = Entity.class.getDeclaredField("av");
            AVField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Objenesis objenesis = new ObjenesisStd();

    private int oid;

    private UUID profileId;
    private double lx;
    private double ly;
    private double lz;
    private byte yRot;
    private byte xRot;

    @Override
    public String getName() {
        return overName;
    }

    public void setOverName(String overName) {
        this.overName = overName;
    }

    private String overName;

    public static EntityPlayerAdapter createNil() {
        return objenesis.newInstance(EntityPlayerAdapter.class);
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

    @Override
    public GameProfile getProfile() {
        return new GameProfile(profileId, "Yesmanpi");
    }

    public void setProfileId(UUID profileId) {
        this.profileId = profileId;
    }

    public void updateLocVec(double x, double y, double z) {
        try {
            AVField.set(this, new Vec3D(0, 0, 0));
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        this.setPositionRaw(x, y, z);
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

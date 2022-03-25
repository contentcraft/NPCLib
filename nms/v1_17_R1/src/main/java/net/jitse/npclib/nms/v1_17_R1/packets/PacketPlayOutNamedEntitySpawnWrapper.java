/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.nms.v1_17_R1.packets;

import java.util.UUID;

import net.jitse.npclib.nms.v1_17_R1.workarounds.EntityPlayerAdapter;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import org.bukkit.Location;

import com.comphenix.tinyprotocol.Reflection;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutNamedEntitySpawnWrapper {

    public PacketPlayOutNamedEntitySpawn create(UUID uuid, Location location, int entityId) {
        EntityPlayerAdapter epa = EntityPlayerAdapter.createNil();
        epa.setOid(entityId);
        epa.setyRot((byte) ((int) (location.getYaw() * 256.0F / 360.0F)));
        epa.setxRot((byte) ((int) (location.getPitch() * 256.0F / 360.0F)));

        epa.setLx(location.getX());
        epa.setLy(location.getY());
        epa.setLz(location.getZ());
        epa.setProfileId(uuid);

        PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawn(epa);

        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "a", int.class)
                .set(packetPlayOutNamedEntitySpawn, entityId);
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "b", UUID.class)
                .set(packetPlayOutNamedEntitySpawn, uuid);

        return packetPlayOutNamedEntitySpawn;
    }
}

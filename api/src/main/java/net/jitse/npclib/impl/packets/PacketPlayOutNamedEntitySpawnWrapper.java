/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.impl.packets;

import java.util.UUID;

import net.jitse.npclib.impl.workarounds.EntityPlayerAdapter;
import net.jitse.npclib.utilities.Reflection;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import org.bukkit.Location;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutNamedEntitySpawnWrapper {

    public PacketPlayOutNamedEntitySpawn create(UUID uuid, Location location, int entityId) {
        EntityPlayerAdapter epa = EntityPlayerAdapter.createNil();
        epa.setOid(entityId);
        epa.setyRot(location.getYaw());
        epa.setxRot(location.getPitch());

        epa.setLx(location.getX());
        epa.setLy(location.getY());
        epa.setLz(location.getZ());
        epa.updateLocVec(location.getX(), location.getY(), location.getZ());
        epa.setProfileId(uuid);

        PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawn(epa);

        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "a", int.class)
                .set(packetPlayOutNamedEntitySpawn, entityId);
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "b", UUID.class)
                .set(packetPlayOutNamedEntitySpawn, uuid);

        return packetPlayOutNamedEntitySpawn;
    }
}

/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.impl.packets;

import net.jitse.npclib.impl.workarounds.EntityPlayerAdapter;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import org.bukkit.Location;


/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutEntityHeadRotationWrapper {

    public PacketPlayOutEntityHeadRotation create(Location location, int entityId) {
        EntityPlayerAdapter epa = EntityPlayerAdapter.createNil();
        epa.setOid(entityId);
        PacketPlayOutEntityHeadRotation packetPlayOutEntityHeadRotation = new PacketPlayOutEntityHeadRotation(
                epa,
                (byte) ((int) location.getYaw() * 256.0F / 360.0F)
        );

        return packetPlayOutEntityHeadRotation;
    }
}

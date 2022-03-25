/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.nms.v1_17_R1.packets;

import java.util.Collection;
import java.util.Collections;

import com.comphenix.tinyprotocol.Reflection;
import com.google.gson.Gson;
import net.jitse.npclib.nms.v1_17_R1.workarounds.WrappedScoreaBoardTeam;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutScoreboardTeamWrapper {

    public PacketPlayOutScoreboardTeam createRegisterTeam(String name) {
        WrappedScoreaBoardTeam wsbt = WrappedScoreaBoardTeam.createNil();
        wsbt.setOverwrittenName(name);

        return PacketPlayOutScoreboardTeam.a(wsbt);
    }

    public PacketPlayOutScoreboardTeam createUnregisterTeam(String name) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new Gson().fromJson("{}", PacketPlayOutScoreboardTeam.class);

        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "h", int.class)
                .set(packetPlayOutScoreboardTeam, 1);
        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "i", String.class)
                .set(packetPlayOutScoreboardTeam, name);

        return packetPlayOutScoreboardTeam;
    }
}

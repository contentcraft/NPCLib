/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.impl.packets;

import com.google.gson.Gson;
import net.jitse.npclib.impl.workarounds.WrappedScoreaBoardTeam;
import net.jitse.npclib.utilities.Reflection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeamBase;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutScoreboardTeamWrapper {

    public Packet[] createRegisterTeam(String name, String entityname) {
        WrappedScoreaBoardTeam wsbt = WrappedScoreaBoardTeam.createNil();
        wsbt.setOverwrittenName(name);
        wsbt.setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.b);

        return new Packet[] {PacketPlayOutScoreboardTeam.a(wsbt, true), PacketPlayOutScoreboardTeam.a(wsbt, entityname, PacketPlayOutScoreboardTeam.a.a)};
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

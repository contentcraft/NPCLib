/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.impl.packets;

import java.util.Collections;
import java.util.List;

import com.mojang.authlib.GameProfile;
import net.jitse.npclib.impl.workarounds.EntityPlayerAdapter;
import net.jitse.npclib.utilities.Reflection;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.world.level.EnumGamemode;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutPlayerInfoWrapper {

    public PacketPlayOutPlayerInfo create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction action, GameProfile gameProfile, String name) {
        PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo(action);

        PacketPlayOutPlayerInfo.PlayerInfoData data = new PacketPlayOutPlayerInfo.PlayerInfoData(
                gameProfile,
                0,
                EnumGamemode.b,
                IChatBaseComponent.ChatSerializer.b("{\"text\":\"[NPC] " + name + "\",\"color\":\"dark_gray\"}")
        );

        Reflection.FieldAccessor<List> fieldAccessor = Reflection.getField(packetPlayOutPlayerInfo.getClass(), "b", List.class);
        fieldAccessor.set(packetPlayOutPlayerInfo, Collections.singletonList(data));

        return packetPlayOutPlayerInfo;
    }
}

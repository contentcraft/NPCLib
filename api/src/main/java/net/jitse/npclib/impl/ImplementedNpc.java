package net.jitse.npclib.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import net.jitse.npclib.api.state.NPCSlot;
import net.jitse.npclib.hologram.Hologram;
import net.jitse.npclib.internal.NPCBase;
import net.jitse.npclib.impl.packets.*;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EnumItemSlot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;

/**
 * @author Jitse Boonstra
 */
public class ImplementedNpc extends NPCBase {

    private PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn;
    private Packet[] packetPlayOutScoreboardTeamRegister;
    private PacketPlayOutPlayerInfo packetPlayOutPlayerInfoAdd, packetPlayOutPlayerInfoRemove;
    private PacketPlayOutEntityHeadRotation packetPlayOutEntityHeadRotation;
    private PacketPlayOutEntityDestroy packetPlayOutEntityDestroy;
    private PacketPlayOutAnimation packetPlayOutAnimation;

    public ImplementedNpc(NPCLib instance, List<String> lines) {
        super(instance, lines);
    }

    @Override
    public Hologram getHologram(Player player) {
        Hologram hologram = super.getHologram(player);
        if (hologram == null)
            hologram = new Hologram(location.clone().add(0, 0.5, 0), getText(player));
        playerHologram.put(player.getUniqueId(), hologram);
        return hologram;
    }

    @Override
    public void createPackets() {
        PacketPlayOutPlayerInfoWrapper packetPlayOutPlayerInfoWrapper = new PacketPlayOutPlayerInfoWrapper();

        // Packets for spawning the NPC:
        this.packetPlayOutScoreboardTeamRegister = new PacketPlayOutScoreboardTeamWrapper()
                .createRegisterTeam(name, name); // First packet to send.



        this.packetPlayOutPlayerInfoAdd = packetPlayOutPlayerInfoWrapper
                .create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, gameProfile, name); // Second packet to send.

        this.packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawnWrapper()
                .create(uuid, location, entityId); // Third packet to send.

        this.packetPlayOutEntityHeadRotation = new PacketPlayOutEntityHeadRotationWrapper()
                .create(location, entityId); // Fourth packet to send.

        this.packetPlayOutPlayerInfoRemove = packetPlayOutPlayerInfoWrapper
                .create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e, gameProfile, name); // Fifth packet to send (delayed).

        // Packet for destroying the NPC:
        this.packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(entityId); // First packet to send.
    }

    @Override
    public void sendShowPackets(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().b;

        if (hasTeamRegistered.add(player.getUniqueId()))
            for (Packet packet : packetPlayOutScoreboardTeamRegister) {
                playerConnection.sendPacket(packet);
            }


        playerConnection.sendPacket(packetPlayOutPlayerInfoAdd);
        playerConnection.sendPacket(packetPlayOutNamedEntitySpawn);
        playerConnection.sendPacket(packetPlayOutEntityHeadRotation);

        sendMetadataPacket(player);

        getHologram(player).show(player);

        // Removing the player info after 10 seconds.
        Bukkit.getScheduler().runTaskLater(instance.getPlugin(), () ->
                playerConnection.sendPacket(packetPlayOutPlayerInfoRemove), 20 * 10);
    }

    @Override
    public void sendHidePackets(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().b;

        playerConnection.sendPacket(packetPlayOutEntityDestroy);
        playerConnection.sendPacket(packetPlayOutPlayerInfoRemove);

        getHologram(player).hide(player);
    }

    @Override
    public void sendMetadataPacket(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().b;
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadataWrapper().create(activeStates, entityId);

        playerConnection.sendPacket(packet);
    }

    @Override
    public void sendEquipmentPacket(Player player, NPCSlot slot, boolean auto) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().b;

        EnumItemSlot nmsSlot = slot.getNmsEnum(EnumItemSlot.class);
        ItemStack item = getItem(slot);

        Pair<EnumItemSlot, net.minecraft.world.item.ItemStack> pair = new Pair<>(nmsSlot, CraftItemStack.asNMSCopy(item));
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(entityId, Collections.singletonList(pair));
        playerConnection.sendPacket(packet);
    }

    @Override
    public void sendAnimationPacket(Player player, NPCAnimation animation) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().b;

        PacketPlayOutAnimation packet = new PacketPlayOutAnimationWrapper().create(animation, entityId);
        playerConnection.sendPacket(packet);
    }

    @Override
    public void updateSkin(Skin skin) {
        GameProfile newProfile = new GameProfile(uuid, name);
        newProfile.getProperties().get("textures").clear();
        newProfile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
        this.packetPlayOutPlayerInfoAdd =
                new PacketPlayOutPlayerInfoWrapper().create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, newProfile, name);

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().b;
            playerConnection.sendPacket(packetPlayOutPlayerInfoRemove);
            playerConnection.sendPacket(packetPlayOutEntityDestroy);
            playerConnection.sendPacket(packetPlayOutPlayerInfoAdd);
            playerConnection.sendPacket(packetPlayOutNamedEntitySpawn);
        }
    }

    @Override
    public void sendHeadRotationPackets(Location location) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerConnection connection = ((CraftPlayer) player).getHandle().b;

            Location npcLocation = getLocation();
            Vector dirBetweenLocations = location.toVector().subtract(npcLocation.toVector());

            npcLocation.setDirection(dirBetweenLocations);

            float yaw = npcLocation.getYaw();
            float pitch = npcLocation.getPitch();

            connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(entityId, (byte) ((yaw % 360.) * 256 / 360), (byte) ((pitch % 360.) * 256 / 360), false));
            connection.sendPacket(new PacketPlayOutEntityHeadRotationWrapper().create(npcLocation, entityId));
        }
    }
}

/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.hologram;

import net.jitse.npclib.internal.MinecraftVersion;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Hologram {

    private static final double DELTA = 0.3;

    private final List<EntityArmorStand> armorStands = new ArrayList<EntityArmorStand>();
    private final List<Packet> showPackets = new ArrayList<>();
    private final List<Packet> hidePackets = new ArrayList<>();
    private final List<Packet> metaPackets = new ArrayList<>();

    private final Location start;
    private final WorldServer worldServer;

    private List<String> text;

    public Hologram(Location location, List<String> text) {
        this.start = location;
        this.text = text;

        this.worldServer = ((CraftWorld) location.getWorld()).getHandle();

        createPackets();
    }

    private void createPackets() {

        Location location = start.clone().add(0, (DELTA * text.size()) + 1f, 0);
        Class<?> worldClass = worldServer.getClass().getSuperclass();

        if (start.getWorld().getEnvironment() != World.Environment.NORMAL) {
            worldClass = worldClass.getSuperclass();
        }

        for (String line : text) {
            EntityArmorStand entityArmorStand = new EntityArmorStand(worldServer, location.getX(), location.getY(), location.getZ());
            entityArmorStand.setLocation(location.getX(), location.getY(), location.getZ(), 0, 0);

            entityArmorStand.setCustomName(new ChatComponentText(line));
            entityArmorStand.setCustomNameVisible(true);
            entityArmorStand.setNoGravity(true);
            entityArmorStand.setSmall(true);
            entityArmorStand.setInvisible(true);
            entityArmorStand.setBasePlate(false);
            entityArmorStand.setArms(false);

            ArmorStand as = (ArmorStand) entityArmorStand.getBukkitEntity();
            as.setMarker(true); // set the marker state

            armorStands.add(entityArmorStand);

            // Create and add the associated show and hide packets.
            showPackets.add(new PacketPlayOutSpawnEntityLiving(entityArmorStand));
            hidePackets.add(new PacketPlayOutEntityDestroy(entityArmorStand.getId()));

            metaPackets.add(new PacketPlayOutEntityMetadata(entityArmorStand.getId(), entityArmorStand.getDataWatcher(), true));
            location.subtract(0, DELTA, 0);
        }
    }

    public List<Packet> getUpdatePackets(List<String> newText) {
        List<Packet> updatePackets = new ArrayList<>();

        if (this.text.size() != newText.size()) {
            throw new IllegalArgumentException("When updating the text, the old and new text should have the same amount of lines");
        }

        for (int i = 0; i < newText.size(); i++) {
            EntityArmorStand entityArmorStand = armorStands.get(i);
            String oldLine = this.text.get(i);
            String newLine = newText.get(i);

            entityArmorStand.setCustomName(new ChatComponentText(newLine));
            showPackets.set(i, new PacketPlayOutSpawnEntityLiving(entityArmorStand));

            if (newLine.isEmpty() && !oldLine.isEmpty()) {
                updatePackets.add(hidePackets.get(i));
            } else if (!newLine.isEmpty() && oldLine.isEmpty()) {
                updatePackets.add(showPackets.get(i));
            } else if (!oldLine.equals(newLine)) {
                // Update the line for all players using a Metadata packet.
                updatePackets.add(new PacketPlayOutEntityMetadata(entityArmorStand.getId(), entityArmorStand.getDataWatcher(), true));
            }
        }

        this.text = newText;

        return updatePackets;
    }

    public void update(Player player, List<Packet> updatePackets) {
        for (Packet packet : updatePackets) {
            ((CraftPlayer) player).getHandle().b.sendPacket(packet);
        }
    }

    public void show(Player player) {
        for (int i = 0; i < text.size(); i++) {
            if (text.get(i).isEmpty()) continue; // No need to spawn the line.
            ((CraftPlayer) player).getHandle().b.sendPacket(showPackets.get(i));
            ((CraftPlayer) player).getHandle().b.sendPacket(metaPackets.get(i));
        }
    }

    public void hide(Player player) {

        for (int i = 0; i < text.size(); i++) {
            if (text.get(i).isEmpty()) continue; // No need to hide the line (as it was never spawned).
            ((CraftPlayer) player).getHandle().b.sendPacket(hidePackets.get(i));
        }
    }
}

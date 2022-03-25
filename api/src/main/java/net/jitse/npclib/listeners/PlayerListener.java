/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.listeners;

import net.jitse.npclib.NPCLib;
import net.jitse.npclib.internal.NPCBase;
import net.jitse.npclib.internal.NPCManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Jitse Boonstra
 */
public class PlayerListener extends HandleMoveBase implements Listener {

    private final NPCLib instance;

    public PlayerListener(NPCLib instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(instance.getPlugin(), () -> {
            ((CraftPlayer) event.getPlayer()).getHandle().b.a.k.pipeline().addBefore("packet_handler", "npc_lib", new PacketListener(event.getPlayer()));
            for (NPCBase npc : NPCManager.getAllNPCs()) {
                npc.sendShowPackets(event.getPlayer());
            }
        }, 20);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
    	for (NPCBase npc : NPCManager.getAllNPCs())
            npc.onLogout(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Need to auto hide the NPCs from the player, or else the system will think they can see the NPC on respawn.
        Player player = event.getEntity();
        for (NPCBase npc : NPCManager.getAllNPCs()) {
            if (npc.isShown(player) && npc.getWorld().equals(player.getWorld())) {
                npc.hide(player, true);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // If the player dies in the server spawn world, the world change event isn't called (nor is the PlayerTeleportEvent).
        Player player = event.getPlayer();
        Location respawn = event.getRespawnLocation();
        if (respawn.getWorld() != null && respawn.getWorld().equals(player.getWorld())) {
            // Waiting until the player is moved to the new location or else it'll mess things up.
            // I.e. if the player is at great distance from the NPC spawning, they won't be able to see it.
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        this.cancel();
                        return;
                    }
                    if (player.getLocation().equals(respawn)) {
                        handleMove(player);
                        this.cancel();
                    }
                }
            }.runTaskTimer(instance.getPlugin(), 0L, 1L);
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World from = event.getFrom();

        // The PlayerTeleportEvent is called, and will handle visibility in the new world.
        for (NPCBase npc : NPCManager.getAllNPCs()) {
            if (npc.isShown(player) && npc.getWorld().equals(from)) {
                npc.hide(player, true);
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        handleMove(event.getPlayer());
    }
}

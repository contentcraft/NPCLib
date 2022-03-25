/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.listeners;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;
import net.jitse.npclib.api.events.NPCInteractEvent;
import net.jitse.npclib.internal.NPCBase;
import net.jitse.npclib.internal.NPCManager;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Jitse Boonstra
 */
public class PacketListener extends ChannelDuplexHandler {

    // Prevent players from clicking at very high speeds.
    private final Set<UUID> delay = new HashSet<>();
    private Player player;

    private static Field idField;
    private static Field actionField;

    static {
        try {
            idField = PacketPlayInUseEntity.class.getDeclaredField("a");
            idField.setAccessible(true);
            actionField = PacketPlayInUseEntity.class.getDeclaredField("b");
            actionField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public PacketListener(Player player) {
        this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext c, Object packet) throws Exception {
        if (packet instanceof PacketPlayInUseEntity) {
            PacketPlayInUseEntity p = (PacketPlayInUseEntity) packet;

        }
        super.channelRead(c, packet);
    }

    @SneakyThrows
    private boolean handleInteractPacket(Player player, PacketPlayInUseEntity packet) {
        NPCBase npc = null;
        int packetEntityId = (int) idField.get(packet);

        // Not using streams here is an intentional choice.
        // Packet listeners is one of the few places where it is important to write optimized code.
        // Lambdas (And the stream api) create a massive amount of objects, especially if it isn't a static lambda.
        // So, we're avoiding them here.
        // ~ Kneesnap, 9 / 20 / 2019.

        for (NPCBase testNPC : NPCManager.getAllNPCs()) {
            if (testNPC.isCreated() && testNPC.isShown(player) && testNPC.getEntityId() == packetEntityId) {
                npc = testNPC;
                break;
            }
        }

        if (npc == null) {
            // Default player, not doing magic with the packet.
            return true;
        }

        if (delay.contains(player.getUniqueId())) { // There is an active delay.
            return false;
        }

        NPCInteractEvent.ClickType clickType = actionField.get(packet).toString().equals("ATTACK")
                ? NPCInteractEvent.ClickType.LEFT_CLICK : NPCInteractEvent.ClickType.RIGHT_CLICK;

        delay.add(player.getUniqueId());
        Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugins()[0], new TaskCallNpcInteractEvent(new NPCInteractEvent(player, clickType, npc), this));
        return false;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext channelHandlerContext) throws Exception {

    }

    @Override
    public void handlerRemoved(ChannelHandlerContext channelHandlerContext) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {

    }

    // This would be a non-static lambda, and its usage matters, so we'll make it a full class.
    private static final class TaskCallNpcInteractEvent implements Runnable {
        private NPCInteractEvent eventToCall;
        private PacketListener listener;

        private static Location playerLocation = new Location(null, 0, 0, 0);

        TaskCallNpcInteractEvent(NPCInteractEvent eventToCall, PacketListener listener) {
            this.eventToCall = eventToCall;
            this.listener = listener;
        }

        @Override
        public void run() {
            Player player = eventToCall.getWhoClicked();
            this.listener.delay.remove(player.getUniqueId()); // Remove the NPC from the interact cooldown.
            if (!player.getWorld().equals(eventToCall.getNPC().getWorld()))
                return; // If the NPC and player are not in the same world, abort!

            double distance = player.getLocation(playerLocation).distanceSquared(eventToCall.getNPC().getLocation());
            if (distance <= 64) // Only handle the interaction if the player is within interaction range. This way, hacked clients can't interact with NPCs that they shouldn't be able to interact with.
                Bukkit.getPluginManager().callEvent(this.eventToCall);
        }
    }
}

package net.jitse.npclib.impl.packets;

import net.jitse.npclib.api.state.NPCAnimation;
import net.jitse.npclib.impl.workarounds.EntityPlayerAdapter;
import net.minecraft.network.protocol.game.PacketPlayOutAnimation;

public class PacketPlayOutAnimationWrapper {

    public PacketPlayOutAnimation create(NPCAnimation npcAnimation, int entityId)  {
        EntityPlayerAdapter epa = EntityPlayerAdapter.createNil();
        epa.setOid(entityId);

        PacketPlayOutAnimation packetPlayOutAnimation = new PacketPlayOutAnimation(epa, npcAnimation.getId());

        return packetPlayOutAnimation;
    }

}

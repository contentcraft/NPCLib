package net.jitse.npclib.nms.v1_17_R1.packets;

import net.jitse.npclib.api.state.NPCAnimation;
import net.jitse.npclib.nms.v1_17_R1.workarounds.EntityPlayerAdapter;
import net.minecraft.network.protocol.game.PacketPlayOutAnimation;

public class PacketPlayOutAnimationWrapper {

    public PacketPlayOutAnimation create(NPCAnimation npcAnimation, int entityId)  {
        EntityPlayerAdapter epa = EntityPlayerAdapter.createNil();
        epa.setOid(entityId);

        PacketPlayOutAnimation packetPlayOutAnimation = new PacketPlayOutAnimation(epa, npcAnimation.getId());

        return packetPlayOutAnimation;
    }

}

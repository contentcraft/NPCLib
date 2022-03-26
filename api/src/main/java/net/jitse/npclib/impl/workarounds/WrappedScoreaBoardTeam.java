package net.jitse.npclib.impl.workarounds;

import com.google.gson.Gson;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardTeam;

public class WrappedScoreaBoardTeam extends ScoreboardTeam {

    private String overwrittenName;
    private EnumNameTagVisibility l;

    public static WrappedScoreaBoardTeam createNil() {
        return new WrappedScoreaBoardTeam(null, "tits");
    }

    public WrappedScoreaBoardTeam(Scoreboard var0, String var1) {
        super(var0, var1);
    }

    @Override
    public String getName() {
        return overwrittenName;
    }

    @Override
    public void setNameTagVisibility(EnumNameTagVisibility var0) {
        this.l = var0;
    }

    @Override
    public EnumNameTagVisibility getNameTagVisibility() {
        return this.l;
    }

    public void setOverwrittenName(String overwrittenName) {
        this.overwrittenName = overwrittenName;
    }
}

package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.save.*;
import net.lordofthecraft.arche.skill.*;
import net.lordofthecraft.arche.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import org.bukkit.entity.*;
import net.lordofthecraft.arche.save.tasks.*;

public class SkillAttachment
{
    private static final SaveHandler buffer;
    private FutureTask<SkillData> call;
    private double xp;
    private boolean canSee;
    private double modifier;
    final ArcheSkill skill;
    private final String uuid;
    private final int id;
    private boolean error;
    
    SkillAttachment(final ArcheSkill skill, final ArchePersona persona, final FutureTask<SkillData> call) {
        super();
        this.modifier = -1.0;
        this.error = false;
        this.call = call;
        this.xp = 0.0;
        this.canSee = (skill.getVisibility() == 1);
        this.skill = skill;
        this.uuid = persona.getPlayerUUID().toString();
        this.id = persona.getId();
    }
    
    public void initialize() {
        if (this.call != null) {
            SkillData data = null;
            try {
                data = this.call.get(200L, TimeUnit.MILLISECONDS);
                if (data != null) {
                    this.xp = data.xp;
                    this.canSee = data.visible;
                }
                this.call = null;
            }
            catch (TimeoutException e2) {
                this.error = true;
                final Logger log = ArcheCore.getPlugin().getLogger();
                log.severe("SQL interfacing thread is lagging behind.");
                log.severe("Skill data might be impossible to retrieve.");
                final UUID u = UUID.fromString(this.uuid);
                ArchePersonaHandler.getInstance().unload(u);
                final Player x = ArcheCore.getPlayer(u);
                if (x == null) {
                    log.severe("ERROR: SkillAttachment owning Player " + this.uuid + "was not found online.");
                }
                else {
                    log.severe("Kicking player " + x.getName() + "in an effort to preserve skill data integrity.");
                    log.severe("Sorry, " + x.getName() + " :(");
                    x.kickPlayer("Skill Data error. Please reconnect.");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public boolean isInitialized() {
        return this.call == null;
    }
    
    public double getModifier() {
        return this.modifier;
    }
    
    public void setModifier(final double modifier) {
        this.modifier = modifier;
    }
    
    public double getXp() {
        return this.xp;
    }
    
    public boolean isVisible() {
        return this.canSee;
    }
    
    public void reveal() {
        if (!this.canSee) {
            this.canSee = true;
            this.performSQLUpdate();
        }
    }
    
    public void addXp(final double added) {
        this.xp += added;
        this.performSQLUpdate();
    }
    
    public void removeXP(final double removed) {
        this.xp -= removed;
        this.performSQLUpdate();
    }
    
    public void setXp(final double xp) {
        this.xp = xp;
        this.performSQLUpdate();
    }
    
    private void performSQLUpdate() {
        if (this.error) {
            return;
        }
        final ArcheTask task = new UpdateSkillTask(this.skill, this.uuid, this.id, this.xp, this.canSee);
        SkillAttachment.buffer.put(task);
    }
    
    static {
        buffer = SaveHandler.getInstance();
    }
}

package net.lordofthecraft.arche.skill;

import java.util.*;
import java.sql.*;
import net.lordofthecraft.arche.*;
import org.bukkit.entity.*;
import net.lordofthecraft.arche.interfaces.*;
import net.lordofthecraft.arche.event.*;
import org.bukkit.event.*;
import org.apache.commons.lang.*;
import org.bukkit.*;
import net.lordofthecraft.arche.enums.*;
import net.lordofthecraft.arche.persona.*;

public class ArcheSkill implements Skill
{
    private static Set<ExpModifier> xpMods;
    private static ArcheTimer timer;
    private final int id;
    private final String name;
    private final int displayStrategy;
    private final boolean inert;
    private final Set<Race> mains;
    private final Map<Race, Double> raceMods;
    private final boolean intensive;
    private final PreparedStatement statement;
    
    ArcheSkill(final int id, final String name, final int displayStrategy, final boolean inert, final Set<Race> mains, final Map<Race, Double> raceMods, final PreparedStatement state, final boolean intensive) {
        super();
        ArcheSkill.timer = ArcheCore.getPlugin().getMethodTimer();
        this.id = id;
        this.name = name;
        this.displayStrategy = displayStrategy;
        this.inert = inert;
        this.mains = mains;
        this.raceMods = raceMods;
        this.statement = state;
        this.intensive = intensive;
    }
    
    public PreparedStatement getUpdateStatement() {
        return this.statement;
    }
    
    @Override
    public boolean isProfessionFor(final Race race) {
        return this.mains.contains(race);
    }
    
    @Override
    public boolean isIntensiveProfession() {
        return this.intensive;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public int getId() {
        return this.id;
    }
    
    @Override
    public int getVisibility() {
        return this.displayStrategy;
    }
    
    @Override
    public boolean isInert() {
        return this.inert;
    }
    
    @Override
    public boolean isVisible(final Player p) {
        return this.isVisible(this.getPersona(p));
    }
    
    @Override
    public boolean isVisible(final Persona p) {
        return this.getAttachment(p).isVisible();
    }
    
    @Override
    public boolean reveal(final Persona p) {
        final boolean vis = this.isVisible(p);
        this.getAttachment(p).reveal();
        return vis;
    }
    
    @Override
    public void addXp(final Player p, final double xp) {
        this.addXp(this.getPersona(p), xp);
    }
    
    @Override
    public void addXp(final Persona p, final double xp) {
        if (ArcheSkill.timer != null) {
            ArcheSkill.timer.startTiming("xp_" + this.name);
        }
        if (!p.getXPGain()) {
            return;
        }
        if (!this.canGainXp(p)) {
            return;
        }
        this.addRawXp(p, xp);
        if (ArcheSkill.timer != null) {
            ArcheSkill.timer.stopTiming("xp_" + this.name);
        }
    }
    
    @Override
    public void addRawXp(final Player p, final double xp) {
        this.addRawXp(this.getPersona(p), xp);
    }
    
    @Override
    public void addRawXp(final Player p, final double xp, final boolean modify) {
        this.addRawXp(this.getPersona(p), xp, modify);
    }
    
    @Override
    public void addRawXp(final Persona p, final double xp) {
        this.addRawXp(p, xp, true);
    }
    
    @Override
    public void addRawXp(final Persona p, double xp, final boolean modify) {
        final SkillAttachment attach = this.getAttachment(p);
        if (modify) {
            final double mod = this.getXPModifier(p, attach);
            if (mod > 0.0) {
                xp *= ((xp > 0.0) ? mod : (1.0 / mod));
            }
        }
        final GainXPEvent event = new GainXPEvent(p, this, xp);
        Bukkit.getPluginManager().callEvent((Event)event);
        if (event.isCancelled()) {
            return;
        }
        xp = event.getAmountGained();
        final double oldXp = attach.getXp();
        final SkillTier cap = this.getCapTier(p);
        final double newXp = Math.min(cap.getXp(), oldXp + xp);
        if (this.getVisibility() == 2) {
            attach.reveal();
        }
        if (newXp == oldXp) {
            return;
        }
        attach.setXp(newXp);
        if (!attach.isVisible()) {
            return;
        }
        final Player player = p.getPlayer();
        if (player != null) {
            final SkillTier now = this.getSkillTier(p);
            if (now != SkillTier.RUSTY && (now != SkillTier.INEXPERIENCED || newXp >= 0.0)) {
                final int nxp = (now == SkillTier.INEXPERIENCED) ? 0 : now.getXp();
                player.setExp((now == cap) ? 0.0f : ((float)((newXp - nxp) / (now.getNext().getXp() - nxp))));
                player.setLevel((now == cap) ? 0 : ((int)(now.getNext().getXp() - newXp)));
            }
        }
        if (xp > 0.0) {
            final SkillTier current = this.getSkillTier(p);
            final int treshold = current.getXp();
            if (oldXp < treshold && newXp >= treshold && player != null) {
                final char n = this.getName().charAt(0);
                final String an = (n == 'e' || n == 'o' || n == 'i' || n == 'a') ? "an" : "a";
                player.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "You have improved your skill as " + an + " " + ChatColor.AQUA + WordUtils.capitalize(this.getName()));
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 3.0f, 1.0f);
            }
        }
    }
    
    private double getXPModifier(final Persona p, final SkillAttachment att) {
        if (ArcheSkill.xpMods.isEmpty()) {
            return 1.0;
        }
        double mod = att.getModifier();
        if (mod > 0.0) {
            return mod;
        }
        final Race r = p.getRace();
        mod = (ArcheSkill.xpMods.contains(ExpModifier.RACIAL) ? (this.raceMods.containsKey(r) ? this.raceMods.get(p.getRace()) : 1.0) : 1.0);
        if (ArcheSkill.xpMods.contains(ExpModifier.PLAYTIME)) {
            final int mins = p.getTimePlayed();
            if (mins >= 1200) {
                final double f = 0.05 * (mins / 12000 + Math.pow(mins, 1.5) / 700000.0);
                final double dmod = Math.min(1.0, f);
                mod += dmod;
            }
        }
        if (ArcheSkill.xpMods.contains(ExpModifier.AUTOAGE) && p.doesAutoAge() && (r == Race.HUMAN || r == Race.HALFLING || r == Race.NORTHENER || r == Race.SOUTHERON)) {
            mod += 0.1;
        }
        att.setModifier(mod);
        return mod;
    }
    
    @Override
    public SkillTier getCapTier(final Persona p) {
        if (this.isInert()) {
            return SkillTier.AENGULIC;
        }
        if (this.isProfessionFor(p.getRace()) || p.getTimePlayed() > 120000) {
            return SkillTier.AENGULIC;
        }
        for (final ProfessionSlot slot : ProfessionSlot.values()) {
            if (p.getProfession(slot) == this) {
                return SkillTier.AENGULIC;
            }
        }
        final SkillTier t = (p.getProfession(ProfessionSlot.PRIMARY) == null) ? SkillTier.ADEQUATE : ((p.getProfession(ProfessionSlot.SECONDARY) == null && !p.getProfession(ProfessionSlot.PRIMARY).isIntensiveProfession()) ? SkillTier.CLUMSY : SkillTier.RUSTY);
        final Race r = p.getRace();
        if (t != SkillTier.RUSTY && (r == Race.HUMAN || r == Race.NORTHENER || r == Race.SOUTHERON)) {
            return t.getNext().getNext();
        }
        return t;
    }
    
    @Override
    public double getXp(final Player p) {
        return this.getXp(this.getPersona(p));
    }
    
    @Override
    public double getXp(final Persona p) {
        final SkillAttachment attach = this.getAttachment(p);
        return attach.getXp();
    }
    
    @Override
    public boolean achievedTier(final Player p, final SkillTier tier) {
        return this.achievedTier(this.getPersona(p), tier);
    }
    
    @Override
    public boolean achievedTier(final Persona p, final SkillTier tier) {
        final SkillAttachment att = this.getAttachment(p);
        return att.getXp() >= tier.getXp();
    }
    
    @Override
    public SkillTier getSkillTier(final Player p) {
        return this.getSkillTier(this.getPersona(p));
    }
    
    @Override
    public SkillTier getSkillTier(final Persona p) {
        final double xp = this.getXp(p);
        SkillTier result = SkillTier.RUSTY;
        for (final SkillTier st : SkillTier.values()) {
            if (st.getXp() > xp) {
                return result;
            }
            result = st;
        }
        return result;
    }
    
    @Override
    public boolean canGainXp(final Persona p) {
        final SkillAttachment att = this.getAttachment(p);
        return this.inert || att.isVisible() || this.getVisibility() == 2;
    }
    
    private Persona getPersona(final Player p) {
        return ArchePersonaHandler.getInstance().getPersona(p);
    }
    
    private SkillAttachment getAttachment(final Persona pers) {
        final SkillAttachment attach = ((ArchePersona)pers).getSkill(this.getId());
        if (!attach.isInitialized()) {
            attach.initialize();
        }
        return attach;
    }
    
    static {
        ArcheSkill.xpMods = ArcheSkillFactory.xpMods;
    }
}

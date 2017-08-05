package net.lordofthecraft.arche.magic;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Magic;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.persona.MagicAttachment;
import net.lordofthecraft.arche.save.SaveExecutorManager;
import net.lordofthecraft.arche.save.tasks.magic.ArcheMagicDeleteTask;
import net.lordofthecraft.arche.save.tasks.magic.ArcheMagicInsertTask;
import net.lordofthecraft.arche.save.tasks.magic.ArcheMagicUpdateTask;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Created on 7/3/2017
 *
 * @author 501warhead
 */
public class ArcheMagic implements Magic {

    /*
CREATE TABLE IF NOT EXISTS magics (
    id_key          VARCHAR(255),
    max_tier        INT,
    extra_tier      BOOLEAN,
    self_teach      BOOLEAN,
    teachable       BOOLEAN,
    description     TEXT,
    label           TEXT,
    days_to_max     INT UNSIGNED,
    days_to_extra   INT UNSIGNED,
    archetype       VARCHAR(255),
    PRIMARY KEY (name),
    FOREIGN KEY (id_key) REFERENCES magic_archetypes(id_key)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;
     */

    private static Set<ArcheMagic> MAGICS = Sets.newConcurrentHashSet();
    private final String name;
    private int maxTier;
    private boolean selfTeachable;
    private String label;
    private String description;
    private boolean teachable;
    private int daysToMaxTier;
    private int daysToBonusTier;
    private ArcheType type;
    private Map<Magic, Integer> weaknesses;

    public static ArcheMagic createMagic(String name, int maxTier, boolean selfTeachable) {
        Optional<ArcheMagic> magic = MAGICS.stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst();
        if (magic.isPresent()) {
            return magic.get();
        }
        ArcheMagic m = new ArcheMagic(name, maxTier, selfTeachable);
        MAGICS.add(m);
        SaveExecutorManager.getInstance().submit(new ArcheMagicInsertTask(m));
        return m;
    }

    public static Set<ArcheMagic> getMagics() {
        return Collections.unmodifiableSet(MAGICS);
    }

    public static Optional<ArcheMagic> getMagicByName(String name) {
        return MAGICS.stream().filter(m -> m.name.equalsIgnoreCase(name)).findFirst();
    }

    protected ArcheMagic(String name, int maxTier, boolean selfTeachable) {
        this.name = name;
        this.maxTier = maxTier;
        this.selfTeachable = selfTeachable;
        weaknesses = Maps.newConcurrentMap();
    }

    public ArcheMagic(String name, int maxTier, boolean selfTeachable, String label, String description, boolean teachable, int daysToMaxTier, int daysToBonusTier, ArcheType type) {
        this.name = name;
        this.maxTier = maxTier;
        this.selfTeachable = selfTeachable;
        this.label = label;
        this.description = description;
        this.teachable = teachable;
        this.daysToMaxTier = daysToMaxTier;
        this.daysToBonusTier = daysToBonusTier;
        this.type = type;
        weaknesses = Maps.newConcurrentMap();
    }

    public void setMaxTier(int maxTier) {
        this.maxTier = maxTier;
        performSQLUpdate();
    }

    public void setSelfTeachable(boolean selfTeachable) {
        this.selfTeachable = selfTeachable;
        performSQLUpdate();
    }

    public void addWeakness(Magic m, int mod) {
        weaknesses.put(m, mod);
    }

    public boolean isWeakAgainst(Magic m) {
        return weaknesses.containsKey(m);
    }

    public int getWeaknessModifier(Magic m) {
        if (!weaknesses.containsKey(m)) return 0;
        return weaknesses.get(m);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isTeachable() {
        return teachable;
    }

    @Override
    public int getDaysToMaxTier() {
        return daysToMaxTier;
    }

    @Override
    public int getDaysToBonusTier() {
        return daysToBonusTier;
    }

    @Override
    public ArcheType getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getMaxTier() {
        return maxTier;
    }

    @Override
    public boolean isSelfTeachable() {
        return selfTeachable;
    }

    @Override
    public boolean hasLearned(Player p) {
        return hasLearned(ArcheCore.getPersona(p));
    }

    @Override
    public boolean hasLearned(Persona p) {
        return getAttach(p).isPresent();
    }

    @Override
    public boolean isVisible(Player p) { return isVisible(ArcheCore.getPersona(p)); }

    @Override
    public boolean isVisible(Persona p) {
        return getAttach(p).isPresent() && getAttach(p).get().isVisible();
    }

    @Override
    public int getTier(Player p) { return getTier(ArcheCore.getPersona(p));}

    @Override
    public int getTier(Persona p) {
        if (getAttach(p).isPresent()) {
            return getAttach(p).get().getTier();
        } else {
            return 0;
        }
    }

    @Override
    public boolean achievedTier(Player p, int tier) {
        return achievedTier(ArcheCore.getPersona(p), tier);
    }

    @Override
    public boolean achievedTier(Persona p, int tier) {
        return getTier(p) >= tier;
    }

    @Override
    public void setTier(Persona p, int tier) {
        if (getAttach(p).isPresent()) {
            getAttach(p).get().setTier(tier);
        }
    }

    private Optional<MagicAttachment> getAttach(Persona p) {
        return ((ArchePersona) p).getMagicAttachment(this);
    }

    private Optional<Persona> getPersona(Player p) {
        Persona pers = ArcheCore.getPersona(p);
        if (pers == null) {
            return Optional.empty();
        } else {
            return Optional.of(pers);
        }
    }

    protected void performSQLUpdate() {
        SaveExecutorManager.getInstance().submit(new ArcheMagicUpdateTask(this));
    }

    public void remove() {
        ArchePersonaHandler.getInstance().removeMagic(this);
        SaveExecutorManager.getInstance().submit(new ArcheMagicDeleteTask(name));
    }

}

package net.lordofthecraft.arche.magic;

import com.google.common.collect.Maps;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Magic;
import net.lordofthecraft.arche.interfaces.MagicType;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.MagicAttachment;
import net.lordofthecraft.arche.save.archerows.magic.delete.ArcheMagicDeleteRow;
import net.lordofthecraft.arche.save.archerows.magic.insert.ArcheMagicInsertRow;
import net.lordofthecraft.arche.save.archerows.magic.update.ArcheMagicUpdateRow;
import org.bukkit.entity.Player;

import java.sql.JDBCType;
import java.sql.SQLType;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

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

    public enum Field {
        MAX_TIER("max_tier", JDBCType.INTEGER),
        EXTRA_TIER("extra_tier", JDBCType.BOOLEAN),
        SELF_TEACH("self_teach", JDBCType.BOOLEAN),
        TEACHABLE("teachable", JDBCType.BOOLEAN),
        DESCRIPTION("description", JDBCType.VARCHAR),
        LABEL("label", JDBCType.VARCHAR),
        DAYS_TO_MAX("days_to_max", JDBCType.INTEGER),
        DAYS_TO_EXTRA("days_to_extra", JDBCType.INTEGER),
        ARCHETYPE("archetype", JDBCType.VARCHAR);

        public final String field;
        public final SQLType type;


        Field(String field, SQLType type) {
            this.field = field;
            this.type = type;
        }
    }

    private final String name;
    private int maxTier;
    private boolean selfTeachable;
    private String label;
    private String description;
    private boolean teachable;
    private int daysToMaxTier;
    private int daysToBonusTier;
    private MagicType type;
    private Map<Magic, Double> weaknesses;

    public static Magic createMagic(String name, int maxTier, boolean selfTeachable) {
        Optional<Magic> magic = ArcheCore.getMagicControls().getMagic(name);
        if (magic.isPresent()) {
            return magic.get();
        }
        ArcheMagic m = new ArcheMagic(name, maxTier, selfTeachable);
        ArcheCore.getMagicControls().registerMagic(m);
        //SaveHandler.getInstance().put(new ArcheMagicInsertTask(m));
        ArcheCore.getConsumerControls().queueRow(new ArcheMagicInsertRow(m));
        return m;
    }


    protected ArcheMagic(String name, int maxTier, boolean selfTeachable) {
        this.name = name;
        this.maxTier = maxTier;
        this.selfTeachable = selfTeachable;
        weaknesses = Maps.newConcurrentMap();
    }

    ArcheMagic(String name, int maxTier, boolean selfTeachable, String label, String description, boolean teachable, int daysToMaxTier, int daysToBonusTier, ArcheType type) {
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
        performSQLUpdate(Field.MAX_TIER, maxTier);
    }

    public void setSelfTeachable(boolean selfTeachable) {
        this.selfTeachable = selfTeachable;
        performSQLUpdate(Field.SELF_TEACH, selfTeachable);
    }

    public void addWeakness(Magic m, double mod) {
        weaknesses.put(m, mod);
    }

    @Override
    public boolean isWeakAgainst(Magic m) {
        return weaknesses.containsKey(m);
    }

    @Override
    public Map<Magic, Double> getWeaknesses() {
        return Collections.unmodifiableMap(weaknesses);
    }

    @Override
    public double getWeaknessModifier(Magic m) {
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
    public MagicType getType() {
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
    public boolean isVisible(Player p) {
        return isVisible(ArcheCore.getPersona(p));
    }

    @Override
    public boolean isVisible(Persona p) {
        return getAttach(p).isPresent() && getAttach(p).get().isVisible();
    }

    @Override
    public int getTier(Player p) {
        return getTier(ArcheCore.getPersona(p));
    }

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

    protected void performSQLUpdate(Field f, Object data) {
        //SaveHandler.getInstance().put(new ArcheMagicUpdateTask(this, f, data));
        ArcheCore.getConsumerControls().queueRow(new ArcheMagicUpdateRow(this, f, data));
    }

    public void remove() {
        ArcheCore.getMagicControls().removeMagic(this);
        //SaveHandler.getInstance().put(new ArcheMagicDeleteTask(name));
        ArcheCore.getConsumerControls().queueRow(new ArcheMagicDeleteRow(this));
    }

}

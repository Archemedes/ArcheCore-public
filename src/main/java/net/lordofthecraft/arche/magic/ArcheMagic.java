package net.lordofthecraft.arche.magic;

import com.google.common.collect.Sets;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.SQLHandler;
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

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

    public static void init(SQLHandler handler) {
        try {
            PreparedStatement stat = handler.getConnection().prepareStatement("SELECT id_key,max_tier,self_teach,extra_tier,teachable,description,label,days_to_max,days_to_extra,archetype FROM magics");
            ResultSet rs = stat.executeQuery();
            while (rs.next()) {
                String id_key = rs.getString("id_key");
                int max_tier = rs.getInt("max_tier");
                boolean selfteach = rs.getBoolean("self_teach");
                boolean teach = rs.getBoolean("teachable");
                String label = rs.getString("label");
                String description = rs.getString("description");
                int extra_tier = rs.getInt("extra_tier");
                int days_to_max = rs.getInt("days_to_max");
                int days_to_extra = rs.getInt("days_to_extra");
                String sarchetype = rs.getString("archetype");
                //TODO verify archetype
                ArcheMagic m = new ArcheMagic(id_key, max_tier, selfteach);
                MAGICS.add(m);
            }
            stat.close();
            rs.close();
        } catch (SQLException e) {
            ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "We failed to initialize ArcheMagics! Persona-tethered magics will be unavailable!", e);
        }
    }

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
    }

    public void setMaxTier(int maxTier) {
        this.maxTier = maxTier;
        performSQLUpdate();
    }

    public void setSelfTeachable(boolean selfTeachable) {
        this.selfTeachable = selfTeachable;
        performSQLUpdate();
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

    public boolean hasLearned(Player p) {
        return hasLearned(ArcheCore.getPersona(p));
    }

    public boolean hasLearned(Persona p) {
        return getAttach(p).isPresent();
    }

    public boolean isVisible(Player p) { return isVisible(ArcheCore.getPersona(p)); }

    public boolean isVisible(Persona p) {
        if (getAttach(p).isPresent()) {
            return getAttach(p).get().isVisible();
        } else {
            return false;
        }
    }

    public int getTier(Player p) { return getTier(ArcheCore.getPersona(p));}

    public int getTier(Persona p) {
        if (getAttach(p).isPresent()) {
            return getAttach(p).get().getTier();
        } else {
            return 0;
        }
    }

    public boolean achievedTier(Player p, int tier) {
        return achievedTier(ArcheCore.getPersona(p), tier);
    }

    public boolean achievedTier(Persona p, int tier) {
        return getTier(p) >= tier;
    }

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

package net.lordofthecraft.arche.persona.magic;

import com.google.common.collect.Sets;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.save.SaveExecutorManager;
import net.lordofthecraft.arche.save.tasks.magic.ArcheMagicDeleteTask;
import net.lordofthecraft.arche.save.tasks.magic.ArcheMagicInsertTask;
import net.lordofthecraft.arche.save.tasks.magic.ArcheMagicUpdateTask;

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
public class ArcheMagic {

    /*
    CREATE TABLE IF NOT EXISTS magics (
    name        VARCHAR(255),
    max_tier    INT,
    self_teach  BOOLEAN,
    PRIMARY KEY (name)
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


    public static void init(SQLHandler handler) {
        try {
            PreparedStatement stat = handler.getConnection().prepareStatement("SELECT name,max_tier,self_teach FROM magics");
            ResultSet rs = stat.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                int max_tier = rs.getInt("max_tier");
                boolean selfteach = rs.getBoolean("self_teach");
                ArcheMagic m = new ArcheMagic(name, max_tier, selfteach);
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

    public String getName() {
        return name;
    }

    public int getMaxTier() {
        return maxTier;
    }

    public boolean isSelfTeachable() {
        return selfTeachable;
    }

    protected void performSQLUpdate() {
        SaveExecutorManager.getInstance().submit(new ArcheMagicUpdateTask(this));
    }

    public void remove() {
        ArchePersonaHandler.getInstance().removeMagic(this);
        SaveExecutorManager.getInstance().submit(new ArcheMagicDeleteTask(name));
    }

}

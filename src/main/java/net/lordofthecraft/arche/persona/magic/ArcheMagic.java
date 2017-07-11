package net.lordofthecraft.arche.persona.magic;

import com.google.common.collect.Sets;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.SQLHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public static Optional<ArcheMagic> getMagicByName(String name) {
        return MAGICS.stream().filter(m -> m.name.equalsIgnoreCase(name)).findFirst();
    }

    public ArcheMagic(String name, int maxTier, boolean selfTeachable) {
        this.name = name;
        this.maxTier = maxTier;
        this.selfTeachable = selfTeachable;
    }

    public void setMaxTier(int maxTier) {
        this.maxTier = maxTier;
    }

    public void setSelfTeachable(boolean selfTeachable) {
        this.selfTeachable = selfTeachable;
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
}

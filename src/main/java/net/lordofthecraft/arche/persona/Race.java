package net.lordofthecraft.arche.persona;

import com.google.common.collect.Sets;
import net.lordofthecraft.arche.ArcheCore;
import org.jsoup.helper.Validate;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created on 3/22/2017
 *
 * @author 501warhead
 */
public class Race {

    /*
    CREATE TABLE IF NOT EXISTS races (
    race_key 		VARCHAR(255),
    max_age 		INT UNSIGNED,
    r_name 			TEXT,
    base_xp_mult 	DOUBLE(10,2) DEFAULT 1.0,
    luck_value 		DOUBLE(10,2) DEFAULT 0.0,
    special         BOOLEAN DEFAULT FALSE,
    parent_race 	VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (race_key)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;
     */
    static void init() {
        try {
            PreparedStatement statement = ArcheCore.getSQLControls().getConnection().prepareStatement("SELECT race_key,max_age,r_name,base_xp_mult,luck_value,special,parent_race FROM races");
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String key = rs.getString("race_key");
                int age = rs.getInt("max_age");
                String name = rs.getString("r_name");
                double xp_mult = rs.getDouble("base_xp_mult");
                double luck_value = rs.getDouble("luck_value");
                String parent = rs.getString("parent_race");
                boolean special = rs.getBoolean("special");
                Race r = new Race(name, key, age, xp_mult, luck_value, parent, special);

                races.add(r);
            }
        } catch (SQLException e) {
            ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "Failed to instantiate a race! This is bad!", e);
        }
    }

    @Deprecated
    public static Optional<Race> getOldRace(net.lordofthecraft.arche.enums.Race race) {
        return getRaceByKey(race.name());
    }

    public static Optional<Race> getRace(String name) {
        return races.parallelStream().filter(r -> r.name.equalsIgnoreCase(name)).findFirst();
    }

    public static Optional<Race> getRaceByKey(String key) {
        return races.parallelStream().filter(r -> r.race_id.equalsIgnoreCase(key)).findFirst();
    }

    public static Optional<Race> getOrCreateRace(String key) throws SQLException {
        Validate.notNull(key, "Race key cannot be null!");
        Optional<Race> or = races.parallelStream().filter(r -> r.race_id.equalsIgnoreCase(key)).findFirst();
        if (!or.isPresent()) {
            PreparedStatement stat = ArcheCore.getControls().getSQLHandler().getConnection().prepareStatement("SELECT * FROM races WHERE race_key=?");
            stat.setString(1, key);
            ResultSet rs = stat.executeQuery();

            if (rs.isBeforeFirst() && rs.next()) {
                int age = rs.getInt("max_age");
                String name = rs.getString("r_name");
                double xp_mult = rs.getDouble("base_xp_mult");
                double luck_value = rs.getDouble("luck_value");
                String parent = rs.getString("parent_race");
                boolean special = rs.getBoolean("special");
                Race r = null;
                if (parent != null && !parent.isEmpty()) {
                    Optional<Race> r_parent = getRaceByKey(parent);
                    r = r_parent.map(race -> new Race(name, key, age, xp_mult, luck_value, parent, special)).orElseGet(() -> new Race(name, key, age, xp_mult, luck_value, null, special));
                } else {
                    r = new Race(key.toLowerCase(),key,age,xp_mult,luck_value, null, special);
                }


                races.add(r);
                return Optional.of(r);
            } else {
                Race r = new Race(key.toLowerCase(), key);
                return Optional.of(r);
            }

        } else {
            return or;
        }
    }

    public static Set<Race> getRaces() {
        return Collections.unmodifiableSet(races);
    }

    private static Set<Race> races = Sets.newConcurrentHashSet();

    private final String name;
    private final String race_id;
    private final int maxAge;
    private final double xpMult;
    private final double luckModifier;
    private final boolean special;
    private final @Nullable
    String superRace;

    protected Race(String name, String racial_key) {
        this.name = name;
        this.race_id = racial_key;
        this.maxAge = 10000;
        this.xpMult = 1;
        this.luckModifier = 1;
        this.superRace = null;
        this.special = false;
    }

    public Race(String name, String race_id, int maxAge, double xpMult, double luckModifier, String superRace, boolean special) {
        this.name = name;
        this.race_id = race_id;
        this.maxAge = maxAge;
        this.xpMult = xpMult;
        this.luckModifier = luckModifier;
        this.superRace = superRace;
        this.special = special;
    }

    public String getName() {
        return name;
    }

    public String getRaceId() {
        return race_id;
    }

    public int getMaximumAge() {
        return maxAge;
    }

    public double getBaseXpMultiplier() {
        return xpMult;
    }

    public double getLuckModifier() {
        return luckModifier;
    }

    public boolean isSpecial() {
        return special;
    }

    public boolean idEquals(String id) {
        return race_id.equalsIgnoreCase(id);
    }

    public boolean superEquals(String superRace) {
        return this.superRace != null && this.superRace.equalsIgnoreCase(superRace);
    }

    public List<Race> getChildren() {
        return races.parallelStream().filter(race -> race.superEquals(race_id)).collect(Collectors.toList());
    }

    public boolean hasChildren() {
        return races.parallelStream().anyMatch(race -> race.superEquals(race_id));
    }

    @Nullable
    public String getSuperRace() {
        return superRace;
    }
}

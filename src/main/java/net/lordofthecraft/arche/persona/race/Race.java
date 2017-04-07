package net.lordofthecraft.arche.persona.race;

import com.google.common.collect.Sets;
import net.lordofthecraft.arche.ArcheCore;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

/**
 * Created on 3/22/2017
 *
 * @author 501warhead
 */
public class Race {

    public static Optional<Race> getRace(String name) {
        return races.parallelStream().filter(r -> r.name.equalsIgnoreCase(name)).findFirst();
    }

    public static Optional<Race> getOrCreateRace(String name) throws SQLException {
        Optional<Race> or = races.parallelStream().filter(r -> r.name.equalsIgnoreCase(name)).findFirst();
        if (!or.isPresent()) {
            PreparedStatement stat = ArcheCore.getControls().getSQLHandler().getConnection().prepareStatement("SELECT * FROM races WHERE race_key=?");
            stat.setString(1, name);
            ResultSet rs = stat.executeQuery();
            Race r = new Race(name);
            if (rs.isBeforeFirst() && rs.next()) {
                r.maxAge = rs.getInt("max_age");
                r.race_id = rs.getString("race_key");
                r.xpMult = rs.getDouble("base_xp_mult");
                r.luckModifier = rs.getDouble("luck_value");

                races.add(r);
                return Optional.of(r);
            } else {
                return Optional.of(r);
            }

        } else {
            return or;
        }
    }

    private static Set<Race> races = Sets.newConcurrentHashSet();

    private final String name;
    private String race_id;
    private int maxAge;
    private double xpMult;
    private double luckModifier;
    private @Nullable
    Race superRace;

    protected Race(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getRace_id() {
        return race_id;
    }

    public void setRace_id(String race_id) {
        this.race_id = race_id;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public double getXpMult() {
        return xpMult;
    }

    public void setXpMult(double xpMult) {
        this.xpMult = xpMult;
    }

    public double getLuckModifier() {
        return luckModifier;
    }

    public void setLuckModifier(double luckModifier) {
        this.luckModifier = luckModifier;
    }

    @Nullable
    public Race getSuperRace() {
        return superRace;
    }

    public void setSuperRace(@Nullable Race superRace) {
        this.superRace = superRace;
    }
}

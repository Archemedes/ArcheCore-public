package net.lordofthecraft.arche.persona;

import com.google.common.collect.Maps;
import net.lordofthecraft.arche.enums.AbilityScore;
import net.lordofthecraft.arche.interfaces.Persona;
import org.bukkit.ChatColor;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class PersonaAbilityScores {

    private EnumMap<AbilityScore, Integer> scores = new EnumMap<>(AbilityScore.class);
    private int unspentPoints = 0;
    private final Persona persona;

    public PersonaAbilityScores(Persona persona) {
        this.persona = persona;
    }

    public int getScore(AbilityScore score) {
        return scores.get(score);
    }

    public void setScore(AbilityScore score, int newScore) {
        if (scores.containsKey(score)) {
            scores.replace(score, newScore);
        } else {
            scores.put(score, newScore);
        }
    }

    public Map<AbilityScore, Integer> getScores() {
        return Collections.unmodifiableMap(scores);
    }

    public void setUnspentPoints(int unspentPoints) {
        this.unspentPoints = unspentPoints;
    }

    public void addRawPoint() {
        unspentPoints++;
    }

    public int getUnspentPoints() {
        return unspentPoints;
    }

    public Persona getPersona() {
        return persona;
    }

    public String getScoreString() {
        StringBuilder builder = new StringBuilder();
        String k = "";
        for (Map.Entry<AbilityScore, Integer> entry : scores.entrySet()) {
            builder.append(k);
            builder.append(entry.getKey().getIcon());
            builder.append(": ").append(ChatColor.RESET).append(entry.getValue());
            k = ChatColor.RESET+", ";
        }
        return builder.toString();
    }
}

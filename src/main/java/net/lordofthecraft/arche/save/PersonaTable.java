package net.lordofthecraft.arche.save;

/**
 * Created on 5/12/2017
 *
 * @author 501warhead
 */
public enum PersonaTable {

    MASTER("persona"),
    STATS("persona_stats"),
    VITALS("persona_vitals"),
    TAGS("persona_tags"),
    SKILLS("persona_skills");

    private final String table;

    PersonaTable(String table) {
        this.table = table;
    }

    public String getTable() {
        return table;
    }
}

package net.lordofthecraft.arche.save;

/**
 * Created on 5/12/2017
 *
 * @author 501warhead
 */
public enum PersonaTable {

    MASTER("persona"),
    WORLD("persona_world"),
    STATS("persona_stats"),
    EXTRAS("persona_extras"),
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

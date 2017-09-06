package net.lordofthecraft.arche.enums;

import org.bukkit.ChatColor;

/**
 * Created on 9/3/2017
 *
 * @author 501warhead
 */
public enum PersonaType {
    LORE("Lore", ChatColor.YELLOW + "((This is a notable Lore Character))"),
    STAFF("Staff", ChatColor.RED + "((This is a Staff Persona, used for Staff-related matters))"),
    EVENT("Event", ChatColor.DARK_GREEN + "((This is an Event Character))"),
    NORMAL("Normal", ChatColor.DARK_RED + "((Please remember not to metagame this information))");

    public final String readableName;
    public final String personaViewLine;

    PersonaType(String readableName, String personaViewLine) {
        this.readableName = readableName;
        this.personaViewLine = personaViewLine;
    }
}

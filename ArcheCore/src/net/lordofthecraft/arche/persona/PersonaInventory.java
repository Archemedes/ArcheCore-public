package net.lordofthecraft.arche.persona;

import org.bukkit.inventory.*;
import org.bukkit.configuration.file.*;
import org.bukkit.configuration.*;
import org.bukkit.entity.*;

public class PersonaInventory
{
    private final ItemStack[] armor;
    private final ItemStack[] contents;
    
    public static PersonaInventory restore(final String str) throws InvalidConfigurationException {
        final YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(str);
        final ItemStack[] armor = config.getList("armor").toArray(new ItemStack[0]);
        final ItemStack[] contents = config.getList("contents").toArray(new ItemStack[0]);
        return new PersonaInventory(armor, contents);
    }
    
    public static PersonaInventory store(final Player p) {
        final PersonaInventory result = new PersonaInventory(p.getInventory().getArmorContents(), p.getInventory().getContents());
        return result;
    }
    
    private PersonaInventory(final ItemStack[] armor, final ItemStack[] contents) {
        super();
        this.armor = armor;
        this.contents = contents;
    }
    
    public ItemStack[] getContents() {
        return this.contents;
    }
    
    public ItemStack[] getArmorContents() {
        return this.armor;
    }
    
    public String getAsString() {
        final YamlConfiguration config = new YamlConfiguration();
        config.set("armor", (Object)this.armor);
        config.set("contents", (Object)this.contents);
        return config.saveToString();
    }
}

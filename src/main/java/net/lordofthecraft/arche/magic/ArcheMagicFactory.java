package net.lordofthecraft.arche.magic;

import net.lordofthecraft.arche.interfaces.MagicFactory;
import org.bukkit.Material;

/**
 * Created on 7/12/2017
 *
 * @author 501warhead
 */
public class ArcheMagicFactory implements MagicFactory {

    /*
        private static Set<ArcheMagic> MAGICS = Sets.newConcurrentHashSet();
    private final String name;
    private int maxTier;
    private boolean selfTeachable;
    private String label;
    private String description;
    private boolean teachable;
    private int daysToMaxTier;
    private int daysToBonusTier;
     */


    private final String id;
    private int maxTier = 5;
    private boolean selfTeachable = false;
    private boolean teachable = true;
    private String name = "DEFAULT_MAGIC_NAME";
    private String description = null;
    private int daysToMaxTier = 120;
    private int daysToBonusTier = 0;
    private Material icon = null;

    ArcheMagicFactory(String id) {
        this.id = id;
    }

    public void register() {

    }
}

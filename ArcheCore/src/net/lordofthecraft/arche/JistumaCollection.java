package net.lordofthecraft.arche;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import net.lordofthecraft.arche.interfaces.JMisc;
import net.lordofthecraft.arche.interfaces.PersonaHandler;

public class JistumaCollection implements JMisc{

	private final PersonaHandler ph;
	
	public JistumaCollection(PersonaHandler ph) {
		this.ph = ph;
	}
	
	@Override
    public void noEvent(final Player p, final int time, final String name){
        String persona = "";
        if (ph.hasPersona(p)){
            persona = ph.getPersona(p).getName();
        }
        p.setMetadata(name, new FixedMetadataValue(ArcheCore.getPlugin(),persona));
        Bukkit.getScheduler().scheduleSyncDelayedTask(ArcheCore.getPlugin(), new Runnable(){
            @Override
            public void run() {
                if (p.hasMetadata(name)){
                    p.removeMetadata(name, ArcheCore.getPlugin());
                }
            }
        }, time);
    }
	
}

package net.lordofthecraft.arche.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.lordofthecraft.arche.event.PersonaActivateEvent;
import net.lordofthecraft.arche.event.PersonaDeactivateEvent.Reason;
import net.lordofthecraft.arche.event.PersonaRemoveEvent;
import net.lordofthecraft.arche.event.PersonaSwitchEvent;
import net.lordofthecraft.arche.interfaces.WhyPermissionHandler;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PersonaPermissionListener implements Listener{
	
	private WhyPermissionHandler handle;

	public PersonaPermissionListener(WhyPermissionHandler handler){
		handle = handler;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPersonaSwitch(PersonaSwitchEvent e){
		final PermissionUser user = PermissionsEx.getPermissionManager().getUser(e.getPlayer());
		for (String ss : handle.getPermissions(e.getOriginPersona()))
			user.removePermission(ss);
		
		for (String ss : handle.getPermissions(e.getPersona()))
			user.addPermission(ss);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPersonaPerma(PersonaRemoveEvent e){
		if (e.getReason() == Reason.REMOVE){
			handle.handlePerma(e.getPersona());
			if (e.getPersona().isCurrent()){
				final PermissionUser user = handle.getPexManager().getUser(e.getPlayer());
				for (String ss : handle.getPermissions(e.getPersona()))
					user.removePermission(ss);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPersonaActivate(PersonaActivateEvent e){
		final PermissionUser user = handle.getPexManager().getUser(e.getPlayer());
		if (e.getPersona().isCurrent()){
			for (String ss : handle.getPermissions(e.getPersona()))
				user.addPermission(ss);
		}
	}
	
}

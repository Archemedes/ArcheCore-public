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
		final PermissionUser user = PermissionsEx.getUser(e.getPlayer());
		String[] holder = handle.getPermissions(e.getOriginPersona());
		if (holder != null)
			for (String ss : holder)
				user.removePermission(ss);

		holder = handle.getPermissions(e.getPersona());
		if (holder != null)
			for (String ss : holder)
				if (!user.has(ss))
					user.addPermission(ss);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPersonaPerma(PersonaRemoveEvent e){
		if (e.getReason() == Reason.REMOVE){
			handle.handlePerma(e.getPersona());
			if (e.getPersona().isCurrent()){
				final PermissionUser user = PermissionsEx.getUser(e.getPlayer());
				final String[] holder = handle.getPermissions(e.getPersona());
				if (holder != null)
					for (String ss : holder)
						user.removePermission(ss);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPersonaActivate(PersonaActivateEvent e){
		final PermissionUser user = PermissionsEx.getUser(e.getPlayer());
		final String[] holder = handle.getPermissions(e.getPersona());
		if (holder != null)
			for (String ss : holder)
				if (!user.has(ss))
					user.addPermission(ss);
	}
}

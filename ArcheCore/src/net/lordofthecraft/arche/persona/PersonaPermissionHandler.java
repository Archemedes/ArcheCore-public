package net.lordofthecraft.arche.persona;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.google.common.collect.Maps;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.interfaces.WhyPermissionHandler;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import net.lordofthecraft.arche.interfaces.Persona;

public class PersonaPermissionHandler implements WhyPermissionHandler{

	private SQLHandler handler;
	private PermissionManager permHandler;

	public PersonaPermissionHandler(){
		this.handler = ArcheCore.getControls().getSQLHandler();
		this.permHandler = PermissionsEx.getPermissionManager();

		Map<String, String> cols = Maps.newLinkedHashMap();

		cols.put("player", "TEXT");
		cols.put("id", "INT");
		cols.put("perms", "TEXT");
		cols.put("UNIQUE (player, id)", "ON CONFLICT REPLACE");

		handler.createTable("persona_permissions", cols);
	}

	@Override
	public String[] getPermissions(Persona target){
		final String compiled = getRawPermissions(target);
		if (compiled != null)
			return compiled.split("@");
		else
			return null;
	}

	public String getRawPermissions(Persona target){
		ResultSet rs;
		try {
			rs = handler.query("SELECT perms FROM persona_permissions WHERE player = '"+target.getPlayerUUID().toString()+"' AND id = "+target.getId());
			return rs.getString(2);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean addPermission(Persona target, String permission){
		if (target.isCurrent()){
			permHandler.getUser(target.getPlayer()).addPermission(permission);
		}

		String formatted = getRawPermissions(target);

		if (formatted == null)
			formatted = permission;
		else
			formatted += "@"+permission;

		Map<String, Object> cols = Maps.newLinkedHashMap();

		cols.put("player", target.getPlayerUUID().toString());
		cols.put("id", target.getId());
		cols.put("perms", formatted);

		final int hold = handler.insert("persona_permissions", cols);
		if (hold < 0)
			return false;
		else
			return true;
	}

	@Override
	public boolean removePermission(Persona target, String permission){
		if (target.isCurrent()){
			permHandler.getUser(target.getPlayer()).removePermission(permission);
		}

		String raw = getRawPermissions(target);
		if (raw != null){
			if (!raw.contains(permission))
				return true;
			String[] list = raw.split("@");
			StringBuilder builder = new StringBuilder();
			String div = "";
			for (String ss : list){
				if (!ss.equalsIgnoreCase(permission)){
					builder.append(div);
					div = "@";
					builder.append(ss);
				}
			}

			handler.execute("UPDATE persona_permissions SET perms = '"+builder.toString()+"' WHERE player = '"+target.getPlayerUUID().toString()+"' AND id = "+target.getId());
			return true;
		} else
			return false;
	}

	@Override
	public boolean handlePerma(Persona killed){
		handler.execute("DELETE FROM persona_permissions WHERE player = '"+killed.getPlayerUUID().toString()+"' AND id = "+killed.getId());
		return true;
	}

	@Override
	public PermissionManager getPexManager(){ return permHandler; }

}

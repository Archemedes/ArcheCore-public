package net.lordofthecraft.arche.persona;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.google.common.collect.Maps;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.interfaces.WhyPermissionHandler;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import net.lordofthecraft.arche.interfaces.Persona;

public class PersonaPermissionHandler implements WhyPermissionHandler{

	private SQLHandler handler;
	private boolean permHandler;

	public PersonaPermissionHandler(){
		try {
			this.handler = ArcheCore.getControls().getSQLHandler();
			this.permHandler = ArcheCore.getControls().willUsePermissions();
			Map<String, String> cols = Maps.newLinkedHashMap();

			cols.put("player", "TEXT");
			cols.put("id", "INT");
			cols.put("perms", "TEXT");
			cols.put("UNIQUE (player, id)", "ON CONFLICT REPLACE");

			handler.createTable("persona_permissions", cols);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			rs = handler.query("SELECT * FROM persona_permissions WHERE player = '"+target.getPlayerUUID().toString()+"' AND id = "+target.getId());
			final String string = rs.getString(3);
			if (!rs.wasNull()){
				if (string == null){
					return null;
				} else if (string.length() < 1)
					return null;
				else
					return string;
			}
			return null;
		} catch (SQLException e) {
			return null;
		}
	}

	@Override
	public boolean addPermission(Persona target, String permission){
		if (hasPermission(target, permission)) return false;
		
		if (target.isCurrent() && permHandler){
			PermissionsEx.getUser(target.getPlayer()).addPermission(permission);
		}

		String[] formatted = getPermissions(target);
		StringBuilder builder = new StringBuilder();
		String tag = "";

		if (formatted != null){
			for (String ss : formatted){
				if (!ss.equalsIgnoreCase(permission)){
					builder.append(tag);
					tag = "@";
					builder.append(ss);
				}
			}
		}

		if (formatted == null)
			builder.append(permission);
		else
			builder.append(tag+permission);
		if (formatted == null){
			Map<String, Object> cols = Maps.newLinkedHashMap();

			cols.put("player", target.getPlayerUUID().toString());
			cols.put("id", target.getId());
			cols.put("perms", builder.toString());

			handler.insert("persona_permissions", cols);
		} else {
			handler.execute("UPDATE persona_permissions SET perms = '"+builder.toString()+"' WHERE player = '"+target.getPlayerUUID().toString()+"' AND id = "+target.getId());
		}
		return true;
	}

	@Override
	public boolean removePermission(Persona target, String permission){
		if (!hasPermission(target, permission)) return false;
		
		if (target.isCurrent() && permHandler){
			PermissionsEx.getUser(target.getPlayer()).removePermission(permission);
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
	public boolean hasPermission(Persona target, String permission){
		try {
			for (String ss : this.getPermissions(target)){
				if (ss.equalsIgnoreCase(permission))
					return true;
			}
			return false;
		} catch (NullPointerException e){
			return false;
		}
	}
	
	@Override
	public boolean handlePerma(Persona killed){
		handler.execute("DELETE FROM persona_permissions WHERE player = '"+killed.getPlayerUUID().toString()+"' AND id = "+killed.getId());
		return true;
	}

}

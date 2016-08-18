package net.lordofthecraft.arche.skill;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.interfaces.IArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.save.SaveHandler;
import net.lordofthecraft.arche.save.tasks.BonusExpModifierTask;

public class BonusExpModifierHandler {

	private static HashMap<Integer, BonusExpModifier> modifiers;
	private static SQLHandler handler;
	private static IArcheCore controls;
	private static int currentId;

	public BonusExpModifierHandler(SQLHandler sqlHandler, IArcheCore iArcheCore) {
		modifiers = Maps.newHashMap();
		handler = sqlHandler;
		controls = iArcheCore;
		initSQL();
		loadModifers();
		currentId = modifiers.size();
	}

	private static void loadModifers () {
		ResultSet res;
		try {
			res = handler.query("SELECT * FROM bonus_exp_modifiers");
			while(res.next()){
				BonusExpModifier modifier = buildModifier(res);
				if (modifier != null) if (!modifier.isExpired())
					modifiers.put(modifier.getId(), modifier);
			}	
		} catch (SQLException e) { e.printStackTrace();	}

	}
	
	public void addModifier(BonusExpModifier m) {
		modifiers.put(m.getId(), m);
		SaveHandler.getInstance().put(new BonusExpModifierTask(m, false));
	}
	
	public void endNow(BonusExpModifier m) {
		modifiers.remove(m);
		m.setDuration(System.currentTimeMillis() - m.getStartTime());
		SaveHandler.getInstance().put(new BonusExpModifierTask(m, true));
	}

	public HashMap<Integer, BonusExpModifier> getModifiers() {
		return modifiers;
	}

	public ArrayList<BonusExpModifier> getAccountModifiers(Player p) {
		ArrayList<BonusExpModifier> amodifiers = Lists.newArrayList();
		for (BonusExpModifier m : this.getModifiers().values()) 
			if (m.getType().equals(ExpModifier.ACCOUNT)) 
				if (m.getUUID().equals(p.getUniqueId()))
					amodifiers.add(m);
		return amodifiers;
	}

	public ArrayList<BonusExpModifier> getPersonaModifiers(Persona pers) {
		ArrayList<BonusExpModifier> amodifiers = Lists.newArrayList();
		for (BonusExpModifier m : this.getModifiers().values()) 
			if (m.getType().equals(ExpModifier.PERSONA)) 
				if (m.getUUID().equals(pers.getPlayerUUID()))
					if (m.getPersonaID() == pers.getId())
						amodifiers.add(m);
		return amodifiers;
	}

	public ArrayList<BonusExpModifier> getAllPersonaModifiers(Player p) {
		ArrayList<BonusExpModifier> amodifiers = Lists.newArrayList();
		for (BonusExpModifier m : this.getModifiers().values()) 
			if (m.getType().equals(ExpModifier.PERSONA)) 
				if (m.getUUID().equals(p.getUniqueId()))
					amodifiers.add(m);
		return amodifiers;
	}

	public ArrayList<BonusExpModifier> getGlobalModifiers() {
		ArrayList<BonusExpModifier> gmodifiers = Lists.newArrayList();
		for (BonusExpModifier m : this.getModifiers().values()) 
			if (m.getType().equals(ExpModifier.GLOBAL))
				gmodifiers.add(m);
		return gmodifiers;
	}
	
	public ArrayList<BonusExpModifier> getGlobalModifiersFromPlayer(Player p) {
		ArrayList<BonusExpModifier> gmodifiers = Lists.newArrayList();
		for (BonusExpModifier m : this.getModifiers().values()) 
			if (m.getType().equals(ExpModifier.GLOBAL))
				if (m.getUUID().equals(p.getUniqueId()))
					gmodifiers.add(m);
		return gmodifiers;
	}

	private static BonusExpModifier buildModifier(ResultSet res) {
		try {
			int mID = res.getInt(1);
			ExpModifier type = ExpModifier.valueOf(res.getString(2));
			long duration = res.getLong(3);
			long starttime = res.getLong(4);
			int startxp = res.getInt(5);
			int capxp = res.getInt(6);
			Skill skill = controls.getSkill(res.getString(7));
			UUID uuid;
			try {
				uuid = UUID.fromString(res.getString(8));
			} catch (Exception e) {
				uuid = null;
			}
			int id = res.getInt(9);
			double mod = res.getDouble(10);

			BonusExpModifier modifier = new BonusExpModifier(mID, type, uuid, id, skill, starttime, duration, startxp, capxp, mod);

			return modifier;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void initSQL() {
		Map<String, String> cols = Maps.newLinkedHashMap();
		
		cols.put("id", "INT NOT NULL PRIMARY KEY");
		cols.put("type", "TEXT NOT NULL");
		cols.put("duration", "REAL");
		cols.put("starttime", "REAL NOT NULL");
		cols.put("startxp", "INT");
		cols.put("capxp", "INT");
		cols.put("skill", "TEXT");
		cols.put("player", "TEXT");
		cols.put("pid", "INT");
		cols.put("multiplier", "REAL NOT NULL");

		handler.createTable("bonus_exp_modifiers", cols);
	}

	public static int nextId() {
		return currentId++;
	}

}

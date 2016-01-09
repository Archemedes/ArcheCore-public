package net.lordofthecraft.arche.skill;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;

public class TopData {

	private static LinkedHashMap<Skill, List<Persona>> topList;
	private static LinkedHashMap<Persona, Double> topMoney;
	private static final String tag = "[ArcheCoreTop] ";
	
	public TopData() {
		if (topList != null) {
			throw new IllegalStateException("Top list already declared!");
		} else {
			topList = Maps.newLinkedHashMap();
			for (Entry<String, ArcheSkill> e : ArcheSkillFactory.getSkills().entrySet()) {
				topList.put(e.getValue(), getTopSkills(e.getValue()));
			}
		}
		if (topMoney == null) {
			topMoney = getTop();
		}
	}
	
	public List<Persona> getTopList(Skill sk) {
		return Collections.unmodifiableList(topList.get(sk));
	}
	
	public LinkedHashMap<Persona, Double> getTopMoney() {
		return topMoney;
	}
	
	public void registerTop(ArcheSkill sk) {
		topList.put(sk, getTopSkills(sk));
	}
	
	private LinkedHashMap<Persona, Double> getTop() {
		LinkedHashMap<Persona, Double> top = Maps.newLinkedHashMap();
		ResultSet rs;
		try {
			rs = ArcheCore.getControls().getSQLHandler().query("SELECT * FROM accs ORDER BY money DESC");
			int count = 0;
			Persona pers;
			while (rs.next() && count < 10) {
				pers = ArcheCore.getControls().getPersonaHandler().getPersona(UUID.fromString(rs.getString(1)), rs.getInt(2));
				if (pers != null && pers.getPlayer() != null) {
					if (!isMod(pers.getPlayer()) || ArcheCore.getPlugin().debugMode()) {
						++count;
						top.put(pers, rs.getDouble(3));
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return top;
	}
	
	private boolean isMod(CommandSender sender){
		return sender.hasPermission("archecore.admin") || sender.hasPermission("archecore.mod.economy");
	}
	
	private List<Persona> getTopSkills(ArcheSkill sk) {
		System.out.println(tag+"Getting top for "+sk.getName());
		ResultSet rs;
		List<Persona> top = Lists.newArrayList();
		try {
			System.out.println(tag+"Statement is "+"SELECT * FROM sk_"+sk.getName().toLowerCase()+" ORDER BY xp DESC");
			rs = ArcheCore.getControls().getSQLHandler().query("SELECT * FROM sk_"+sk.getName().toLowerCase()+" ORDER BY xp DESC");
			int count = 0;
			Persona hold;
			while (rs.next() && count < 10) {
				System.out.println(tag+"Count is "+count);
				if (rs.getInt(3) > 0) {
					System.out.println(tag+"Xp is not 0");
					if (!Bukkit.getOfflinePlayer(UUID.fromString(rs.getString(1))).isOp() || ArcheCore.getPlugin().debugMode()){
						System.out.println(tag+"The UUID "+rs.getString(1)+" is not opped");
						hold = ArcheCore.getControls().getPersonaHandler().getPersona(UUID.fromString(rs.getString(1)), rs.getInt(2));
						if (hold != null) {
							System.out.println(tag+"The persona for "+hold.getPlayerName()+" was not null. adding it with the xp "+rs.getInt(3));
							top.add(hold);
							++count;
						}
					}
				}
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return top;
	}
}

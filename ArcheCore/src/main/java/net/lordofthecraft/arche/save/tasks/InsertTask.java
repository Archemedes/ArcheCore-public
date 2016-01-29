package net.lordofthecraft.arche.save.tasks;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.lordofthecraft.arche.enums.Race;

public class InsertTask extends ArcheTask {
	private static PreparedStatement stat = null;
	
	private final String player;
	private final int id;
	private final String name;
	private final int age;
	private final Race race;
	private final int gender;
	private final boolean autoage;
	
	public InsertTask(String player, int id, String name, int age, Race race, int gender, boolean autoage){
		this.player = player;
		this.id = id;
		this.name = name;
		this.age = age;
		this.race = race;
		this.gender = gender;
		this.autoage = autoage;
	}
	
	@Override
	public void run() {
		try{
			if(stat == null) stat = handle.getSQL().getConnection()
				.prepareStatement("INSERT INTO persona (player,id,name,age,race,gender,autoage) VALUES (?,?,?,?,?,?,?)");
				
			stat.setString(1,player);
			stat.setInt(2, id);
			stat.setString(3,name);
			stat.setInt(4, age);
			stat.setString(5, race.toString());
			stat.setInt(6,gender);
			stat.setBoolean(7, autoage);
			
			stat.execute();
		}catch(SQLException e){e.printStackTrace();}	
	}

}

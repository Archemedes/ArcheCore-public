package net.lordofthecraft.arche.save.tasks;

import net.lordofthecraft.arche.enums.Race;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertTask extends ArcheTask {
	private static PreparedStatement stat = null;
	
	private final String player;
	private final int id;
	private final String name;
	private final Race race;
	private final int gender;
	private final long creationTime;
	
	public InsertTask(String player, int id, String name, Race race, int gender, long creationTime){
		this.player = player;
		this.id = id;
		this.name = name;
		this.race = race;
		this.gender = gender;
		this.creationTime = creationTime;
	}
	
	//TODO will need updating
	
	@Override
	public void run() {
		try{
			if (stat == null) stat = handle.getConnection()
				.prepareStatement("INSERT INTO persona (player,id,name,age,race,gender,autoage,stat_creation) VALUES (?,?,?,?,?,?,?,?)");
				
			stat.setString(1,player);
			stat.setInt(2, id);
			stat.setString(3,name);
			//stat.setInt(4, age);
			stat.setString(5, race.toString());
			stat.setInt(6,gender);
			//stat.setBoolean(7, autoage);
			stat.setLong(8,creationTime);

			stat.execute();
		}catch(SQLException e){e.printStackTrace();}	
	}

}

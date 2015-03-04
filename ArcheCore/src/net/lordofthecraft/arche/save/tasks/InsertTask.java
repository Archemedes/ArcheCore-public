package net.lordofthecraft.arche.save.tasks;

import net.lordofthecraft.arche.enums.*;
import java.sql.*;

public class InsertTask extends ArcheTask
{
    private static PreparedStatement stat;
    private final String player;
    private final int id;
    private final String name;
    private final int age;
    private final Race race;
    private final int gender;
    private final boolean autoage;
    
    public InsertTask(final String player, final int id, final String name, final int age, final Race race, final int gender, final boolean autoage) {
        super();
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
        try {
            if (InsertTask.stat == null) {
                InsertTask.stat = InsertTask.handle.getSQL().getConnection().prepareStatement("INSERT INTO persona (player,id,name,age,race,gender,autoage) VALUES (?,?,?,?,?,?,?)");
            }
            InsertTask.stat.setString(1, this.player);
            InsertTask.stat.setInt(2, this.id);
            InsertTask.stat.setString(3, this.name);
            InsertTask.stat.setInt(4, this.age);
            InsertTask.stat.setString(5, this.race.toString());
            InsertTask.stat.setInt(6, this.gender);
            InsertTask.stat.setBoolean(7, this.autoage);
            InsertTask.stat.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    static {
        InsertTask.stat = null;
    }
}

package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.enums.Race;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Created on 9/3/2017
 *
 * @author 501warhead
 */
public class ArchePersonaCreateCallable implements Callable<ArchePersona> {

    private final UUID player;
    private final int slot;
    private final int gender;
    private final Race race;
    private final String name;
    private final Timestamp creationtime;

    public ArchePersonaCreateCallable(UUID player, int slot, int gender, Race race, String name, Timestamp creationtime) {
        this.player = player;
        this.slot = slot;
        this.gender = gender;
        this.race = race;
        this.name = name;
        this.creationtime = creationtime;
    }

    @Override
    public ArchePersona call() throws Exception {
        SQLHandler handler = ArcheCore.getSQLControls();
        PreparedStatement insertPrimary = handler.getConnection().prepareStatement("INSERT INTO persona(name,slot,");
        return null;
    }
}

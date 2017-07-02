package net.lordofthecraft.arche.save.tasks;

import net.lordofthecraft.arche.interfaces.Persona;

import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * Created on 5/8/2017
 *
 * @author 501warhead
 */
public class PersonaDeleteTask extends ArcheTask {

    private static CallableStatement deleteStat = null;

    private final Persona toDelete;

    public PersonaDeleteTask(Persona toDelete) {
        this.toDelete = toDelete;
    }

    @Override
    public void run() {
        try {
            if (deleteStat == null) {
                deleteStat = handle.getConnection().prepareCall("{call persona_delete(?, ?)}");
            }
            deleteStat.clearParameters();
            deleteStat.setString(1, toDelete.getPlayerUUID().toString());
            deleteStat.setInt(2, toDelete.getId());
            deleteStat.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

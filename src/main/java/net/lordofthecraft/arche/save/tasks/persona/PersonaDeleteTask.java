package net.lordofthecraft.arche.save.tasks.persona;

import net.lordofthecraft.arche.SQL.WhySQLHandler;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.tasks.ArcheTask;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created on 5/8/2017
 *
 * @author 501warhead
 */
public class PersonaDeleteTask extends ArcheTask {

    private static CallableStatement deleteStat = null;

    private final UUID toDelete;

    public PersonaDeleteTask(Persona toDelete) {
        this.toDelete = toDelete.getPersonaId();
    }

    @Override
    public void run() {
        try {
            if (deleteStat == null) {
                deleteStat = handle.getConnection().prepareCall("{call persona_delete(?)}");
            }
            deleteStat.clearParameters();
            deleteStat.setString(1, toDelete.toString());
            deleteStat.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closeConnection() {
        if (deleteStat == null) {
            return;
        }
        try {
            deleteStat.closeOnCompletion();
            if (handle instanceof WhySQLHandler) {
                deleteStat.getConnection().close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

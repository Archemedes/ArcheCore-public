package net.lordofthecraft.arche.save.tasks.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.save.tasks.ArcheTask;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * @author 501warhead
 */
public class PersonaDeleteCallTask extends ArcheTask {

    private static CallableStatement personaDelete = null;

    private final int persona_id;

    public PersonaDeleteCallTask(int persona_id) {
        if (personaDelete == null) {
            try {
                personaDelete = handle.getConnection().prepareCall("{call delete_persona(?)}");
            } catch (SQLException e) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "We failed to create the personaDelete CallableStatement! Personas wont be able to be deleted!", e);
            }
        }
        this.persona_id = persona_id;
    }

    @Override
    public void run() {
        if (personaDelete == null) {
            ArcheCore.getPlugin().getLogger().severe("The persona delete call was not initialized and a persona with the id of " + persona_id + " was attempted to be deleted.");
            return;
        }
        try {
            personaDelete.clearParameters();
            personaDelete.setInt(1, persona_id);
            personaDelete.execute();
        } catch (SQLException e) {
            ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "We failed to call the delete for the persona with the id of " + persona_id + "! THIS WILL CAUSE ISSUES!", e);
        }

    }
}

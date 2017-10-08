package net.lordofthecraft.arche.save.rows;

import net.lordofthecraft.arche.interfaces.OfflinePersona;

public interface ArchePersonaRow extends ArcheRow {

    OfflinePersona[] getPersonas();
}

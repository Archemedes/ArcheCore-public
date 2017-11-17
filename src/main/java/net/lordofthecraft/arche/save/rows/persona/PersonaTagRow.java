package net.lordofthecraft.arche.save.rows.persona;

import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.persona.TagAttachment;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;

public class PersonaTagRow extends SingleStatementRow {
    private final OfflinePersona persona;
    private final TagAttachment att;

    public PersonaTagRow(OfflinePersona persona, TagAttachment att) {
        this.persona = persona;
        this.att = att;
    }

    @Override
    protected String getStatement() {
        return "REPLACE INTO persona_tags(persona_id_fk,tag_key,tag_value,offline) VALUES (?,?,?,?)";
    }

    @Override
    protected Object getValueFor(int index) {
        switch (index) {
            case 1:
                return persona.getPersonaId();
            case 2:
                return att.getKey();
            case 3:
                return att.getValue();
            default:
                throw new IllegalArgumentException();
        }
    }

}

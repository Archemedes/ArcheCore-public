package net.lordofthecraft.arche.save.rows.attribute;

import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;

import java.util.UUID;

public class AttributeRemoveRow extends SingleStatementRow {

    final UUID mod_uuid;
    final ArcheAttribute attribute;
    final Persona persona;

    public AttributeRemoveRow(ExtendedAttributeModifier mod, ArcheAttribute attribute, Persona persona) {
        this.mod_uuid = mod.getUniqueId();
        this.attribute = attribute;
        this.persona = persona;
    }

    @Override
    protected String getStatement() {
        return "DELETE FROM persona_attributes WHERE mod_uuid=? AND persona_id_fk=? AND attribute_type=?";
    }

    @Override
    protected Object getValueFor(int index) {
        switch (index) {
            case 1:
                return mod_uuid;
            case 2:
                return persona.getPersonaId();
            case 3:
                return attribute.getName();
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return "AttributeRemoveRow{" +
                "mod=" + mod_uuid +
                ", attribute=" + attribute +
                ", persona=" + persona.identify() +
                '}';
    }
}

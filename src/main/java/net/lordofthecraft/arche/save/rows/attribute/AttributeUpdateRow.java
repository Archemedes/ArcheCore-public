package net.lordofthecraft.arche.save.rows.attribute;

import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;
import net.lordofthecraft.arche.util.MessageUtil;

public class AttributeUpdateRow extends SingleStatementRow {

    final ExtendedAttributeModifier mod;
    final Persona persona;
    final ArcheAttribute attribute;

    public AttributeUpdateRow(ExtendedAttributeModifier mod, Persona persona, ArcheAttribute attribute) {
        this.mod = mod.clone();
        this.persona = persona;
        this.attribute = attribute;
    }
    
	@Override
	protected String getStatement() {
		return "UPDATE persona_attributes SET decaytype=?, decayticks=?, lostondeath=? WHERE persona_id_fk=? AND mod_uuid=? AND attribute_type=?";
	}

	@Override
	protected Object getValueFor(int index) {
		switch(index) {
		case 1: return mod.getDecayStrategy().name();
		case 2: return mod.getTicksRemaining();
		case 3: return mod.isLostOnDeath();
		case 4: return persona.getPersonaId();
		case 5: return mod.getUniqueId();
		case 6: return attribute.getName();
		default: throw new IllegalArgumentException();
		}
	}
	
    @Override
    public String toString() {
        return "AttributeUpdateRow{" +
                "mod=" + mod +
                ", persona=" + MessageUtil.identifyPersona(persona) +
                ", attribute=" + attribute +
                '}';
    }

}

package net.lordofthecraft.arche.save.rows.attribute;

import java.sql.Timestamp;

import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;
import net.lordofthecraft.arche.util.MessageUtil;

public class AttributeInsertRow extends SingleStatementRow {
    private final ExtendedAttributeModifier mod;
    private final Persona persona;
    private final ArcheAttribute attribute;
    private final Timestamp now;
    
    public AttributeInsertRow(ExtendedAttributeModifier mod, Persona persona, ArcheAttribute attribute) {
        this.mod = mod.clone();
        this.persona = persona;
        this.attribute = attribute;
        now = now();
    }
    
	@Override
	protected String getStatement() {
		return "INSERT INTO persona_attributes(mod_uuid,persona_id_fk,attribute_type,mod_name,mod_value,operation,created,decayticks,decaytype,lostondeath) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?)";
	}

	@Override
	protected Object getValueFor(int index) {
		switch(index) {
		case 1: return mod.getUniqueId();
		case 2: return persona.getPersonaId();
		case 3: return attribute.getName();
		case 4: return mod.getName();
		case 5: return mod.getAmount();
		case 6: return mod.getOperation().name();
		case 7: return now;
		case 8: return mod.getTicksRemaining();
		case 9: return mod.getDecayStrategy().name();
		case 10: return mod.isLostOnDeath();
		default: throw new IllegalArgumentException();
		}
	}
	
    @Override
    public String toString() {
        return "AttributeInsertRow{" +
                "mod=" + mod +
                ", persona=" + MessageUtil.identifyPersona(persona) +
                ", attribute=" + attribute +
                '}';
    }

}

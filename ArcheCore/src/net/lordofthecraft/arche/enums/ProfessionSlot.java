package net.lordofthecraft.arche.enums;

import net.lordofthecraft.arche.save.*;

public enum ProfessionSlot
{
    PRIMARY(PersonaField.SKILL_PRIMARY), 
    SECONDARY(PersonaField.SKILL_SECONDARY), 
    ADDITIONAL(PersonaField.SKILL_ADDITIONAL);
    
    private final PersonaField field;
    
    private ProfessionSlot(final PersonaField field) {
        this.field = field;
    }
    
    public PersonaField getPersonaField() {
        return this.field;
    }
}

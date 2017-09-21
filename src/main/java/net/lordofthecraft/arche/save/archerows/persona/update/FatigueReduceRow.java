package net.lordofthecraft.arche.save.archerows.persona.update;

import net.lordofthecraft.arche.save.archerows.ArcheRow;

public class FatigueReduceRow implements ArcheRow {

    public FatigueReduceRow() {
    }

    @Override
    public String[] getInserts() {
        return new String[]{"UPDATE persona SET fatigue=fatigue-1 WHERE fatigue>0;", "UPDATE persona SET fatigue=0 WHERE fatigue<0;"};
    }


    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}

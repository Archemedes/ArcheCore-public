package net.lordofthecraft.arche.skill;

public class SkillData {

	public final double xp;
	public final boolean visible;
    public final int slot;

    public SkillData(double xp, boolean visible, int slot) {
        this.xp = xp;
		this.visible = visible;
        this.slot = slot;
    }
}

package net.lordofthecraft.arche.attributes;

import org.bukkit.entity.Player;

import net.lordofthecraft.arche.ArcheCore;

public class AttributeMaxFatigue extends ArcheAttribute {

	AttributeMaxFatigue(String name, double defaultValue) {
		super(name, defaultValue);
	}

	@Override
	public void calibrate(Player p, double value) {
		ArcheCore.getControls().getFatigueHandler().showFatigueBar(p);
	}
}

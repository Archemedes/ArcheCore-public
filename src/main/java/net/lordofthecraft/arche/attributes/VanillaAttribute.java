package net.lordofthecraft.arche.attributes;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import net.lordofthecraft.arche.interfaces.Persona;

public final class VanillaAttribute extends ArcheAttribute{
	private final Attribute handle;
		
	public VanillaAttribute(String name, double defaultValue, Attribute handle) {
		super(name, defaultValue);
		this.handle = handle;
	}
	
	public Attribute getHandle() { return handle;}
	
	@Override
	public void tryApply(ArcheAttributeInstance instance) {
		Persona p = instance.getPersona().getPersona();
		if(p != null && p.isCurrent()) {
			Player player = p.getPlayer();
			if(player != null) {
				AttributeInstance under = player.getAttribute(handle);
				Set<UUID> list = under.getModifiers().stream().map(AttributeModifier::getUniqueId).collect(Collectors.toSet());
						
				instance.getModifiers().stream()
				.filter(a -> !list.contains(a.getUniqueId()))
				.forEach(under::addModifier);
			}
		}
	}
}

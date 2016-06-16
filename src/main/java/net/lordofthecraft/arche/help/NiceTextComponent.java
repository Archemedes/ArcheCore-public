package net.lordofthecraft.arche.help;

import net.md_5.bungee.api.chat.TextComponent;

public class NiceTextComponent extends TextComponent {

	
	@Override
	public String toString(){
		System.out.println("Holla from NiceTextComponent!");
		new RuntimeException().printStackTrace();
		return "Fuck this!";
	}
}

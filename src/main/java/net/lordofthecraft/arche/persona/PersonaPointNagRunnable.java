package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.attributes.AttributeRegistry;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import net.lordofthecraft.arche.util.MessageUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class PersonaPointNagRunnable implements Runnable {

    private final PersonaHandler handler;

    public PersonaPointNagRunnable(PersonaHandler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        handler.getPersonas()
                .parallelStream()
                .filter(aop -> aop.isLoaded() && aop.isCurrent())
                .map(ArchePersona.class::cast)
                .filter(p -> ((int) p.attributes().getAttributeValue(AttributeRegistry.SCORE_UNSPENT)) > 0 && p.getPlayer() != null)
                .forEach(PersonaPointNagRunnable::nag);
    }

    public static void nag(Persona p) {
        TextComponent component = new TextComponent(ChatColor.AQUA + "You have " + ChatColor.GOLD + ((int) p.attributes().getAttributeValue(AttributeRegistry.SCORE_UNSPENT) + ChatColor.AQUA.toString() + " unspent points! Click "));
        component.addExtra(MessageUtil.CommandButton("here", "/persona points spend", "Click me to spend points!"));
        component.addExtra(new TextComponent(ChatColor.AQUA + " to spend them!"));
        p.getPlayer().spigot().sendMessage(component);
    }
}

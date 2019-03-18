package net.lordofthecraft.arche.commands;

import co.lotc.core.command.CommandTemplate;
import co.lotc.core.command.annotate.Cmd;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.seasons.LotcianCalendar;

public class CommandDate extends CommandTemplate
{
    private final LotcianCalendar calendar;
    
    public CommandDate() {
        calendar = ArcheCore.getControls().getCalendar();
    }
    
    @Override
		public void invoke() {
    	msg(calendar.toPrettyString());
    }
    
    @Cmd("Real Date")
    public void real() {
    	msg(calendar.toString());
    }
}


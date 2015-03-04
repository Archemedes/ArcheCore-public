package net.lordofthecraft.arche.enums;

public enum ChatBoxAction
{
    SHOW_TEXT, 
    SHOW_ITEM, 
    SHOW_ACHIEVEMENT, 
    RUN_COMMAND, 
    SUGGEST_COMMAND, 
    OPEN_URL, 
    OPEN_FILE;
    
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}

package net.lordofthecraft.arche.enums;

public enum ChatBoxAction {
	
	CHANGE_PAGE,
	SHOW_TEXT,
	SHOW_ITEM,
	SHOW_ENTITY,
	SHOW_ACHIEVEMENT,
	RUN_COMMAND,
	SUGGEST_COMMAND,
	OPEN_URL,
	OPEN_FILE;
	
	public String toString(){
		return name().toLowerCase();
	}

}

package net.lordofthecraft.arche.menu;

public interface Folder {

	
	int size();
	
	void addButton(Button button);
	void setButton(int slot, Button button);
	void removeButton(Button button);
	
	void setTitle(String title);
	String getTitle();
}

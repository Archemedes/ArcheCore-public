package net.lordofthecraft.arche.menu;

public interface Folder {

	
	int size();
	
	void addButton(Button button);
	void setButton(int slot, Button button);
	void removeButton(Button button);
	
	String getTitle();

	void setTitle(String title);
}

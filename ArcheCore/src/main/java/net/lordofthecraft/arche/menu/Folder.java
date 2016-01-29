package net.lordofthecraft.arche.menu;

public interface Folder {

	
	public int size();
	
	public void addButton(Button button);
	public void setButton(int slot, Button button);
	public void removeButton(Button button);
	
	public void setTitle(String title);
	public String getTitle();
}

package net.lordofthecraft.arche.menu;

import java.util.List;
import java.util.Map;

public interface Button {

	
	public Map<String, Object> getContext();
	
	public void onClick();
	
	public String getName();
	
	public String setName();
	
	public List<String> getDescription();
	public void setDescription();
}

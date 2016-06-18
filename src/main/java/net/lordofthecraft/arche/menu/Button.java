package net.lordofthecraft.arche.menu;

import java.util.List;
import java.util.Map;

public interface Button {

	
	Map<String, Object> getContext();
	
	void onClick();
	
	String getName();
	
	String setName();
	
	List<String> getDescription();
	void setDescription();
}

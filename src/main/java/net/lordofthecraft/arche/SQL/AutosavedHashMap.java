package net.lordofthecraft.arche.SQL;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.*;
import java.util.HashMap;

public class AutosavedHashMap<V extends Serializable> extends HashMap<String, V>{
	
	private static final long serialVersionUID = 8033169651578143510L;
	private final String name;
	private final Connection con;
	private final boolean reflective;
	
	AutosavedHashMap(Connection con, String name, boolean reflective){
		super();
		this.con = con;
		this.name = name;
		this.reflective = reflective;
	}
	
	@SuppressWarnings("unchecked")
	public V loadValue(String key){
		if(reflective) throw new UnsupportedOperationException("Can't load for reflective maps.");
		
		ObjectInputStream objectIn = null;
		try{
			Statement state = con.createStatement();
			V result = null;
			ResultSet res = state.executeQuery("SELECT value FROM " + name + "WHERE key='" + key + "';");
			if(res.next()){
			    byte[] buf = res.getBytes(1);
			    
			    if (buf != null){
			      objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));
			      result = (V) objectIn.readObject();
			      super.put(key, result);
			    } else throw new SQLException();

			}

			res.close();
			state.close();
			if(objectIn != null) objectIn.close();
			return result;
			
		}catch(Exception e){e.printStackTrace();}
		
		
		return null;
	}
	
	@Override
	public V put(String key, V value){
		try{
			PreparedStatement pstm = con.prepareStatement("INSERT INTO " + name + "(key, value) VALUES (?, ?)");
			pstm.setString(1, key);
			pstm.setObject(2, value);
			
			pstm.executeUpdate();
			pstm.close();
			
			return super.put(key, value);
		}catch(SQLException e){e.printStackTrace();}
		
		return null;
	}
	
	@Override
	public V remove(Object o){
		if(reflective){
			if(!(o instanceof String)) return null;
			String key = (String) o;
			return removeFromTable(key);
		}else return super.remove(o);
	}
	
	public V removeFromTable(String key){
		try{
			Statement state = con.createStatement();
			state.executeQuery("DELETE FROM " + name + "WHERE key='" + key + "';");
			state.close();
			
			return super.remove(key);
		}catch(SQLException e){e.printStackTrace();}
		return null;
	}
	
	@Override
	public void clear(){
		if(reflective){
			clearTable();
		} else {
			super.clear();
		}
	}
	
	public void clearTable(){
		try{
		Statement state = con.createStatement();
		state.executeQuery("DELETE FROM " + name + ";");
		state.close();
		}catch(SQLException e){e.printStackTrace();}
		
		super.clear();
	}
	
	
}

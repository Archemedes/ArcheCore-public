package net.lordofthecraft.arche.SQL;

/**
 * Represents an object that will be treated as syntax, not string, by
 * the ArcheSQLiteHandler object, meaning it will not wrap the string in single quotes.
 */
public class Syntax {
	private final String syntax;
	
	
	public Syntax(String syntax){
		this.syntax = syntax;
	}
	
	@Override
	public String toString(){
		return syntax;
	}
	
}

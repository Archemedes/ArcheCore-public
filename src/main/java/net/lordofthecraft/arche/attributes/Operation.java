package net.lordofthecraft.arche.attributes;

@Deprecated
public enum Operation {
	INCREMENT (0),
	MULTIPLY (1),
	MULTIPLY_ALL (2);
	
	private final int value;

	Operation(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}

package net.lordofthecraft.arche.attributes;

public enum Operation
{
    INCREMENT(0), 
    MULTIPLY(1), 
    MULTIPLY_ALL(2);
    
    private final int value;
    
    private Operation(final int value) {
        this.value = value;
    }
    
    public int getValue() {
        return this.value;
    }
}

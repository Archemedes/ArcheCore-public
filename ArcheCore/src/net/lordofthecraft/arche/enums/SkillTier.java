package net.lordofthecraft.arche.enums;

public enum SkillTier
{
    RUSTY(0, Integer.MIN_VALUE, "Inept"), 
    INEXPERIENCED(1, -3000, "Promising"), 
    BUNGLING(2, 250, "Bungling"), 
    CLUMSY(3, 600, "Clumsy"), 
    APPRENTICE(4, 1500, "Apprentice"), 
    FAIR(5, 3000, "Fair"), 
    ADEQUATE(6, 6500, "Adequate"), 
    PROFICIENT(7, 14000, "Proficient"), 
    ADEPT(8, 30000, "Adept"), 
    VETERAN(9, 70000, "Veteran"), 
    MASTERFUL(10, 150000, "Masterful"), 
    LEGENDARY(11, 350000, "Legendary"), 
    AENGULIC(12, 1000000, "Aengulic");
    
    final int xp;
    final int tier;
    final String title;
    
    private SkillTier(final int tier, final int xp, final String title) {
        this.xp = xp;
        this.tier = tier;
        this.title = title;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public boolean achieved(final SkillTier other) {
        return this.compareTo(other) >= 0;
    }
    
    public SkillTier getNext() {
        return (this.ordinal() < values().length - 1) ? values()[this.ordinal() + 1] : null;
    }
    
    public int getTier() {
        return this.tier;
    }
    
    public int getXp() {
        return this.xp;
    }
}

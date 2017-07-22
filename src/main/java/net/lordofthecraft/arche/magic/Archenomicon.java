package net.lordofthecraft.arche.magic;

/**
 * Created on 7/12/2017
 *
 * @author 501warhead
 */
public class Archenomicon {
    private static Archenomicon ourInstance = new Archenomicon();

    public static Archenomicon getInstance() {
        return ourInstance;
    }

    private Archenomicon() {
    }
}

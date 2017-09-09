package net.lordofthecraft.arche.magic;

import net.lordofthecraft.arche.interfaces.Magic;

/**
 * Created on 7/3/2017
 *
 * @author 501warhead
 */
public class MagicData {

    public final Magic magic;
    public final int tier;
    public final boolean visible;
    public final boolean taught;
    public final Integer teacher;
    public final long learned;
    public final long lastAdvanced;

    public MagicData(Magic magic, int tier, boolean visible, boolean taught, Integer teacher, long learned, long lastAdvanced) {
        this.magic = magic;
        this.tier = tier;
        this.visible = visible;
        this.taught = taught;
        this.teacher = teacher;
        this.learned = learned;
        this.lastAdvanced = lastAdvanced;
    }
}

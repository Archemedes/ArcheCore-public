package net.lordofthecraft.arche.magic;

import java.util.UUID;

/**
 * Created on 7/3/2017
 *
 * @author 501warhead
 */
public class MagicData {

    public final ArcheMagic magic;
    public final int id;
    public final int tier;
    public final boolean visible;
    public final boolean taught;
    public final UUID teacher;
    public final long learned;
    public final long lastAdvanced;

    public MagicData(ArcheMagic magic, int id, int tier, boolean visible, boolean taught, UUID teacher, long learned, long lastAdvanced) {
        this.magic = magic;
        this.id = id;
        this.tier = tier;
        this.visible = visible;
        this.taught = taught;
        this.teacher = teacher;
        this.learned = learned;
        this.lastAdvanced = lastAdvanced;
    }
}

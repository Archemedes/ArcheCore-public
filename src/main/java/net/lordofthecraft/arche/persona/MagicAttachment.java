package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.interfaces.Magic;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.magic.MagicData;
import net.lordofthecraft.arche.save.SaveHandler;
import net.lordofthecraft.arche.save.tasks.magic.MagicUpdateTask;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created on 7/3/2017
 *
 * @author 501warhead
 */
public class MagicAttachment {
    private static final SaveHandler buffer = SaveHandler.getInstance();
    private final Magic magic;
    private final int id;
    private final int persona_id;
    //private FutureTask<MagicData> call;
    private int tier;
    private AtomicBoolean visible;
    private AtomicBoolean taught;
    private int teacher;
    private long learned;
    private long lastAdvanced;


    public MagicAttachment(Magic magic, int persona_id, MagicData data) {
        this.magic = magic;
        this.persona_id = persona_id;
        id = data.id;
        tier = data.tier;
        visible = new AtomicBoolean(data.visible);
        taught = new AtomicBoolean(data.taught);
        teacher = data.teacher;
        learned = data.learned;
        lastAdvanced = data.lastAdvanced;

    }

    public void setTier(int i) {
        if (i > magic.getMaxTier()) {
            i = magic.getMaxTier();
        }
        this.tier = i;
        this.lastAdvanced = System.currentTimeMillis();
        performSQLUpdate();
    }

    public void setVisible(boolean b) {
        visible.set(b);
        performSQLUpdate();
    }

    public void setTaughtBy(Persona p) {
        taught.set(true);
        teacher = p.getPersonaId();
        performSQLUpdate();
    }

    public void clearTeacher() {
        taught.set(false);
        teacher = -1;
        performSQLUpdate();
    }

    public int getId() {
        return id;
    }

    public Magic getMagic() {
        return magic;
    }

    public int getPersonaId() {
        return persona_id;
    }

    public int getTier() {
        return tier;
    }

    public boolean isVisible() {
        return visible.get();
    }

    public boolean wasTaught() {
        return taught.get();
    }

    public int getTeacherID() {
        return teacher;
    }

    public long getWhenLearned() {
        return learned;
    }

    public long getLastAdvancedTime() {
        return lastAdvanced;
    }

    protected void performSQLUpdate() {
        buffer.put(new MagicUpdateTask(id, tier, learned, lastAdvanced, teacher, visible.get()));
    }
}

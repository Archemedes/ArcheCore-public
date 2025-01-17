package net.lordofthecraft.arche.save.rows;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

public abstract class SingleStatementRow extends MultiStatementRow {

    @Override
    public String toString() { //Works in most cases else override
        String sql = getStatement();
        String[] bits = sql.split("\\?");
        int extra = sql.endsWith("?")? 1 : 0;
       
        for (int i = 0; i < bits.length; i++) {
            String[] its = bits[i].split(" ");
            bits[i] = its[its.length - 1];
        }

        for (int i = 1; i < bits.length + extra; i++) {
            bits[i - 1] = bits[i - 1] + getValueFor(i).toString().replace('\n', '/');
        }

        return this.getClass().getSimpleName() + '{' + StringUtils.join(bits, ',') + '}';
    }

    @Override
    public final String[] getStatements() {
        return new String[]{getStatement()};
    }

    protected abstract String getStatement();

    @Override
    public final Object getValueFor(int stat, int index) {
        Validate.isTrue(stat == 1);
        return getValueFor(index);
    }

    protected abstract Object getValueFor(int index);
}

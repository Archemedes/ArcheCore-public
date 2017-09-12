package net.lordofthecraft.arche.save.archerows;

public interface ArcheMergeableRow extends ArchePreparedStatementRow {

    boolean isUnique();

    boolean canMerge(ArcheMergeableRow row);

    ArcheMergeableRow merge(ArcheMergeableRow second);
}

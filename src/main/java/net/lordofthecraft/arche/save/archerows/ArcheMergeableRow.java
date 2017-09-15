package net.lordofthecraft.arche.save.archerows;

import java.sql.PreparedStatement;

/**
 * Be warned that this is a high level concept. This will be something that is complex and if you do not understand you should simply use {@link net.lordofthecraft.arche.save.archerows.ArchePreparedStatementRow}
 * <p>
 * An ArcheMergeableRow is an SQL executing object which can be effectively merged with other ArcheMergeableRows to be run collectively as a batch ( {@link PreparedStatement#addBatch()} & {@link PreparedStatement#executeBatch()} )
 * <p>
 * In a loose example you can see {@link net.lordofthecraft.arche.save.archerows.persona.update.MultiPersonaUpdateRow} and {@link net.lordofthecraft.arche.save.archerows.persona.update.PersonaUpdateRow}
 * <p>
 * ArcheMergeableRows usually have {@link java.sql.PreparedStatement}s which are the same, and can be used for multiple entries.
 * For example, say I am having the {@link net.lordofthecraft.arche.persona.ArchePersona} named George updated so he has 20 health.
 * Then, I have the {@link net.lordofthecraft.arche.persona.ArchePersona} named Bob have his health set to 4.
 * <p>
 * For both, the {@link java.sql.PreparedStatement} might look something like "UPDATE persona SET health=? WHERE persona_id=?"
 * In this instance, I can have both "merge" together to be run in the same ArcheMergeableStatement as a batch.
 * First, we use this {@link PreparedStatement} to set George's health to 20, then we set Bob's health to 4, adding each to our batch with {@link PreparedStatement#addBatch()}
 * (write the code the same but instead of {@link PreparedStatement#executeUpdate()} you use addBatch)
 * once both are done we then use {@link PreparedStatement#executeBatch()} which will execute both statements at the same time.
 * <p>
 * Suggestion: Use a try {} catch ({@link java.sql.SQLException} ex) {} finally {} for running multiple batch statements but throw your exceptions back out into the consumer with throw ex;
 * If you do this you can then see <i>which</i> statement in the batch throws an error if it happens, otherwise if one {@link PreparedStatement} errors you'll be clueless as to which one and most likely why it did.
 *
 * @author 501warhead
 */
public interface ArcheMergeableRow extends ArchePreparedStatementRow {

    /**
     * Checks whether or not this row is "Unique" and therefore cannot be merged with another row regardless of state.
     * @return Whether or not this is unique
     */
    boolean isUnique();

    /**
     * Check whether or not this row can be merged with another.
     * @param row The other row to check if it can merge
     * @return Whether or not this is a valid merge.
     */
    boolean canMerge(ArcheMergeableRow row);

    /**
     * Merge the rows together if {@link #canMerge(ArcheMergeableRow)} succeeds.
     *
     * @param second The row to merge
     * @return The combined rows
     */
    ArcheMergeableRow merge(ArcheMergeableRow second);
}

package net.lordofthecraft.arche.magic;

import com.google.common.collect.Sets;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.interfaces.Creature;
import net.lordofthecraft.arche.interfaces.Magic;
import net.lordofthecraft.arche.interfaces.MagicType;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created on 7/12/2017
 *
 * @author 501warhead
 */
public class Archenomicon {

    /*
                               ,   ,
                              /////|
                             ///// |
                            |~~~|  |
                            |===|  |
                            |A  |  |
                            | N |  |
                            |  C| /
                            |===|/
                            '---'
     */

    private static Archenomicon ourInstance = new Archenomicon();

    public static Archenomicon getInstance() {
        return ourInstance;
    }

    private Archenomicon() {
    }

    /*
                                 *
                           *   *
                         *    \* / *
                           * --.:. *
                          *   * :\ -
                            .*  | \
                           * *     \
                         .  *       \
                          ..        /\.
                         *          |\)|
                       .   *         \ |
                      . . *           |/\
                         .* *         /  \
                       *              \ / \
                     *  .  *           \   \
                        * .             POWERFUL SPELLS
                       *    *
                      .   *    *
     */

    public Optional<Magic> getMagic(String id) {
        return magics.stream().filter(m -> m.getName().equalsIgnoreCase(id)).findFirst();
    }

    /*
                    (\
                    \'\
                     \'\     __________
                     / '|   ()_________)
                     \ '/    \~MEMBERS~ \
                       \       \ ~~~~~~   \
                       ==).      \__________\
                      (__)       ()__________)
     */

    private final Set<Magic> magics = Sets.newConcurrentHashSet();
    private final Set<MagicType> archetypes = Sets.newConcurrentHashSet();
    private final Set<Creature> creatures = Sets.newConcurrentHashSet();

    public void init(SQLHandler handler) {
        if (!archetypes.isEmpty()) {
            return;
        }
        try {
            Connection conn = handler.getConnection();
            PreparedStatement stat = conn.prepareStatement("SELECT id_key,name,parent_type,descr FROM magic_archetypes");
            PreparedStatement magicselect = conn.prepareStatement("SELECT id_key,max_tier,extra_tier,self_teach,teachable,description,label,days_to_max,days_to_extra FROM magics WHERE archetype=?");
            PreparedStatement weakselect = conn.prepareStatement("SELECT fk_weakness_magic,modifier FROM magic_weaknesses WHERE fk_source_magic=?");
            ResultSet rs = stat.executeQuery();
            while (rs.next()) {
                String id = rs.getString("id_key");
                String name = rs.getString("name");
                String parent = rs.getString("parent_type");
                String description = rs.getString("descr");
                MagicType aparent = null;
                if (parent != null) {
                    Optional<MagicType> pt = getArchetypeById(parent);
                    if (pt.isPresent()) {
                        aparent = pt.get();
                    }
                }
                ArcheType t = new ArcheType(id, name, (aparent == null ? null : (ArcheType) aparent), description);
                registerArchetype(t);
                magicselect.clearParameters();
                magicselect.setString(1, id);
                ResultSet magics = magicselect.executeQuery();
                while (magics.next()) {
                    String mid = rs.getString("id_key");
                    int max_tier = rs.getInt("max_tier");
                    boolean extra_tier = rs.getBoolean("extra_tier");
                    boolean self_teach = rs.getBoolean("self_teach");
                    boolean teachable = rs.getBoolean("teachable");
                    String mdesc = rs.getString("description");
                    String label = rs.getString("label");
                    int days_to_max = rs.getInt("days_to_max");
                    int days_to_extra = rs.getInt("days_to_extra");
                    ArcheMagic m = new ArcheMagic(mid, max_tier, self_teach, label, description, teachable, days_to_max, days_to_extra, t);
                    registerMagic(m);
                }
                magics.close();
            }
            rs.close();
            magicselect.close();
            stat.close();

            for (Magic m : magics) {
                weakselect.clearParameters();
                weakselect.setString(1, m.getName());
                rs = weakselect.executeQuery();
                while (rs.next()) {
                    String mid = rs.getString("fk_weakness_magic");
                    int modifier = rs.getInt("modifier");
                    Optional<Magic> weak = getMagic(mid);
                    weak.ifPresent(magic -> ((ArcheMagic) m).addWeakness(magic, modifier));
                }
                rs.close();
            }
            weakselect.close();

            PreparedStatement creatureselect = conn.prepareStatement("SELECT id_key,name,descr FROM magic_creatures");
            PreparedStatement creaturecreators = conn.prepareStatement("SELECT magic_id_fk FROM creature_creators WHERE creature_fk=?");
            PreparedStatement creatureabilities = conn.prepareStatement("SELECT ability FROM creature_abilities WHERE creature_fk=?");

            rs = creatureselect.executeQuery();
            while (rs.next()) {
                String cid = rs.getString("id_key");
                String name = rs.getString("name");
                String description = rs.getString("descr");
                ArcheCreature creature = new ArcheCreature(cid, name, description);
                registerCreature(creature);
                creaturecreators.clearParameters();
                creaturecreators.setString(1, cid);
                ResultSet creators = creaturecreators.executeQuery();
                while (creators.next()) {
                    String smagic = creators.getString("magic_id_fk");
                    Optional<Magic> omagic = getMagic(smagic);
                    omagic.ifPresent(creature::addCreator);
                }
                creators.close();
                creatureabilities.clearParameters();
                creatureabilities.setString(1, cid);
                ResultSet abilities = creatureabilities.executeQuery();
                while (abilities.next()) {
                    creature.addAbility(abilities.getString("ability"));
                }
                abilities.close();
            }
            rs.close();
            creatureselect.close();
            creaturecreators.close();
            creatureabilities.close();

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
                     ,..........   ..........,
                 ,..,'          '.'          ',..,
                ,' ,'            :  REGISTRAR ', ',
               ,' ,'             :             ', ',
              ,' ,'              :              ', ',
             ,' ,'............., : ,.............', ',
            ,'  '............   '.'   ............'  ',
             '''''''''''''''''';''';''''''''''''''''''
                                '''
     */


    public void registerMagic(ArcheMagic m) {
        if (!magics.contains(m)) {
            magics.add(m);
        }
    }

    public void registerArchetype(ArcheType type) {
        if (!archetypes.contains(type)) {
            archetypes.add(type);
        }
    }

    public void registerCreature(ArcheCreature creature) {
        if (!creatures.contains(creature)) {
            creatures.add(creature);
        }
    }

    /*
             ,..........   ..........,
         ,..,'          '.'          ',..,
        ,' ,'            :            ', ',
       ,' ,'             :             ', ',
      ,' ,'              :              ', ',
     ,' ,'............., : ,.............', ',
    ,'  '............   '.'   ............'  ',
     '''''''''''''''''';''';''''''''''''''''''
                        '''
     */

    public Optional<Creature> getCreatureById(String id) {
        return creatures.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public Optional<Creature> getCreatureByName(String name) {
        return creatures.stream().filter(c -> c.getName().equals(name)).findFirst();
    }

    public Optional<Magic> getMagicById(String id) {
        return magics.stream().filter(m -> m.getName().equals(id)).findFirst();
    }

    public Optional<Magic> getMagicByName(String name) {
        return magics.stream().filter(m -> m.getLabel().equals(name)).findFirst();
    }

    public Optional<MagicType> getArchetypeById(String id) {
        return archetypes.stream().filter(a -> a.getKey().equals(id)).findFirst();
    }

    public Optional<MagicType> getArchetypeByName(String name) {
        return archetypes.stream().filter(a -> a.getName().equals(name)).findFirst();
    }

    public Set<Magic> listMagicsByArchetype(MagicType type) {
        return magics.parallelStream().filter(m -> m.getType().equals(type)).collect(Collectors.toSet());
    }

    public Set<Magic> getMagics() {
        return Collections.unmodifiableSet(magics);
    }

    public Set<MagicType> getArchetypes() {
        return Collections.unmodifiableSet(archetypes);
    }

    public Set<Creature> getCreatures() {
        return Collections.unmodifiableSet(creatures);
    }

    public void removeMagic(Magic m) {
        ArchePersonaHandler.getInstance().removeMagic(m);
        magics.remove(m);
    }


}

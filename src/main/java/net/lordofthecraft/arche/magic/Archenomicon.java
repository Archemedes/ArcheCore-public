package net.lordofthecraft.arche.magic;

import com.google.common.collect.Sets;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.interfaces.Magic;
import net.lordofthecraft.arche.interfaces.MagicFactory;

import java.util.Optional;
import java.util.Set;

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

    /*
     * loading from list
     * input method
     * get method
     *
         * magic:
         * * name
         * * label
         * * description
         * * abilities
         * * tier.max
         * * tier.extra
         * * mastery.max
         * * mastery.extra
         * * self_teachable
         * * teachable
         * * archetype
         * * weak_to
         *
         * archetype:
         * * parent
         * * description
         *
         * creatures:
         * * name
         * * creators
         * * description
         * * abilities
         * * magic
         * * buff
         *
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

    public MagicFactory conjourMagicForge(String id) {
        id = id.toUpperCase();

        if (researchMagic(id).isPresent()) {

            return null;
        }

        return new ArcheMagicFactory(id);
    }

    public Optional<Magic> researchMagic(String id) {
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
    private final Set<ArcheType> archetypes = Sets.newConcurrentHashSet();
    private final Set<ArcheCreature> creatures = Sets.newConcurrentHashSet();

    public void createTomeFromKnowledge(SQLHandler handler) {
        //TODO init methods
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



    public void registerArcana(ArcheMagic m) {
        if (!magics.contains(m)) {
            magics.add(m);
        }
    }

    public void forgeArchetype(ArcheType type) {
        if (!archetypes.contains(type)) {
            archetypes.add(type);
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

    public Optional<ArcheCreature> summonCreature(String id) {
        return creatures.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public Optional<ArcheCreature> summonCreatureByName(String name) {
        return creatures.stream().filter(c -> c.getName().equals(name)).findFirst();
    }

    public Optional<Magic> invokeMagic(String id) {
        return magics.stream().filter(m -> m.getName().equals(id)).findFirst();
    }

    public Optional<Magic> invokeMagicByName(String name) {
        return magics.stream().filter(m -> m.getLabel().equals(name)).findFirst();
    }

    public Optional<ArcheType> studyMagicType(String id) {
        return archetypes.stream().filter(a -> a.getKey().equals(id)).findFirst();
    }

    public Optional<ArcheType> studyMagicTypeByName(String name) {
        return archetypes.stream().filter(a -> a.getName().equals(name)).findFirst();
    }



}

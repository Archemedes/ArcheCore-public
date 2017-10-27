package net.lordofthecraft.arche.persona;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.attributes.AttributeRegistry;
import net.lordofthecraft.arche.enums.AbilityScore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import net.lordofthecraft.arche.util.ItemUtil;
import net.lordofthecraft.arche.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.conversations.BooleanPrompt;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

public class AttributeSelectMenu implements InventoryHolder {

    public static final String TITLE = ChatColor.BLUE + ChatColor.BOLD.toString() + "Spend Ability Score Points";
    private static final ImmutableMap<AbilityScore, Integer[]> abilityButtons;
    private static final int confirmButton = 26, helpButton = 17, pointsIcon = 8;

    static {
        ImmutableMap.Builder<AbilityScore, Integer[]> builder = ImmutableMap.builder();
        builder
                .put(AbilityScore.CONSTITUTION, new Integer[]{0, 9, 18})
                .put(AbilityScore.STRENGTH, new Integer[]{1, 10, 19})
                .put(AbilityScore.DEXTERITY, new Integer[]{2, 11, 20})
                .put(AbilityScore.INTELLECT, new Integer[]{3, 12, 21})
                .put(AbilityScore.WISDOM, new Integer[]{4, 13, 22})
                .put(AbilityScore.LUCK, new Integer[]{5, 14, 23});
        abilityButtons = builder.build();
    }

    public static AttributeSelectMenu open(Persona pers) {
        return new AttributeSelectMenu(pers).show();
    }

    private final Inventory inv;
    private final Persona persona;
    private Map<AbilityScore, Integer> scores = Maps.newHashMap();
    private int unspentPoints;
    private final ConversationFactory factory;
    private final boolean canModify;

    public AttributeSelectMenu(Persona forWhom) {
        this.persona = forWhom;
        this.unspentPoints = persona.getUnspentPoints();
        canModify = unspentPoints > 0;
        this.inv = Bukkit.createInventory(this, (canModify ? 9 * 3 : 9), TITLE);

        factory = new ConversationFactory(ArcheCore.getPlugin())
                .thatExcludesNonPlayersWithMessage("NO! FU Tythus how are you even seeing this?")
                .withPrefix(new CreationDialog.Prefix())
                .withModality(true);
        for (AbilityScore score : AbilityScore.values()) {
            if (score.isChangeable()) {
                scores.put(score, getScore(score, PersonaHandler.SCORE_ID));
            }
        }
        buildInventory();
    }

    private int getScore(AbilityScore score, UUID target) {
        //501 - mother of god
        return persona.attributes()
                .getInstance(AttributeRegistry.getSAttribute(score.getName()))
                .getModifiers()
                .stream()
                .filter(m -> m.getUniqueId().equals(target))
                .map(AttributeModifier::getAmount)
                .map(Double::new)
                .map(Double::intValue)
                .findFirst()
                .orElse(0);
    }

    public AttributeSelectMenu show() {
        if (persona.getPlayer() != null) {
            persona.getPlayer().openInventory(inv);
        }
        return this;
    }

    void buildInventory() {
        if (canModify) {
            inv.setItem(confirmButton, ItemUtil.make(Material.CONCRETE,
                    (short) 13,
                    ChatColor.GREEN + "Confirm",
                    ChatColor.GRAY + "Press to confirm",
                    ChatColor.GRAY + "that these are your persona's scores"));
            inv.setItem(pointsIcon, ItemUtil.make(Material.NETHER_STAR,
                    (short) 0,
                    unspentPoints,
                    ChatColor.DARK_PURPLE + "Unspent Points: " + unspentPoints,
                    ChatColor.GRAY + "The amount of points you have unspent"));
        }
        inv.setItem((canModify ? helpButton : helpButton - 9), ItemUtil.make(Material.COMMAND,
                ChatColor.YELLOW + "Help",
                ChatColor.GRAY + "Click to learn more about ability scores"));
        for (AbilityScore score : scores.keySet()) {
            updateScoreSlot(score);
        }
    }

    public void updateScoreSlot(AbilityScore score) {
        int currentScore = scores.get(score);
        int noRacialScore = getScore(score, PersonaHandler.SCORE_ID);
        int racialScore = getScore(score, RaceBonusHandler.UUID_RACIAL_SCORE);
        int capScore = (int) persona.attributes().getAttributeValue(AttributeRegistry.getInstance().getAttribute("cap_" + score.getName())) + getScore(score, RaceBonusHandler.UUID_RACIAL_SCORE);
        Integer[] slots = abilityButtons.get(score);
        if (currentScore < capScore && unspentPoints > 0 && canModify) {
            inv.setItem(slots[0], ItemUtil.make(Material.CONCRETE,
                    (short) 5,
                    ChatColor.GREEN + "Add point",
                    ChatColor.GRAY + "Add 1 point to " + score.getName(),
                    ChatColor.GRAY + "which will be added back to the pool"));
        } else if (unspentPoints <= 0 && canModify) {
            inv.setItem(slots[0], ItemUtil.make(Material.BARRIER,
                    ChatColor.DARK_RED + "Cannot add",
                    ChatColor.GRAY + "You have used all your",
                    ChatColor.GRAY + "unused points."));
        } else if (canModify) {
            inv.setItem(slots[0], ItemUtil.make(Material.BARRIER,
                    ChatColor.DARK_RED + "Cannot add",
                    ChatColor.GRAY + "You cannot go above",
                    ChatColor.GRAY.toString() + capScore + " points in " + score.getName(),
                    ChatColor.GRAY + "However there are hidden ways to increase your score cap"));
        }
        inv.setItem((canModify ? slots[1] : slots[1] - 9), ItemUtil.make(score.getItemIcon(),
                (short) 0,
                currentScore + racialScore,
                score.getStringID() + ": " + ChatColor.RESET + (currentScore + racialScore),
                ChatColor.GRAY + score.getDesc()));
        if (currentScore > 1 && currentScore > noRacialScore && canModify) {
            inv.setItem(slots[2], ItemUtil.make(Material.CONCRETE,
                    (short) 14,
                    ChatColor.RED + "Subtract point",
                    ChatColor.GRAY + "Remove 1 point from " + score.getName(),
                    ChatColor.GRAY + "which will be added back to the pool"));
        } else if (canModify && currentScore <= noRacialScore) {
            inv.setItem(slots[2], ItemUtil.make(Material.BARRIER,
                    ChatColor.DARK_RED + "Cannot Subtract",
                    ChatColor.GRAY + "You cannot go",
                    ChatColor.GRAY + "below " + noRacialScore + " point" + (noRacialScore == 1 ? "" : "s") + " in " + score.getName()));
        } else if (canModify) {
            inv.setItem(slots[2], ItemUtil.make(Material.BARRIER,
                    ChatColor.DARK_RED + "Cannot Subtract",
                    ChatColor.GRAY + "You cannot go",
                    ChatColor.GRAY + "below 1 point in " + score.getName()));
        }
    }

    private void updateAll() {
        if (unspentPoints == 0) {
            inv.getItem(pointsIcon).setType(Material.BARRIER);
            inv.getItem(pointsIcon).setAmount(1);
        } else {
            inv.getItem(pointsIcon).setType(Material.NETHER_STAR);
            inv.getItem(pointsIcon).setAmount(unspentPoints);
        }
        scores.keySet().forEach(this::updateScoreSlot);
    }

    public void click(int clicked) {
        if (ArcheCore.isDebugging()) {
            ArcheCore.getPlugin().getLogger().info("Running click method for " + MessageUtil.identifyPersona(persona) + " on slot #" + clicked);
        }
        if (clicked > 26) {
            return;
        }
        if (inv.getItem(clicked).getType().equals(Material.COMMAND)) {
            persona.getPlayer().closeInventory();
            Bukkit.dispatchCommand(persona.getPlayer(), "archehelp points");
        } else if (clicked == confirmButton) {
            persona.getPlayer().closeInventory();
            factory.withFirstPrompt(new ConfirmPrompt(this))
                    .buildConversation(persona.getPlayer())
                    .begin();
        } else {
            if (inv.getItem(clicked).getType() != Material.CONCRETE) {
                return;
            }
            for (Map.Entry<AbilityScore, Integer[]> ent : abilityButtons.entrySet()) {
                Integer[] val = ent.getValue();
                AbilityScore score = ent.getKey();
                if (val[0] == clicked && unspentPoints > 0) {
                    scores.replace(score, scores.get(score) + 1);
                    --unspentPoints;
                    updateAll();
                    break;
                } else if (val[2] == clicked && scores.get(score) > 1 + getScore(score, RaceBonusHandler.UUID_RACIAL_SCORE)) {
                    scores.replace(score, scores.get(score) - 1);
                    ++unspentPoints;
                    updateAll();
                    break;
                }
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    public Map<AbilityScore, Integer> getScores() {
        return scores;
    }

    public static class ConfirmPrompt extends BooleanPrompt {

        private final AttributeSelectMenu menu;

        public ConfirmPrompt(AttributeSelectMenu menu) {
            this.menu = menu;
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return "Type yes or no to confirm or deny";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext conversationContext, boolean b) {
            if (b) {
                menu.scores.forEach((score, i) -> {
                    if (menu.getScore(score, PersonaHandler.SCORE_ID) != i) {
                        menu.persona.attributes().addModifier(AttributeRegistry.getSAttribute(score.getName()), new AttributeModifier(PersonaHandler.SCORE_ID, score.getName(), i, AttributeModifier.Operation.ADD_NUMBER), true, true);
                    }
                });
                menu.persona.setUnspentPoints(menu.unspentPoints);
                conversationContext.getForWhom().sendRawMessage(ChatColor.GREEN + "Success! Your points have been successfully spent.");
            } else {
                menu.show();
                conversationContext.getForWhom().sendRawMessage(ChatColor.RED + "Returning to the menu...");
            }
            return Prompt.END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            context.getForWhom().sendRawMessage(ChatColor.YELLOW + "Your stats:");
            menu.scores.entrySet().parallelStream()
                    .sorted(Comparator.comparingInt(o -> o.getKey().ordinal()))
                    .forEach((score) -> context.getForWhom().sendRawMessage(score.getKey().getStringID() + ": " + ChatColor.RESET + score.getValue()));
            context.getForWhom().sendRawMessage(ChatColor.YELLOW + "Is this correct? Yes/No\n" + ChatColor.RED.toString() + ChatColor.BOLD + "Warning! You cannot change once set!");

            return CreationDialog.DIVIDER;
        }
    }
}

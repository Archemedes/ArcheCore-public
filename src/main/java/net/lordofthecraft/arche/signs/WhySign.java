package net.lordofthecraft.arche.signs;

import com.google.common.collect.Maps;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.ChatBoxAction;
import net.lordofthecraft.arche.help.ArcheMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Level;

/**
 * Created by Sean on 5/29/2016.
 */
public class WhySign {
    final private static String PATH = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
    final private static String C_PATH = "org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
    private static final boolean nmsSetUp = true;
    private static Class<?> craftSign;
    private static Class<?> tileEntitySign;
    private static Class<?> nbtTagCompound;
    private static Constructor<?> nbtTagCompoundConstructor;
    private static Method nbtTagCompoundSetString;
    private static Method VOLATILE_SIGN_SET_METHOD;
    //private static Method craftSignGetTileEntity;

    static {
        try {
            /*craftWorld = Class.forName(C_PATH + "CraftWorld");
            worldServer = Class.forName(PATH + "WorldServer");
            nmsBlockPosition = Class.forName(PATH + "BlockPosition");
            tileEntity = Class.forName(PATH + "TileEntity");*/
            craftSign = Class.forName(C_PATH + "block.CraftSign");
            tileEntitySign = Class.forName(PATH + "TileEntitySign");
            nbtTagCompound = Class.forName(PATH + "NBTTagCompound");

            //nmsBlockPositionConstructor = nmsBlockPosition.getConstructor(int.class, int.class, int.class);
            nbtTagCompoundConstructor = nbtTagCompound.getConstructor();

            //craftWorldGetHandle = craftWorld.getMethod("getHandle", worldServer);
            //worldServerGetTileEntity = worldServer.getMethod("getTileEntity", tileEntity, nmsBlockPosition); //We're going to need to cast this.

            //craftSignGetTileEntity = craftSign.getDeclaredMethod("getTileEntity", tileEntitySign);
            nbtTagCompoundSetString = nbtTagCompound.getMethod("setString", String.class, String.class); //really all we need

            //WARNING: this method is VOLATILE! This will CHANGE ON REVISIONS!
            VOLATILE_SIGN_SET_METHOD = tileEntitySign.getMethod("a", /*String.class*,*/ nbtTagCompound);
            //WARNING: this method is VOLATILE! This will CHANGE ON REVISIONS!

        } catch (ClassNotFoundException | NoSuchMethodException e) {
            ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "Failed to establish reflection for WhySign!", e);
        }
    }

    private Map<Class<?>, Object> castMap = Maps.newHashMap(); //I really hate this tbh
    private BaseComponent[] components = new BaseComponent[4];
    private int lastline = 0;

    private WhySign() {
        if (!nmsSetUp) {
            throw new IllegalStateException("Failed to establish reflection methods, WhySign cannot be instantiated.");
        }
    }

    public static WhySign newInstance() {
        return new WhySign();
    }

    public WhySign setLineText(int line, String text) {
        if (line > 3) {
            throw new IndexOutOfBoundsException("Line cannot be greater than 3");
        }
        if (text.length() > 14) {
            text = text.substring(0, 14);
        }
        BaseComponent[] comp = TextComponent.fromLegacyText(text);
        if (comp.length == 1) {
            components[line] = comp[0];
        } else {
            BaseComponent ncomp = new TextComponent("");
            for (BaseComponent c : comp) {
                ncomp.addExtra(c);
            }
            components[line] = ncomp;
        }
        updateLastLine();
        return this;
    }

    public WhySign setLineBold(int line, boolean set) {
        if (line > 3) {
            throw new IndexOutOfBoundsException("Line cannot be greater than 3");
        }
        if (components[line] == null) {
            components[line] = new TextComponent("");
        }
        components[line].setBold(set);
        updateLastLine();
        return this;
    }

    public WhySign setLineItalic(int line, boolean it) {
        if (line > 3) {
            throw new IndexOutOfBoundsException("Line cannot be greater than 3");
        }
        if (components[line] == null) {
            components[line] = new TextComponent("");
        }
        components[line].setItalic(it);
        updateLastLine();
        return this;
    }

    public WhySign setLineStrike(int line, boolean st) {
        if (line > 3) {
            throw new IndexOutOfBoundsException("Line cannot be greater than 3");
        }
        if (components[line] == null) {
            components[line] = new TextComponent("");
        }
        components[line].setStrikethrough(st);
        updateLastLine();
        return this;
    }

    public WhySign setLineObfuscated(int line, boolean ob) {
        if (line > 3) {
            throw new IndexOutOfBoundsException("Line cannot be greater than 3");
        }
        if (components[line] == null) {
            components[line] = new TextComponent("");
        }
        components[line].setObfuscated(ob);
        updateLastLine();
        return this;
    }

    public WhySign setLineUnderlined(int line, boolean un) {
        if (line > 3) {
            throw new IndexOutOfBoundsException("Line cannot be greater than 3");
        }
        if (components[line] == null) {
            components[line] = new TextComponent("");
        }
        components[line].setUnderlined(un);
        updateLastLine();
        return this;
    }

    private void updateLastLine() {
        for (int i = 0; i < 4; i++) {
            if (components[i] != null) {
                lastline = i;
            }
        }
    }

    public WhySign applyChatColor(int line, org.bukkit.ChatColor color) {
        if (line > 3) {
            throw new IndexOutOfBoundsException("Line cannot be greater than 3");
        }
        if (components[line] == null) {
            components[line] = new TextComponent("");
        }
        if (color.isColor()) {
            ChatColor convertedColor = ArcheMessage.convertChatColor(color);
            components[line].setColor(convertedColor);
        } else {
            switch (color) {
                default:
                    break;
                case ITALIC:
                    setLineItalic(line, true);
                    break;
                case BOLD:
                    setLineBold(line, true);
                    break;
                case MAGIC:
                    setLineObfuscated(line, true);
                    break;
                case UNDERLINE:
                    setLineUnderlined(line, true);
                    break;
                case STRIKETHROUGH:
                    setLineStrike(line, true);
                    break;
            }
        }
        updateLastLine();
        return this;
    }

    public WhySign setClickEvent(ChatBoxAction action, String value, int line) {
        if (line > 3) {
            throw new IndexOutOfBoundsException("Line cannot be greater than 3");
        }
        if (components[line] == null) {
            components[line] = new TextComponent("");
        }
        ClickEvent.Action act;
        switch (action) {

            case RUN_COMMAND:
                act = ClickEvent.Action.RUN_COMMAND;
                break;
            case SUGGEST_COMMAND:
                act = ClickEvent.Action.SUGGEST_COMMAND;
                break;
            case OPEN_URL:
                act = ClickEvent.Action.OPEN_URL;
                break;
            case OPEN_FILE:
                act = ClickEvent.Action.OPEN_FILE;
                break;
            default:
                throw new IllegalArgumentException("Not all actions supported for ClickEvent");
        }

        ClickEvent event = new ClickEvent(act, value);
        components[line].setClickEvent(event);
        updateLastLine();
        return this;
    }

    public BaseComponent getLine(int line) throws IndexOutOfBoundsException {
        return components[line];
    }

    public boolean isBold(int line) {
        return line <= 3 && components[line] != null && components[line].isBold();
    }

    public boolean isItalic(int line) {
        return line <= 3 && components[line] != null && components[line].isItalic();
    }

    public boolean isStrikethrough(int line) {
        return line <= 3 && components[line] != null && components[line].isStrikethrough();
    }

    public void build(Block b) {
        if (b.getType() != Material.SIGN_POST && b.getType() != Material.WALL_SIGN) {
            throw new IllegalArgumentException("Block is not a sign!");
        }
        buildNBTSign(b);
        //runCommand(b, pl);
    }

    private String toRawJson(BaseComponent comp) {
        String formatted = "{\\\"text\\\":\\\"" + comp.toPlainText() + "\\\"";
        if (comp.isBold()) {
            formatted += ",\\\"bold\\\":true";
        }
        if (comp.isItalic()) {
            formatted += ",\\\"italic\\\":true";
        }
        if (comp.isStrikethrough()) {
            formatted += ",\\\"strikethrough\\\":true";
        }
        if (comp.isObfuscated()) {
            formatted += ",\\\"obfuscated\\\":true";
        }
        if (comp.getColor() != null && comp.getColor() != ChatColor.WHITE) {
            formatted += ",\\\"color\\\":\\\"" + comp.getColor().name().toLowerCase() + "\\\"";
        }
        if (comp.getClickEvent() != null) {
            formatted += ",\\\"clickEvent\\\":{\\\"action\\\":";
            formatted += "\\\"" + comp.getClickEvent().getAction().name().toLowerCase() + "\\\"";
            formatted += ",\\\"value\\\":\\\"" + comp.getClickEvent().getValue() + "\\\"}";
        }
        formatted += "}";
        return formatted;
    }

    private void buildNBTSign(Block b) {
        //Wow this looks absolutely disgusting.
        try {
            Sign s = (Sign) b.getState();
            castMap.put(craftSign, s);
            Object craftsign = craftSign.cast(castMap.get(craftSign));
            castMap.remove(craftSign);
            Field f = craftSign.getDeclaredField("sign");
            f.setAccessible(true);
            Object tEntitySign = f.get(craftsign);
            f.setAccessible(false);
            Object nbtCompoundList = nbtTagCompoundConstructor.newInstance();
            for (int i = 0; i < 4; i++) {
                if (components[i] != null) {
                    nbtTagCompoundSetString.invoke(nbtCompoundList, "Text" + (i + 1), toRawJson(components[i]));
                }
            }
            VOLATILE_SIGN_SET_METHOD.invoke(tEntitySign, nbtCompoundList);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

}

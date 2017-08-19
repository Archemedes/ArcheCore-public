package net.lordofthecraft.arche.persona;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PersonaIcon {

	private String data;

	public PersonaIcon(String data) {
		this.data = data;
	}

	public PersonaIcon(Player p) {
		String texture = this.retrieveTextureFromAPI(p);
		this.data = (texture == null) ? null : texture;
	}

	private String retrieveTextureFromAPI(Player p) {

		URL url;
		HttpURLConnection con;
		InputStreamReader in;

		String uuid = p.getUniqueId().toString().replaceAll("-", "");
		//Request player profile from mojang api
		try {
			url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestProperty("Content-type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setDoInput(true); 
			in = new InputStreamReader(con.getInputStream());
		} catch (IOException e) {
			p.sendMessage("You have requested to update your persona icon too many times in the past minute.");

			return null;
		}

		JsonParser parser = new JsonParser();
		JsonElement result = parser.parse(in);
		JsonObject jobject;

		try {
			jobject = result.getAsJsonObject();
		} catch (IllegalStateException e) {
			try {
				in.close();
			} catch (IOException ee) {
				ee.printStackTrace();
			}
			return null;
		}

		JsonArray jarray = jobject.getAsJsonArray("properties");
		jobject = jarray.get(0).getAsJsonObject();

		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return jobject.get("value").toString();
	}

	public String getData() {
		return data;
	}

	public ItemStack getHeadItem() {
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		if (data == null) return skull;
		ItemMeta skullMeta = skull.getItemMeta();

		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		profile.getProperties().put("textures", new Property("textures", data));

		Field profileField = null;

		try {
			profileField = skullMeta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(skullMeta, profile);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
		}
		skull.setItemMeta(skullMeta);
		return skull;
	}

}

package net.lordofthecraft.arche.skin;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.lang.Validate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

public class MojangCommunicator {

	@SuppressWarnings("unchecked")
	public static AuthenthicationData authenthicate(MinecraftAccount account) throws IOException, ParseException, AuthenticationException {
		//See wiki.vg/Authenthication		
		InputStream in = null;
		BufferedWriter out = null;
		try {
			URL url = new URL("https://authserver.mojang.com/authenticate");
			HttpURLConnection conn;
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true); //lets us write method body
			conn.setDoInput(true); //cuz fuck it why not

			conn.setRequestMethod("POST");
			conn.setRequestProperty( "Content-Type", "application/json" ); //required by Mojang

			JSONObject payload = new JSONObject();
			JSONObject agent = new JSONObject();
			agent.put("name", "Minecraft");
			agent.put("version", "1");
			payload.put("agent", agent);
			payload.put("username", account.username);
			payload.put("password", account.password);

			String payloadString = payload.toJSONString();
			System.out.print(payloadString);

			out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			out.write(payloadString);

			int responseCode = conn.getResponseCode();
			if(responseCode == 200) {
				in = new BufferedInputStream(conn.getInputStream());
				String result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
				JSONParser parser = new JSONParser();
				JSONObject responseJson = (JSONObject) parser.parse(result);

				AuthenthicationData data = new AuthenthicationData();
				data.accessToken = responseJson.get("accessToken").toString();
				data.uuid = ((JSONObject) responseJson.get("selectedProfile")).get("id").toString();
				return data;

			} else if(responseCode == 403){
				throw new AuthenticationException("Mojang returned 403 on authenthication."
						+ " Account: " + account.username + " may have had its password changed!");
			} else {
				throw new RuntimeException("Unexpected response code on authenthication: " + responseCode);
			}

		} finally {
			if(in != null) in.close();
			if(out != null) out.close();
		}
	}
	
	public static void setSkin(AuthenthicationData data, String skinUrl) throws IOException {
		//See wiki.vg/Mojang_API#Change_Skin
		InputStream in = null;
		DataOutputStream out = null;
		try {
			String urlString = "https://api.mojang.com/user/profile/"+ data.uuid +"/skin";
			URL url = new URL(urlString);
			HttpURLConnection conn;
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true); //lets us write method body
			conn.setDoInput(true); //cuz fuck it why not

			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", "Bearer " + data.accessToken); //shows we're logged in

			String query = "model="+URLEncoder.encode("slim","UTF-8"); 
			query += "&";
			query += "url="+URLEncoder.encode(skinUrl,"UTF-8") ;

			out = new DataOutputStream(conn.getOutputStream());  
			out.writeBytes(query);

			System.out.println(conn.getResponseCode());
			in = new BufferedInputStream(conn.getInputStream());
			String result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
			in.close();	
			System.out.println(result);
		} finally {
			if(in != null) in.close();
			if(out != null) out.close();
		}
	}
	
	public static PropertyMap requestSkin(String uuidUser) throws IOException, ParseException {
		//See Kowaman (if you see him ask him to read up on resource leaks)
		InputStreamReader in = null;

		try {//Request player profile from mojang api
			URL url;
			HttpURLConnection con;
			url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuidUser);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestProperty("Content-type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setDoInput(true);			
			in = new InputStreamReader(con.getInputStream());

			JSONParser parser = new JSONParser();
			JSONObject result = (JSONObject) parser.parse(in);
			JSONArray properties = (JSONArray) result.get("properties");
			JSONObject textures = (JSONObject) properties.get(0);
			
			String name = textures.get("name").toString();
			String value = textures.get("value").toString();
			String signature = textures.get("signature").toString();
			
			Validate.isTrue("textures".equals(name), "Skin properties file fetched from Mojang had wrong name: " + name);
			PropertyMap props = new PropertyMap();
			Property textureProperty = new Property("textures", value, signature);
			props.put("textures", textureProperty);
			return props;
		} finally { if(in != null) in.close(); }
	}

	
	public static class MinecraftAccount{ public String username,password;}
	
	public static class AuthenthicationData{ public String accessToken,uuid;}
}

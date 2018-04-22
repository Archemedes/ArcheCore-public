package net.lordofthecraft.arche.util;

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
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.CoreLog;

public class MojangCommunicator {
    public static class AuthenticationException extends Exception {
        private static final long serialVersionUID = -2260469046718388024L;

        public AuthenticationException(String msg) {
            super(msg);
        }
    }

    @SuppressWarnings("unchecked")
    public static AuthenthicationData authenthicate(MinecraftAccount account) throws IOException, ParseException, AuthenticationException {
        //See wiki.vg/Authenthication
        InputStream in = null;
		BufferedWriter out = null;
		HttpURLConnection conn = null;
		try {
			URL url = new URL("https://authserver.mojang.com/authenticate");
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

			out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			out.write(payloadString);
			out.close();
			
			int responseCode = conn.getResponseCode();
			if(responseCode == 200) {
				in = new BufferedInputStream(conn.getInputStream());
				String result = new String(ByteStreams.toByteArray(in), Charsets.UTF_8);
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
			if(conn != null) conn.disconnect();
		}
	}
	
	public static void setSkin(AuthenthicationData data, String skinUrl) throws IOException {
		//See wiki.vg/Mojang_API#Change_Skin
		InputStream in = null;
		DataOutputStream out = null;
		HttpURLConnection conn = null;
		try {
			String urlString = "https://api.mojang.com/user/profile/"+ data.uuid +"/skin";
			URL url = new URL(urlString);
			
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

			if(ArcheCore.getPlugin().debugMode()) {
				in = new BufferedInputStream(conn.getInputStream());
				String result = new String(ByteStreams.toByteArray(in), Charsets.UTF_8);
				in.close();	
				CoreLog.debug("[Debug] Mojang response to setting skin: ");
				CoreLog.debug(result);
			}
		} finally {
			if(in != null) in.close();
			if(out != null) out.close();
			if(conn != null) conn.disconnect();
		}
	}

    public static WrappedSignedProperty requestSkin(String uuidUser) throws IOException, ParseException {
        InputStreamReader in = null;
        HttpURLConnection con = null;

		try {//Request player profile from mojang api
			URL url;
			url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuidUser + "?unsigned=false");
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
            WrappedSignedProperty textureProperty = new WrappedSignedProperty("textures", value, signature);
            return textureProperty;
        } finally {
            if (in != null) in.close();
            if (con != null) con.disconnect();
        }
    }
    
    public static String requestCurrentUsername(UUID uuid) throws IOException {
    	String uuid_string = uuid.toString().replaceAll("-", "");
    	
        InputStreamReader in = null;
        HttpURLConnection con = null;

		try {//Request player profile from mojang api
			URL url;
			url = new URL("https://api.mojang.com/user/profiles/"+uuid_string+"/names");
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setDoInput(true);
			in = new InputStreamReader(con.getInputStream());

			JSONParser parser = new JSONParser();
			JSONArray result = (JSONArray) parser.parse(in);
			JSONObject firstName = (JSONObject) result.get(result.size() - 1);
			return String.valueOf(firstName.get("name"));
        }catch(ParseException e) {
        	throw new RuntimeException();
        } finally {
            if (in != null) in.close();
            if (con != null) con.disconnect();
        }
    }
    
    public static UUID requestPlayerUUID(String name) throws IOException {
    	return requestPlayerUUIDs(new String[]{name} )[0];
    }

    public static UUID[] requestPlayerUUIDs(String... names) throws IOException {
    	if(names.length > 100) throw new ArrayIndexOutOfBoundsException("Can only request 100 uuids at a time");
    	if(names.length > Arrays.stream(names)
    			.filter(Objects::nonNull)
    			.map(String::toLowerCase)
    			.distinct()
    			.collect(Collectors.toList()).size())
    		throw new IllegalArgumentException("Needs an array of unique, non-null names");
    	
        InputStreamReader in = null;
		BufferedWriter out = null;
        HttpURLConnection con = null;

        UUID[] uuids = new UUID[names.length];
        
		try {//Request player profile from mojang api
			URL url = new URL("https://api.mojang.com/profiles/minecraft");
			con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true); 
			con.setDoInput(true);

			con.setRequestMethod("POST");
			con.setRequestProperty( "Content-Type", "application/json" );

			String payload = JSONArray.toJSONString(Arrays.asList(names));

			out = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
			out.write(payload);
			out.close();

			in = new InputStreamReader(con.getInputStream());
			
			JSONParser parser = new JSONParser();
			JSONArray result = (JSONArray) parser.parse(in);
			int i = 0;
			for(Object x : result) {
				JSONObject o = (JSONObject) x;
				String name = o.get("name").toString();
				do {
					while(uuids[i] != null) i++;
					
					if(name.equalsIgnoreCase(names[i])) {
						String id = o.get("id").toString();
						String uid = id.substring(0, 8) + '-' + id.substring(8,12) + '-'
								+ id.substring(12,16) + '-' + id.substring(16, 20)
								+ '-' + id.substring(20);
						uuids[i++] = UUID.fromString(uid);
						break;
					}
				}while(++i < names.length);
				
			}
			return uuids;
        }catch(ParseException e) {
        	throw new RuntimeException();
    	}finally {
            if (in != null) in.close();
            if (out != null) out.close();
            if (con != null) con.disconnect();
        }
		
    }
    
	public static class MinecraftAccount{ public String username,password;}
	
	public static class AuthenthicationData{ public String accessToken,uuid;}
}

package net.lordofthecraft.arche.util;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonParser;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Hastebin {
	private final StringBuilder sb = new StringBuilder();
	
	public static String upload(String text) {
		Hastebin hb = new Hastebin();
		hb.sb.append(text);
		return hb.go();
	}
	
	public static Hastebin create() {
		return new Hastebin();
	}
	
	public Hastebin add(String line) {
		sb.append(line).append('\n');
		return this;
	}
	
	public String go() {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost("https://hastebin.com/documents");
		
		try {
			post.setEntity(new StringEntity(sb.toString()));

			HttpResponse response = client.execute(post);
			String result = EntityUtils.toString(response.getEntity());
			return "https://hastebin.com/" + new JsonParser().parse(result).getAsJsonObject().get("key").getAsString();
		} catch (IOException e) {
			e.printStackTrace();
			return "Hastebin Error!";
		}
		
	}
}

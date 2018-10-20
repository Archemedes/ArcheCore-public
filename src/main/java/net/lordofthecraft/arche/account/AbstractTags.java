package net.lordofthecraft.arche.account;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.google.common.base.Objects;
import com.google.gson.Gson;

import net.lordofthecraft.arche.interfaces.Tags;
import net.lordofthecraft.arche.persona.TagAttachment;

public abstract class AbstractTags<T> implements Tags<T> {
	protected static final String TAG_VALUE = "tag_value";
	protected static final String TAG_KEY = "tag_key";
	
	private static final Gson gson = new Gson();
	public static Gson getGson() { return gson; }

	protected final Map<String, TagAttachment> tags = new HashMap<>();
	protected boolean forOffline;
	protected boolean wasInit = false;

  protected abstract void commitTag(TagAttachment tag);
  protected abstract void deleteTag(String tagKey);
	
	
	public void init(ResultSet rs, boolean isForOffline) throws SQLException {
		Validate.isTrue(!wasInit, "Can only init an Tags instance once");

		forOffline = isForOffline;
		while (rs.next()) {
			String key = rs.getString(TAG_KEY);
			TagAttachment att = new TagAttachment(key, rs.getString(TAG_VALUE), forOffline);
			tags.put(key, att);
		}

		wasInit = true;
	}

	public void merge(AbstractTags<T> fromOffline) {
		Validate.isTrue(!forOffline, "Trying to merge INTO Tags that are for OFFLINE object");
		Validate.isTrue(fromOffline.forOffline, "Trying to merge FROM Tags that are for ONLINE object");

		fromOffline.getTags().forEach(t -> tags.put(t.getKey(), t));
	}
	
	
  @Override
  public String getValue(String key) {
      if (tags.containsKey(key)) return tags.get(key).getValue();
      else return null;
  }

  @Override
  public TagAttachment getTag(String key) {
      return tags.get(key);
  }

  @Override
  public void giveTag(String name, String value) {
      giveTag(name, value, false);
  }

  @Override
  public void giveTag(String name, String value, boolean offline) {
      giveTag(new TagAttachment(name, value, offline));
  }

  @Override
  public void giveTag(TagAttachment tag) {
      if (forOffline && !tag.isAvailableOffline())
          throw new IllegalArgumentException("Trying to add online-only tags to an Offline Persona");

      String k = tag.getKey();
      
      if(tags.containsKey(k)) {
      	TagAttachment othertag = tags.get(k);
      	if(tag.isAvailableOffline() == othertag.isAvailableOffline()
      			&& Objects.equal(othertag.getValue(), tag.getValue()))
      		return; //Tag already exists fully
      }
      
      tags.put(k, tag);
      commitTag(tag);
  }
  


  @Override
  public boolean removeTag(String key) {
      if (tags.containsKey(key)) {
          tags.remove(key);
          deleteTag(key);
          return true;
      }

      return false;
  }

  @Override
  public boolean hasTag(String key) {
      return tags.containsKey(key);
  }

  @Override
  public Collection<TagAttachment> getTags() {
      return Collections.unmodifiableCollection(tags.values());
  }

  @Override
  public Map<String, TagAttachment> getTagMap() {
      return Collections.unmodifiableMap(tags);
  }

}

package net.lordofthecraft.arche.account;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.lordofthecraft.arche.persona.TagAttachment;

@RequiredArgsConstructor
public class AgnosticTags<T> extends AbstractTags<T> {
	@Getter private final T holder;
	private final String tableName;
	private final String holderKeyName;
	private final Object holderKey;
	
	@Override
	protected void commitTag(TagAttachment tag) {
		getConsumer().replace(tableName)
			.set(holderKeyName, holderKey)
			.set(TAG_KEY, tag.getKey())
			.set(TAG_VALUE, tag.getValue())
			.queue();
	}

	@Override
	protected void deleteTag(String tagKey) {
		getConsumer().delete(tableName)
		.where(holderKeyName, holderKey)
		.where(TAG_KEY, tagKey)
		.queue();
	}
	
	void putInternal(String key, String value) {
		this.tags.put(key, new TagAttachment(key, value, false));
	}

}

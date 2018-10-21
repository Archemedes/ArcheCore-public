package net.lordofthecraft.arche.account;

import lombok.Getter;
import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.persona.TagAttachment;

public class AccountTags extends AbstractTags<Account> {
	@Getter
	private ArcheAccount holder;
	
	

	@Override
	protected void commitTag(TagAttachment tag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void deleteTag(String tagKey) {
		// TODO Auto-generated method stub
		
	}

}

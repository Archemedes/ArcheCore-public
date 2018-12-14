package net.lordofthecraft.arche.account;

import java.util.List;

import lombok.Value;
import net.lordofthecraft.arche.persona.ArchePersona;

@Value
public class AccountBlob {
		private ArcheAccount account;
		private List<ArchePersona> personas;
}

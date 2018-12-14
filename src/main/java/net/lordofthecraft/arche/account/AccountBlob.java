package net.lordofthecraft.arche.account;

import java.util.List;

import lombok.Getter;
import net.lordofthecraft.arche.persona.ArchePersona;

public class AccountBlob {
		@Getter private ArcheAccount account;
		@Getter private List<ArchePersona> personas;
}

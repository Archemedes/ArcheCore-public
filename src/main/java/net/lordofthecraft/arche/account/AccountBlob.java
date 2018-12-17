package net.lordofthecraft.arche.account;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.Value;
import net.lordofthecraft.arche.persona.ArchePersona;

@Value
public class AccountBlob {
		private final long week = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7);
		
		ArcheAccount account;
		List<ArchePersona> personas;
		
		Map<Integer, long[]> timeBlob = new HashMap<>();
		
		public void integrateSession(int personaId, long logout, long time_played) {
			//For account
			account.lastSeen = Math.max(account.lastSeen, logout);
			account.timePlayed += time_played;
			if(logout > week) account.timePlayedThisWeek += time_played;
			
			//For persona
			personas.stream().filter(ps->ps.getPersonaId() == personaId).findAny().ifPresent(ps->{
				ps.compareLastPlayed(logout);
				ps.addTimePlayed(time_played);
			});
		}
}

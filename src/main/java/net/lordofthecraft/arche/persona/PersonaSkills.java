package net.lordofthecraft.arche.persona;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;


public class PersonaSkills {
	private final Persona persona;
	private final List<SkillAttachment> profs = new ArrayList<>();
	private Skill mainProfession;
	
	PersonaSkills(Persona persona){
		this.persona = persona;
	}
	
	//TODO: Add this if the skill was loaded from SQL by ArchePersonaHandler
	void addSkillAttachment(SkillAttachment att) {
		profs.add(att);
	}
	
	void removeSkillAttachment(SkillAttachment att) {
		profs.remove(att);
	}
	
	Skill getMainProfession() {
		return mainProfession;
	}
	
	void setMainProfession(Skill main) {
		mainProfession = main;
	}
	
	public boolean hasSkill(Skill skill) {
		return profs.stream().filter( s -> s.skill == skill ).findAny().isPresent();
	}
	
	
	public SkillAttachment getSkill(Skill skill) {
		Optional<SkillAttachment> attach = profs.stream()
				.filter( s -> s.skill == skill )
				.findFirst();
		
		if(attach.isPresent()) {
			return attach.get();
		}else {
			SkillAttachment result = new SkillAttachment(skill, persona);
			return result;
		}
				
	}
	
}

package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class PersonaSkills {
	private final Persona persona;
	private final List<SkillAttachment> profs = new ArrayList<>();
	private Skill mainProfession;
	
	PersonaSkills(Persona persona){
		this.persona = persona;
	}
	
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
        return profs.stream().anyMatch(s -> s.skill == skill);
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

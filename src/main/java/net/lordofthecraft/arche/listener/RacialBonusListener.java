package net.lordofthecraft.arche.listener;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.persona.RaceBonusHandler;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class RacialBonusListener implements Listener {
	//private final UUID random_uuid = UUID.randomUUID();
	private final Random rnd = new Random();
	private final ArchePersonaHandler handler;
	private final ArcheCore plugin;
	private final HashSet<Byte> IGNORE_BLOCKS;

	private final List<String> sneakAttempts = Lists.newArrayList();
	private final List<String> sneakers = Lists.newArrayList();

	public RacialBonusListener(ArcheCore plugin, ArchePersonaHandler handler){
		this.plugin = plugin;
		this.handler = handler;
		IGNORE_BLOCKS = new HashSet<>();
		initIgnore();
	}

	private void initIgnore(){
		IGNORE_BLOCKS.add((byte) 0);
		IGNORE_BLOCKS.add((byte) 6);
		IGNORE_BLOCKS.add((byte) 31);
		IGNORE_BLOCKS.add((byte) 37);
		IGNORE_BLOCKS.add((byte) 38);
		IGNORE_BLOCKS.add((byte) 39);
		IGNORE_BLOCKS.add((byte) 40);
		IGNORE_BLOCKS.add((byte) 175);
	}

	private boolean hasTogglePower(Race race){
		switch(race){
		case OLOG:
		case ORC:
		case DARK_ELF:
		case KHARAJYR:
		case KHA_CHEETRAH:
		case KHA_PANTERA:
		case KHA_LEPARDA:
		case KHA_TIGRASI:
		case HOUZI_FEI:
		case HOUZI_LAO:
		case NEPHILIM:
		case SNOW_ELF:
			return true;
		default:
			return false;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent e){
		Player p = e.getPlayer();
		RaceBonusHandler.reset(p);
		//AttributeBase.clearModifiers(p, AttributeType.MOVEMENT_SPEED);
		//AttributeBase.clearModifiers(p, AttributeType.ATTACK_DAMAGE);

		Persona ps = handler.getPersona(p);
		if (ps != null) {
			RaceBonusHandler.apply(p, ps.getRace());
			if (handler.getRacespawns().containsKey(ps.getRace())) {
				e.setRespawnLocation(handler.getRacespawns().get(ps.getRace()));
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onSprint(PlayerToggleSprintEvent e){
		Player p = e.getPlayer();

		if(!e.isSprinting()) return;

		Persona ps = handler.getPersona(p);
		if(ps != null){
			if(ps.getRace() == Race.CONSTRUCT && p.getGameMode() != GameMode.CREATIVE){
				//p.sendMessage(ChatColor.RED + "You are moving too fast!");
				//p.damage(4);
				int hunger = p.getFoodLevel();
				p.setFoodLevel(4); //Ghetto but works.
				Bukkit.getScheduler().scheduleSyncDelayedTask(ArcheCore.getPlugin(), () -> p.setFoodLevel(hunger), 8);
				p.setSprinting(false);
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onHunger(FoodLevelChangeEvent e){
		Player p = (Player) e.getEntity();

		if(p.getFoodLevel() - e.getFoodLevel() == 1){ //Exactly 1 point drain, normal decay
			ArchePersona pers = handler.getPersona(p);
			if(pers != null){
				Race race = pers.getRace();

				if(race == Race.SPECTRE || race == Race.CONSTRUCT || race == Race.NECROLYTE){
					e.setCancelled(true);
					e.setFoodLevel(20);
				} else if(race == Race.ORC || race == Race.OLOG || race == Race.GOBLIN){
					//Allows eating of rotten food
					p.removePotionEffect(PotionEffectType.HUNGER);

					/*if(race == Race.OLOG && p.isSprinting()){
						e.setFoodLevel(Math.max(0, e.getFoodLevel() - 3));
					}*/
				}  else {
					if(rnd.nextBoolean())
						e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority= EventPriority.MONITOR)
	public void onSneak(PlayerToggleSneakEvent e){
		//Dark Elf Speed Boost on Sneak
		final Player p = e.getPlayer();
		if(e.isSneaking()){
			ArchePersona pers = handler.getPersona(p);
			if(pers != null && hasTogglePower(pers.getRace())){
				if(!sneakers.contains(p.getName())){
					if(!sneakAttempts.contains(p.getName())){ //Shift pressed down once.
						sneakAttempts.add(p.getName());
						new BukkitRunnable(){public void run(){sneakAttempts.remove(p.getName());}}.runTaskLater(plugin, 12);
					} else { //Shift pressed down twice in short time
						if (pers.getRace() == Race.DARK_ELF) {
							p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 1, true), true);
						}else if (pers.getRace().getParentRace().equalsIgnoreCase("Kharajyr")) {
							p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 450, 2, true), true);
							p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 240, 1, true), true);
						}else if(pers.getRace() == Race.OLOG || pers.getRace() == Race.ORC) {
							p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,100,1),true);
						}else if (pers.getRace().getParentRace().equalsIgnoreCase("hou-zi")) {
							AreaEffectCloud ae = (AreaEffectCloud) p.getWorld().spawnEntity(p.getLocation().clone().add(0, 0.5, 0), EntityType.AREA_EFFECT_CLOUD);
							ae.setParticle(Particle.CLOUD);
							ae.setDuration(5);
							ae.setRadius(3);
							ae.setWaitTime(0);

							Bukkit.getScheduler().runTaskLater(ArcheCore.getPlugin(), () -> p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 0, true)), 2);
						} else if (pers.getRace() == Race.NEPHILIM) {
							//breathe fire

						} else if (pers.getRace() == Race.SNOW_ELF) { //I had to. -501warhead
							Location l = p.getLocation();
							AreaEffectCloud cloud = (AreaEffectCloud) l.getWorld().spawnEntity(l, EntityType.AREA_EFFECT_CLOUD);
							cloud.setDuration(40);
							cloud.setColor(Color.WHITE);
							for (Entity en : l.getWorld().getNearbyEntities(l, 5, 5, 5)) {
								if (!(en instanceof LivingEntity)) continue;
								LivingEntity ent = (LivingEntity) en;
								if (ent.getLocation().distanceSquared(l) > 9) continue;
								if (ent.equals(pers.getPlayer())) continue;
								ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0, false, false));
								ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0, false, false));
								if (!(ent instanceof Player)) continue;
								Player targ = (Player) ent;
								targ.sendMessage(ChatColor.AQUA + "" + ChatColor.ITALIC + "You are blinded and slowed by a gust of snow!");
							}
							if (l.getBlock().getType() == Material.AIR && l.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
								l.getBlock().setType(Material.SNOW);
							} else {
								return;
							}
						}

						p.playSound(p.getLocation(), Sound.AMBIENT_CAVE, 0.8f, 2f);
						sneakers.add(p.getName());
						new BukkitRunnable(){public void run(){
							sneakers.remove(p.getName());
							p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
						}}.runTaskLater(plugin, 600);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDamage(EntityDamageByEntityEvent e){
		//Racial Resistance Bonuses
		if(e.getEntity() instanceof Player){
			Player p = (Player) e.getEntity();
			Persona pers = handler.getPersona(p);
			if(pers != null){
				Race r = pers.getRace();
				if(r == Race.NECROLYTE || r == Race.SPECTRE){
					if(holdsGoldenWeapon(e.getDamager())){
						double factor = r == Race.SPECTRE? 3 : 1.5;
						e.setDamage(e.getDamage()*factor);
					}
				} else if (r == Race.CONSTRUCT) {
					e.setDamage(DamageModifier.MAGIC, e.getDamage(DamageModifier.MAGIC) * 0.2);

				} else if (r == Race.HIGH_ELF) {
					e.setDamage(DamageModifier.MAGIC, e.getDamage(DamageModifier.MAGIC) * 0.7);
				} else if (r.getParentRace().equalsIgnoreCase("hou-zi") && r != Race.HOUZI_HEI && !isWearingArmor(p)) {
					e.setDamage(e.getDamage()*0.75);
				}
			}
		}
		if (e.getDamager() instanceof Player && e.getEntity() instanceof LivingEntity) {
			Player p = (Player) e.getDamager();
			LivingEntity ent = (LivingEntity) e.getEntity();
			Persona pers = handler.getPersona(p);
			if(pers != null) {
				Race r = pers.getRace();
				if (r.getParentRace().equalsIgnoreCase("hou-zi")
						&& r != Race.HOUZI_HEI
						&& p.hasPotionEffect(PotionEffectType.INVISIBILITY)
						&& (p.getEquipment().getItemInMainHand() == null || p.getEquipment().getItemInMainHand().getType() == Material.AIR)) {
					ent.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
					p.removePotionEffect(PotionEffectType.INVISIBILITY);
					ent.sendMessage(ChatColor.RED+"The "+r.getName()+" snuck up on you and delivered a quick strike, leaving you weakened!");
				}
			}
		}
		/*if (e.getDamager() instanceof Player) {
			Player p = (Player) e.getDamager();
			Persona pers = handler.getPersona(p);
			if (pers != null) {
				double dmg = e.getDamage();
				if(pers.getRace() == Race.SPECTRE){
					if (e.getCause() != DamageCause.MAGIC
							&& !(e.getEntity() instanceof ArmorStand)
							&& e.getEntity() instanceof LivingEntity
							&& !(e.getEntity() instanceof ItemFrame)) {
						final double dmg1 = e.getDamage();
						final double blocking = e.getDamage(DamageModifier.BLOCKING);
						e.setDamage(0);
						e.setDamage(DamageModifier.MAGIC, dmg1 + blocking);
						//Bukkit.broadcastMessage("spectre hit");
					}
				}
			}
		}*/

		//Racial Damage bonuses
		if (plugin.areRacialDamageBonusesEnabled()) {
			//Wood Elf arrow bonus
			if(e.getDamager() instanceof Arrow){ 
				if(((Arrow) e.getDamager()).getShooter() instanceof Player){
					Player p =  (Player) ((Arrow) e.getDamager()).getShooter();
					Persona pers = handler.getPersona(p);
					if(pers != null && pers.getRace() == Race.WOOD_ELF){
						double dmg = e.getDamage();
						dmg *= 1.1;
						e.setDamage(dmg);
					}
				}
			} else if (e.getDamager() instanceof Player){
				Player p = (Player) e.getDamager();
				Persona pers = handler.getPersona(p);
				if(pers != null){
					double dmg = e.getDamage();
					Race r = pers.getRace();
					switch(r){
					//Magical Affinity. A portion of damage a high elf does is converted to Magic damage. Increases if using a gold weapon.
					case HIGH_ELF:
						if (holdsGoldenWeapon(e.getDamager())) {
							e.setDamage(dmg * 0.5);
							e.setDamage(DamageModifier.MAGIC, dmg * 0.5);
						} else {
							e.setDamage(dmg * 0.8);
							e.setDamage(DamageModifier.MAGIC, dmg * 0.20);
						}
						break;
					case ORC:
					case OLOG:
						if (e.getDamage() > 2) {
							e.setDamage(dmg + 2);
						}
						break;
					case HUMAN:
					case SOUTHERON:
					case NORTHENER:
					case HEARTLANDER: //Troop Morale
						if (e.getEntity() instanceof Player) {
							int count = 0;
							for (Entity ent : p.getNearbyEntities(10, 5, 10)) {
								if (ent instanceof Player) {
									ArchePersona x = handler.getPersona((Player) ent);
									if (x != null && (x.getRace() == Race.HUMAN || x.getRace() == Race.NORTHENER || x.getRace() == Race.SOUTHERON || x.getRace() == Race.HEARTLANDER)) {
										if (++count >= 5 && e.getDamage() > 2) {
											dmg *= 1.25;
											e.setDamage(dmg);
											break;
										}
									}
								}
							}
						}
						break; 
					case SPECTRE:
						if (e.getCause() != DamageCause.MAGIC
						&& !(e.getEntity() instanceof ArmorStand)
						&& e.getEntity() instanceof LivingEntity
						&& !(e.getEntity() instanceof ItemFrame)) {
							final double dmg1 = e.getDamage();
							final double blocking = e.getDamage(DamageModifier.BLOCKING);
							e.setDamage(0);
							e.setDamage(DamageModifier.MAGIC, dmg1 + blocking);
							//Bukkit.broadcastMessage("spectre hit2");
						}
						break;
					case KHARAJYR:
					case KHA_TIGRASI:
					case KHA_PANTERA:
					case KHA_LEPARDA:
					case KHA_CHEETRAH:
						//Kitty got claws
						if (p.getEquipment().getItemInMainHand().getType() == Material.AIR)
							e.setDamage(dmg + 2);

						break;
					default:
						break;
					}
				}
			}
		}
	}


	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.LOWEST)
	public void onEntityMount(EntityMountEvent e) {
		if (e.getMount() instanceof Horse && e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if (handler.hasPersona(p)) {
				if (handler.getPersona(p).getRace() == Race.CONSTRUCT) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPotionDrink(PlayerItemConsumeEvent e) {
		if (e.getItem().getType() == Material.POTION) {
			if (handler.hasPersona(e.getPlayer())){
				if (handler.getPersona(e.getPlayer()).getRace() == Race.CONSTRUCT
						|| handler.getPersona(e.getPlayer()).getRace() == Race.SPECTRE
						|| handler.getPersona(e.getPlayer()).getRace() == Race.NECROLYTE) {
					e.setCancelled(true);
					e.getPlayer().getEquipment().setItemInMainHand(new ItemStack(Material.GLASS_BOTTLE, 1));
				}
			}
		}
	}

	private boolean holdsGoldenWeapon(Entity e){
		if(e instanceof LivingEntity){
			LivingEntity le = (LivingEntity) e;
			ItemStack is = le.getEquipment().getItemInMainHand();
			if(is != null){
				switch(is.getType()){
				case GOLD_SWORD:
				case GOLD_AXE:
				case GOLD_PICKAXE:
				case GOLD_SPADE:
				case GOLD_HOE:
				case GOLD_INGOT:
				case GOLD_BLOCK:
				case GOLD_NUGGET:
					return true;
				default:
					break;
				}
			}
		}
		return false;
	}

	private boolean isWearingArmor(Player p) {
		for (ItemStack is : p.getEquipment().getArmorContents()) {
			if (is != null && is.getType() != Material.ELYTRA && is.getType() != Material.PUMPKIN) {
				return true;
			}
		}
		return false;
	}


	@EventHandler(priority = EventPriority.LOWEST)
	public void onDamage(EntityDamageEvent e){

		if(e.getEntity() instanceof Player){
			ArchePersona pers = handler.getPersona((Player) e.getEntity());
			DamageCause c = e.getCause();
			if(pers == null) return;

			final Race r = pers.getRace();
			if(r == Race.SPECTRE || r == Race.CONSTRUCT){
				if(e.getCause() == DamageCause.POISON || e.getCause() == DamageCause.DROWNING){
					e.setCancelled(true);
				} else if( !(c == DamageCause.SUICIDE || c == DamageCause.VOID) ){
					double factor = r == Race.SPECTRE? 0.5:0.4;
					e.setDamage(e.getDamage() * factor);
				}
			}
			if(c == DamageCause.MAGIC){
				double dmg = e.getDamage();
				if(r == Race.HIGH_ELF){
					dmg *= 0.7;
					e.setDamage(dmg);
				} else if (r == Race.CONSTRUCT){
					dmg *=0.2;
					e.setDamage(dmg);
				}
			} else if (c == DamageCause.FALL){
				double dmg = e.getDamage();
				switch(r){
				case KHARAJYR:
				case KHA_CHEETRAH:
				case KHA_LEPARDA:
				case KHA_TIGRASI:
				case KHA_PANTERA:
				case HOUZI:
				case HOUZI_FEI:
				case HOUZI_LAO:
				case HOUZI_HEI:
					dmg -= 6;
					if (dmg <= 0) e.setCancelled(true);
					else e.setDamage(dmg);
				default:
					break;
				}
			}
		}
	}
}

package dev.redgamer6427a.core.minecraft.paper.util.constants;

import lombok.Getter;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.entity.vehicle.minecart.MinecartSpawner;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;


import java.util.List;
import java.util.Optional;

/**
 * Entity status codes used in the Entity Event packet (Java Edition protocol).
 * <p>
 * Multiple entity types can share the same status code with different client-side
 * behavior — each constant here represents one (code, entity-group) pairing.
 *
 * @see <a href="https://minecraft.wiki/w/Java_Edition_protocol/Entity_statuses">Java Edition protocol/Entity statuses</a>
 */
@Getter
public enum EntityEvent {

    /** Arrow. Spawns tipped arrow particle effects, if the color is not -1. */
    ARROW_TIPPED_PARTICLES(0, List.of(Arrow.class)),

    /** Minecart Spawner. Resets the spawner delay to 200 ticks (default minimum). */
    MINECART_SPAWNER_RESET_DELAY(1, List.of(MinecartSpawner.class)),

    /** Rabbit. Plays the rotated jump animation and spawns jump particles. */
    RABBIT_ROTATED_JUMP(1, List.of(Rabbit.class)),

    /** Living Entity. Plays the spear charge attack animation. */
    SPEAR_CHARGE(2, List.of(LivingEntity.class)),

    /** Egg. Displays 8 iconcrack particles (using the egg item) at the egg's location. */
    EGG_BREAK_PARTICLES(3, List.of(ThrownEgg.class)),

    /** Living Entity. Plays the death sound and death animation. */
    DEATH_ANIMATION(3, List.of(LivingEntity.class)),

    /** Snowball. Displays 8 snowballpoof particles at the snowball's location. */
    SNOWBALL_BREAK_PARTICLES(3, List.of(Snowball.class)),

    /** Evoker Fangs. Starts the attack animation and plays the attack sound. */
    EVOKER_FANGS_ATTACK(4, List.of(EvokerFangs.class)),

    /** Hoglin. Plays the attack animation for 10 ticks and plays the attack sound. */
    HOGLIN_ATTACK(4, List.of(Hoglin.class)),

    /** Iron Golem. Plays the attack animation and attack sound. */
    IRON_GOLEM_ATTACK(4, List.of(IronGolem.class)),

    /** Ravager. Starts the attack animation. */
    RAVAGER_ATTACK(4, List.of(Ravager.class)),

    /** Warden. Stops the roar animation and performs the attack animation. */
    WARDEN_ATTACK(4, List.of(Warden.class)),

    /** Zoglin. Plays the attack animation for 10 ticks and plays the attack sound. */
    ZOGLIN_ATTACK(4, List.of(Zoglin.class)),

    /** Abstract Horse, Tameable Animal. Spawns smoke particles (taming failed). */
    TAMING_FAILED_SMOKE(6, List.of(Horse.class, TamableAnimal.class)),

    /** Abstract Horse, Tameable Animal. Spawns heart particles (taming succeeded). */
    TAMING_SUCCEEDED_HEARTS(7, List.of(Horse.class, TamableAnimal.class)),

    /** Wolf. Plays the shaking-water animation. */
    WOLF_SHAKE_WATER_START(8, List.of(Wolf.class)),

    /**
     * Player. Marks an item use as finished (eating, drinking, etc).
     * Optional client-side shortcut — the server can otherwise drive these
     * changes manually or suppress them; must be paired with correct hand data.
     */
    PLAYER_ITEM_USE_FINISHED(9, List.of(Player.class)),

    /** Sheep. Plays the eating-grass animation for the next 40 ticks. */
    SHEEP_EAT_GRASS(10, List.of(Sheep.class)),

    /** Iron Golem. Holds out a poppy for 400 ticks (20 seconds). */
    IRON_GOLEM_HOLD_POPPY(11, List.of(IronGolem.class)),

    /** Villager. Spawns mating heart particles. */
    VILLAGER_MATING_HEARTS(12, List.of(Villager.class)),

    /** Villager. Spawns angry particles. */
    VILLAGER_ANGRY(13, List.of(Villager.class)),

    /** Villager. Spawns happy particles. */
    VILLAGER_HAPPY(14, List.of(Villager.class)),

    /** Witch. Spawns 10–45 witchMagic particles; ~0.075% chance per tick in vanilla. */
    WITCH_MAGIC_PARTICLES(15, List.of(Witch.class)),

    /** Zombie Villager. Plays the cure sound effect (unless the entity is silent). */
    ZOMBIE_VILLAGER_CURE_SOUND(16, List.of(ZombieVillager.class)),

    /** Firework Rocket. Triggers the explosion effect based on firework info metadata. */
    FIREWORK_EXPLODE(17, List.of(FireworkRocketEntity.class)),

    /** Allay. Spawns heart particles, used upon Allay duplication. */
    ALLAY_DUPLICATE_HEARTS(18, List.of(Allay.class)),

    /** Animal. Spawns "love mode" heart particles. */
    ANIMAL_LOVE_MODE_HEARTS(18, List.of(Animal.class)),

    /** Squid. Resets rotation to 0 radians once it exceeds 2π radians. */
    SQUID_RESET_ROTATION(19, List.of(Squid.class)),

    /**
     * Mob. Spawns explosion particle(s). Used when a silverfish enters/exits a
     * block, or when a mob spawner spawns an entity that supports this status.
     */
    MOB_EXPLOSION_PARTICLE(20, List.of(Mob.class)),

    /** Guardian. Plays the guardian attack sound effect from this entity. */
    GUARDIAN_ATTACK_SOUND(21, List.of(Guardian.class)),

    /** Player. Enables reduced debug screen information. */
    PLAYER_DEBUG_SCREEN_REDUCED_ON(22, List.of(Player.class)),

    /** Player. Disables reduced debug screen information. */
    PLAYER_DEBUG_SCREEN_REDUCED_OFF(23, List.of(Player.class)),

    /** Player. Sets client-side op permission level to 0. */
    OP_LEVEL_0(24, List.of(Player.class)),

    /** Player. Sets client-side op permission level to 1. */
    OP_LEVEL_1(25, List.of(Player.class)),

    /** Player. Sets client-side op permission level to 2. */
    OP_LEVEL_2(26, List.of(Player.class)),

    /** Player. Sets client-side op permission level to 3. */
    OP_LEVEL_3(27, List.of(Player.class)),

    /** Player. Sets client-side op permission level to 4. */
    OP_LEVEL_4(28, List.of(Player.class)),

    /** Living Entity. Plays the shield block sound. */
    SHIELD_BLOCK(29, List.of(LivingEntity.class)),

    /** Living Entity. Plays the shield break sound. */
    SHIELD_BREAK(30, List.of(LivingEntity.class)),

    /** Fishing Hook. If the caught entity is the connected player, pulls them toward the caster. */
    FISHING_HOOK_PULL_PLAYER(31, List.of(FishingHook.class)),

    /** Armor Stand. Plays the hit sound and resets its hit cooldown. */
    ARMOR_STAND_HIT(32, List.of(ArmorStand.class)),

    /** Iron Golem. Puts away its held poppy. */
    IRON_GOLEM_PUT_AWAY_POPPY(34, List.of(IronGolem.class)),

    /** Living Entity. Plays the totem-of-undying animation and sound. */
    TOTEM_OF_UNDYING(35, List.of(LivingEntity.class)),

    /** Dolphin. Spawns "happy villager" particles; used when fed and locating a structure. */
    DOLPHIN_HAPPY_PARTICLES(38, List.of(Dolphin.class)),

    /** Ravager. Marks the ravager as stunned for the next 40 ticks. */
    RAVAGER_STUNNED(39, List.of(Ravager.class)),

    /** Ocelot. Spawns smoke particles (taming failed). */
    OCELOT_TAMING_FAILED(40, List.of(Ocelot.class)),

    /** Ocelot. Spawns heart particles (taming succeeded). */
    OCELOT_TAMING_SUCCEEDED(41, List.of(Ocelot.class)),

    /** Villager. Spawns "splash" particles; ~1% chance per tick while a raid is active. */
    VILLAGER_RAID_SPLASH(42, List.of(Villager.class)),

    /**
     * Player. Spawns cloud particles at the player. Sent when a player's Bad
     * Omen effect is removed to either start a raid or increase its difficulty.
     */
    PLAYER_BAD_OMEN_CLOUD(43, List.of(Player.class)),

    /** Fox. Spawns particles based on the item held in its mouth to indicate chewing. */
    FOX_CHEWING_PARTICLES(45, List.of(Fox.class)),

    /** Living Entity. Spawns portal particles on teleport (chorus fruit consumption or enderman). */
    TELEPORT_PARTICLES(46, List.of(LivingEntity.class)),

    /** Living Entity. Plays equipment-break sound (unless silent) and spawns break particles for the main-hand item. */
    EQUIPMENT_BREAK_MAINHAND(47, List.of(LivingEntity.class)),

    /** Living Entity. Plays equipment-break sound (unless silent) and spawns break particles for the off-hand item. */
    EQUIPMENT_BREAK_OFFHAND(48, List.of(LivingEntity.class)),

    /** Living Entity. Plays equipment-break sound (unless silent) and spawns break particles for the head slot. */
    EQUIPMENT_BREAK_HEAD(49, List.of(LivingEntity.class)),

    /** Living Entity. Plays equipment-break sound (unless silent) and spawns break particles for the chest slot. */
    EQUIPMENT_BREAK_CHEST(50, List.of(LivingEntity.class)),

    /** Living Entity. Plays equipment-break sound (unless silent) and spawns break particles for the legs slot. */
    EQUIPMENT_BREAK_LEGS(51, List.of(LivingEntity.class)),

    /** Living Entity. Plays equipment-break sound (unless silent) and spawns break particles for the feet slot. */
    EQUIPMENT_BREAK_FEET(52, List.of(LivingEntity.class)),

    /** Entity. Spawns honey block slide particles at the entity's feet. */
    ENTITY_HONEY_SLIDE_PARTICLES(53, List.of(Entity.class)),

    /** Living Entity. Spawns honey block fall particles at the entity's feet. */
    HONEY_FALL_PARTICLES(54, List.of(LivingEntity.class)),

    /** Living Entity. Swaps the entity's main-hand and off-hand items. */
    SWAP_HAND_ITEMS(55, List.of(LivingEntity.class)),

    /** Wolf. Stops the shaking-water animation. */
    WOLF_SHAKE_WATER_STOP(56, List.of(Wolf.class)),

    /** Goat. Lowers its head in preparation for ramming. */
    GOAT_LOWER_HEAD(58, List.of(Goat.class)),

    /** Goat. Stops lowering its head. */
    GOAT_STOP_LOWERING_HEAD(59, List.of(Goat.class)),

    /** Living Entity. Spawns death smoke particles. */
    DEATH_SMOKE(60, List.of(LivingEntity.class)),

    /** Warden. Performs the tendril-shaking animation for 10 ticks. */
    WARDEN_TENDRIL_SHAKE(61, List.of(Warden.class)),

    /** Warden. Performs the sonic boom attack animation (charge and release; beam/sound not included). */
    WARDEN_SONIC_BOOM(62, List.of(Warden.class)),

    /** Sniffer. Plays the digging sound; only while it has a target and is in a digging/searching state. */
    SNIFFER_DIGGING_SOUND(63, List.of(Sniffer.class)),

    /** Minecart TNT. Ignites the TNT. Does not play a sound; must be triggered separately. */
    MINECART_TNT_IGNITE(70, List.of(MinecartTNT.class));

    /**
     * The raw status code sent in the Entity Event packet.
     */
    @Getter
    private final int code;

    /**
     * The entity type(s) this status applies to (Bukkit API classes).
     */
    private final List<Class<? extends net.minecraft.world.entity.Entity>> entities;

    EntityEvent(int code, List<Class<? extends net.minecraft.world.entity.Entity>> entities) {
        this.code = code;
        this.entities = entities;
    }
    public byte byteCode() {
        return (byte) code;    
    }
    
    public static Optional<EntityEvent> byCode(int code) {
        for (EntityEvent entityEvent : values()) {
            if (entityEvent.code == code) {
                return Optional.of(entityEvent);
            }
        }
        return Optional.empty();
    }
}
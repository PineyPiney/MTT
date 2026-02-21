package com.pineypiney.mtt.dnd.spells

import com.pineypiney.mtt.dnd.DamageType
import com.pineypiney.mtt.dnd.Duration
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.combat.CombatManager
import com.pineypiney.mtt.dnd.conditions.Conditions
import com.pineypiney.mtt.dnd.rolls.SavingThrow
import com.pineypiney.mtt.dnd.traits.Ability
import com.pineypiney.mtt.dnd.traits.Source
import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.minecraft.util.math.Vec3d

object Spells {

	val map = Int2ObjectMap.ofEntries(*Array(10) { AbstractInt2ObjectMap.BasicEntry(it, mutableSetOf<Spell>()) })

	fun register(spell: Spell) = spell.also {
		if (map.containsKey(spell.level)) map[spell.level].add(spell)
		else map[spell.level] = mutableSetOf(spell)
	}

	// Cantrips

	val ACID_SPLASH = register(
		DamageCantrip(
			"acid_splash", DamageType.ACID, 6, 1, Spell.Settings()
				.shape(SpellShape.Circle(5))
				.verbal()
				.semantic()
		)
	)

	val CHILL_TOUCH = register(object : DamageCantrip(
		"chill_touch", DamageType.NECROTIC, 10, 1, Settings()
			.school(School.NECROMANCY)
			.range(5)
			.targetsEntity()
			.verbal()
			.semantic()
	) {
		override fun apply(caster: Character, target: Character, level: Int, spellCastingAbility: Ability) {
			super.apply(caster, target, level, spellCastingAbility)
			target.conditions.apply(Source.SpellSource, Conditions.CHILL_TOUCH.createState(Duration.Turns(caster.uuid, 2)))
		}
	})

	val DANCING_LIGHTS = register(
		DamageCantrip(
			"dancing_lights", DamageType.ACID, 6, 1, Spell.Settings()
				.school(School.ILLUSION)
				.range(120)
				.shape(SpellShape.Sphere(5))
				.verbal()
				.semantic()
				.material()
				.concentration()
				.minutes(1)
		)
	)

	val DRUIDCRAFT = register(
		DamageCantrip(
			"druidcraft", DamageType.ACID, 6, 1, Spell.Settings()
				.school(School.TRANSMUTATION)
				.range(30)
				.verbal()
				.semantic()
		)
	)

	val ELEMENTALISM = register(object : Spell(
		"elementalism", 0, Settings()
			.school(School.TRANSMUTATION)
			.range(30)
			.verbal()
			.semantic()
	) {
		override fun cast(caster: Character, location: Vec3d, direction: Float, level: Int, spellCastingAbility: Ability, combat: CombatManager?) {

		}
	})

	val FIRE_BOLT = register(
		DamageCantrip(
			"fire_bolt", DamageType.FIRE, 10, 1, Spell.Settings()
				.school(School.TRANSMUTATION)
				.range(120)
				.targetsEntity()
				.verbal()
				.semantic()
		)
	)

	val LIGHT = register(object : Spell(
		"light", 0, Settings()
			.school(School.EVOCATION)
			.range(5)
			.hours(1)
			.verbal()
			.material()
	) {
		override fun cast(caster: Character, location: Vec3d, direction: Float, level: Int, spellCastingAbility: Ability, combat: CombatManager?) {

		}
	})

	val MAGE_HAND = register(object : Spell(
		"mage_hand", 0, Settings()
			.school(School.CONJURATION)
			.range(30)
			.minutes(1)
			.verbal()
			.semantic()
	) {
		override fun cast(caster: Character, location: Vec3d, direction: Float, level: Int, spellCastingAbility: Ability, combat: CombatManager?) {

		}
	})

	val MENDING = register(object : Spell(
		"mending", 0, Settings()
			.school(School.TRANSMUTATION)
			.range(5)
			.verbal()
			.semantic()
			.material()
	) {
		override fun cast(caster: Character, location: Vec3d, direction: Float, level: Int, spellCastingAbility: Ability, combat: CombatManager?) {

		}
	})

	val MESSAGE = register(object : Spell(
		"message", 0, Settings()
			.school(School.TRANSMUTATION)
			.range(120)
			.targetsEntity()
			.semantic()
			.material()
	) {
		override fun cast(caster: Character, location: Vec3d, direction: Float, level: Int, spellCastingAbility: Ability, combat: CombatManager?) {

		}
	})

	val MINOR_ILLUSION = register(object : Spell(
		"minor_illusion", 0, Settings()
			.school(School.ILLUSION)
			.range(30)
			.minutes(1)
			.semantic()
			.material()
	) {
		override fun cast(caster: Character, location: Vec3d, direction: Float, level: Int, spellCastingAbility: Ability, combat: CombatManager?) {

		}
	})

	val POISON_SPRAY = register(
		DamageCantrip(
			"poison_spray", DamageType.POISON, 12, 1, Spell.Settings()
				.school(School.NECROMANCY)
				.range(30)
				.verbal()
				.semantic()
		)
	)

	val PRESTIDIGITATION = register(
		DamageCantrip(
			"prestidigitation", DamageType.ACID, 6, 1, Spell.Settings()
				.school(School.TRANSMUTATION)
				.range(10)
				.verbal()
				.semantic()
				.minutes(1)
		)
	)

	val THAUMATURGY = register(
		DamageCantrip(
			"thaumaturgy", DamageType.ACID, 6, 1, Spell.Settings()
				.school(School.TRANSMUTATION)
				.range(30)
				.concentration()
				.minutes(1)
				.verbal()
		)
	)

	val RAY_OF_FROST = register(object : DamageCantrip(
		"ray_of_frost", DamageType.COLD, 8, 1, Settings()
			.school(School.EVOCATION)
			.targetsEntity()
			.verbal()
			.semantic()
	) {
		override fun apply(caster: Character, target: Character, level: Int, spellCastingAbility: Ability) {
			super.apply(caster, target, level, spellCastingAbility)
			target.conditions.apply(Source.SpellSource, Conditions.RAY_OF_FROST.createState(Duration.Turns(caster.uuid, 2)))
		}
	})

	val SHOCKING_GRASP = register(object : DamageCantrip(
		"shocking_grasp", DamageType.LIGHTNING, 8, 1, Settings()
			.school(School.EVOCATION)
			.range(5)
			.targetsEntity()
			.verbal()
			.semantic()
	) {
		override fun apply(caster: Character, target: Character, level: Int, spellCastingAbility: Ability) {
			super.apply(caster, target, level, spellCastingAbility)
			target.conditions.apply(Source.SpellSource, Conditions.SHOCKING_GRASP.createState(Duration.Turns(target.uuid, 1)))
		}
	})

	val TRUE_STRIKE = register(object : Spell(
		"true_strike", 0, Settings()
			.school(School.DIVINATION)
			.range(5)
			.targetsEntity()
			.verbal()
			.semantic()
	) {
		override fun cast(caster: Character, location: Vec3d, direction: Float, level: Int, spellCastingAbility: Ability, combat: CombatManager?) {
			caster.conditions.apply(Source.SpellSource, Conditions.TRUE_STRIKE.State(Duration.Turns(caster.uuid, 1), spellCastingAbility, true))
		}
	})

	// Level 1

	val ALARM = register(object : Spell(
		"alarm", 1, Settings()
			.range(30)
			.hours(8)
			.ritual()
			.verbal()
			.semantic()
			.material()
	) {
		override fun cast(caster: Character, location: Vec3d, direction: Float, level: Int, spellCastingAbility: Ability, combat: CombatManager?) {

		}
	})

	val BURNING_HANDS = register(
		BasicDamageSpell(
			"burning_hands", 1, DamageType.FIRE, 6, 3, Spell.Settings()
				.school(School.EVOCATION)
				.range(0)
				.shape(SpellShape.Cone(15))
				.verbal()
				.semantic()
		)
	)

	val CHARM_PERSON = register(object : Spell(
		"charm_person", 1, Settings()
			.range(30)
			.targetsEntity()
			.hours(1)
			.verbal()
			.semantic()
	) {
		override fun cast(caster: Character, location: Vec3d, direction: Float, level: Int, spellCastingAbility: Ability, combat: CombatManager?) {
			val characters = combat?.combatants ?: caster.engine.getAllCharacters()
			val target = characters.firstOrNull { it.pos == location } ?: return
			if (!target.rollSavingThrow(SavingThrow(Ability.WISDOM, getSaveThreshold(caster, spellCastingAbility)))) {
				target.conditions.apply(Source.SpellSource, Conditions.CHARMED.State(caster.uuid, Duration.Time(3600)))
			}
		}
	})

	val DETECT_MAGIC = register(
		BasicDamageSpell(
			"detect_magic", 1, DamageType.ACID, 6, 1, Spell.Settings()
				.school(School.DIVINATION)
				.range(0)
				.minutes(10)
				.concentration()
				.verbal()
				.semantic()
		)
	)

	val FAERIE_FIRE = register(
		BasicDamageSpell(
			"faerie_fire", 1, DamageType.ACID, 6, 1, Spell.Settings()
				.school(School.EVOCATION)
				.shape(SpellShape.Cube(20))
				.minutes(1)
				.concentration()
				.verbal()
		)
	)

	val FALSE_LIFE = register(
		BasicDamageSpell(
			"false_life", 1, DamageType.ACID, 6, 1, Spell.Settings()
				.school(School.NECROMANCY)
				.range(0)
				.verbal()
				.semantic()
				.material()
		)
	)

	val GREASE = register(
		BasicDamageSpell(
			"grease", 1, DamageType.ACID, 6, 1, Spell.Settings()
				.verbal()
				.semantic()
				.material()
		)
	)

	val HELLISH_REBUKE = register(
		BasicDamageSpell(
			"hellish_rebuke", 1, DamageType.FIRE, 6, 1, Spell.Settings()
				.targetsEntity()
				.verbal()
				.semantic()
		)
	)

	val LONGSTRIDER = register(
		BasicDamageSpell(
			"longstrider", 1, DamageType.ACID, 6, 1, Spell.Settings()
				.school(School.TRANSMUTATION)
				.range(5)
				.hours(1)
				.verbal()
				.semantic()
				.material()
		)
	)

	val RAY_OF_SICKNESS = register(
		BasicDamageSpell(
			"ray_of_sickness", 0, DamageType.ACID, 6, 1, Spell.Settings()
				.targetsEntity()
				.verbal()
				.semantic()
		)
	)

	// Level 2

	val DARKNESS = register(
		BasicDamageSpell(
			"darkness", 2, DamageType.ACID, 6, 1, Spell.Settings()
				.school(School.EVOCATION)
				.shape(SpellShape.Sphere(15))
				.concentration()
				.minutes(10)
				.verbal()
				.material()
		)
	)

	val HOLD_PERSON = register(
		BasicDamageSpell(
			"hold_person", 2, DamageType.ACID, 6, 1, Spell.Settings()
				.school(School.ENCHANTMENT)
				.targetsEntity()
				.concentration()
				.minutes(1)
				.verbal()
				.semantic()
				.material()
		)
	)

	val MISTY_STEP = register(
		BasicDamageSpell(
			"misty_step", 2, DamageType.ACID, 6, 1, Spell.Settings()
				.school(School.CONJURATION)
				.range(30)
				.verbal()
		)
	)

	val PASS_WITHOUT_TRACE = register(
		BasicDamageSpell(
			"pass_without_trace", 2, DamageType.ACID, 6, 1, Spell.Settings()
				.range(0)
				.concentration()
				.hours(1)
				.verbal()
				.semantic()
				.material()
		)
	)

	val RAY_OF_ENFEEBLEMENT = register(
		BasicDamageSpell(
			"ray_of_enfeeblement", 2, DamageType.ACID, 6, 1, Spell.Settings()
				.school(School.NECROMANCY)
				.targetsEntity()
				.concentration()
				.minutes(1)
				.verbal()
				.semantic()
		)
	)
}
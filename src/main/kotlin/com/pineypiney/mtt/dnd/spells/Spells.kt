package com.pineypiney.mtt.dnd.spells

import com.pineypiney.mtt.dnd.DamageType

object Spells {

	val set = mutableSetOf<Spell>()

	fun register(spell: Spell) = spell.also { set.add(spell) }

	// Cantrips

	val ACID_SPLASH = register(
		AreaDamageCantrip(
			"acid_splash", DamageType.ACID, 6, 1,
			Spell.Settings()
				.verbal()
				.semantic()
				.shape(SpellShape.Circle(5))
		)
	)

	val CHILL_TOUCH = register(
		AreaDamageCantrip(
			"chill_touch", DamageType.NECROTIC, 10, 1,
			Spell.Settings()
				.school(School.NECROMANCY)
				.range(5)
				.shape(SpellShape.Single)
				.verbal()
				.semantic()
		)
	)

	val DANCING_LIGHTS = register(
		AreaDamageCantrip(
			"dancing_lights", DamageType.ACID, 6, 1,
			Spell.Settings()
				.school(School.ILLUSION)
				.range(120)
				.verbal()
				.semantic()
				.material()
				.concentration()
				.minutes(1)
				.shape(SpellShape.Sphere(5))
		)
	)

	val DRUIDCRAFT = register(
		AreaDamageCantrip(
			"druidcraft", DamageType.ACID, 6, 1,
			Spell.Settings()
				.school(School.TRANSMUTATION)
				.range(30)
				.shape(SpellShape.Single)
				.verbal()
				.semantic()
		)
	)

	val FIRE_BOLT = register(
		AreaDamageCantrip(
			"fire_bolt", DamageType.FIRE, 10, 1,
			Spell.Settings()
				.school(School.EVOCATION)
				.range(120)
				.shape(SpellShape.Single)
				.verbal()
				.semantic()
		)
	)

	val POISON_SPRAY = register(
		AreaDamageCantrip(
			"poison_spray", DamageType.POISON, 12, 1,
			Spell.Settings()
				.school(School.NECROMANCY)
				.range(30)
				.shape(SpellShape.Single)
				.verbal()
				.semantic()
		)
	)

	val PRESTIDIGITATION = register(
		AreaDamageCantrip(
			"prestidigitation", DamageType.ACID, 6, 1,
			Spell.Settings()
				.school(School.TRANSMUTATION)
				.range(10)
				.shape(SpellShape.Single)
				.verbal()
				.semantic()
				.minutes(1)
		)
	)

	val THAUMATURGY = register(
		AreaDamageCantrip(
			"thaumaturgy", DamageType.ACID, 6, 1,
			Spell.Settings()
				.school(School.TRANSMUTATION)
				.range(30)
				.shape(SpellShape.Single)
				.concentration()
				.minutes(1)
				.verbal()
		)
	)

	// Level 1

	val DETECT_MAGIC = register(
		BasicDamageSpell(
			"detect_magic", 1, DamageType.ACID, 6, 1,
			Spell.Settings()
				.school(School.DIVINATION)
				.range(0)
				.shape(SpellShape.Single)
				.concentration()
				.minutes(10)
				.verbal()
				.semantic()
		)
	)

	val FAERIE_FIRE = register(
		BasicDamageSpell(
			"faerie_fire", 1, DamageType.ACID, 6, 1,
			Spell.Settings()
				.school(School.EVOCATION)
				.shape(SpellShape.Cube(20))
				.targetsEntity()
				.concentration()
				.minutes(1)
				.verbal()
		)
	)

	val FALSE_LIFE = register(
		BasicDamageSpell(
			"false_life", 1, DamageType.ACID, 6, 1,
			Spell.Settings()
				.school(School.NECROMANCY)
				.range(0)
				.shape(SpellShape.Single)
				.verbal()
				.semantic()
				.material()
		)
	)

	val GREASE = register(
		BasicDamageSpell(
			"grease", 1, DamageType.ACID, 6, 1,
			Spell.Settings()
				.verbal()
				.semantic()
				.material()
		)
	)

	val HELLISH_REBUKE = register(
		BasicDamageSpell(
			"hellish_rebuke", 1, DamageType.FIRE, 6, 1,
			Spell.Settings()
				.targetsEntity()
				.verbal()
				.semantic()
		)
	)

	val LONGSTRIDER = register(
		BasicDamageSpell(
			"longstrider", 1, DamageType.ACID, 6, 1,
			Spell.Settings()
				.school(School.TRANSMUTATION)
				.range(5)
				.shape(SpellShape.Single)
				.hours(1)
				.verbal()
				.semantic()
				.material()
		)
	)

	val RAY_OF_SICKNESS = register(
		BasicDamageSpell(
			"ray_of_sickness", 0, DamageType.ACID, 6, 1,
			Spell.Settings()
				.shape(SpellShape.Single)
				.targetsEntity()
				.verbal()
				.semantic()
		)
	)

	// Level 2

	val DARKNESS = register(
		BasicDamageSpell(
			"darkness", 2, DamageType.ACID, 6, 1,
			Spell.Settings()
				.school(School.EVOCATION)
				.shape(SpellShape.Sphere(15))
				.targetsEntity()
				.concentration()
				.minutes(10)
				.verbal()
				.material()
		)
	)

	val HOLD_PERSON = register(
		BasicDamageSpell(
			"hold_person", 2, DamageType.ACID, 6, 1,
			Spell.Settings()
				.school(School.ENCHANTMENT)
				.range(60)
				.shape(SpellShape.Single)
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
			"misty_step", 2, DamageType.ACID, 6, 1,
			Spell.Settings()
				.school(School.CONJURATION)
				.range(30)
				.shape(SpellShape.Single)
				.verbal()
		)
	)

	val PASS_WITHOUT_TRACE = register(
		BasicDamageSpell(
			"pass_without_trace", 2, DamageType.ACID, 6, 1,
			Spell.Settings()
				.school(School.ABJURATION)
				.range(0)
				.shape(SpellShape.Single)
				.concentration()
				.hours(1)
				.verbal()
				.semantic()
				.material()
		)
	)

	val RAY_OF_ENFEEBLEMENT = register(
		BasicDamageSpell(
			"ray_of_enfeeblement", 2, DamageType.ACID, 6, 1,
			Spell.Settings()
				.school(School.NECROMANCY)
				.range(60)
				.shape(SpellShape.Single)
				.targetsEntity()
				.concentration()
				.minutes(1)
				.verbal()
				.semantic()
		)
	)
}
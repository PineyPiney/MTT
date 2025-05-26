package com.pineypiney.mtt.dnd.traits

class Size(val name: String, val shortest: Float, val tallest: Float, val width: Float) : Comparable<Size> {

	override fun compareTo(other: Size): Int = (shortest + tallest).compareTo(other.shortest + other.tallest)

	override fun toString(): String {
		return "Size($name)"
	}

	companion object {
		val TINY = Size("tiny", 1f, 2f, .5f)
		val SMALL = Size("small", 2f, 4f, 1f)
		val MEDIUM = Size("medium", 4f, 8f, 1f)
		val LARGE = Size("large", 8f, 16f, 2f)
		val HUGE = Size("huge", 16f, 32f, 3f)
		val GARGANTUAN = Size("gargantuan", 32f, 64f, 4f)

		fun fromString(string: String): Size{
			return when(string){
				"tiny" -> TINY
				"small" -> SMALL
				"large" -> LARGE
				"huge" -> HUGE
				"gargantuan" -> GARGANTUAN
				else -> MEDIUM
			}
		}
	}
}
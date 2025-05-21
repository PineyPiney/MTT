package com.pineypiney.mtt.dice

class DiceFormulaError: Error {

	constructor() : super()
	constructor(message: String?) : super(message)
	constructor(message: String?, cause: Throwable?) : super(message, cause)
	constructor(message: String?, cause: Throwable?, suppression: Boolean, writableStackTrace: Boolean) : super(message, cause, suppression, writableStackTrace)
}
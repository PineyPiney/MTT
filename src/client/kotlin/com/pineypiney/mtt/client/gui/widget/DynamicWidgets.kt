package com.pineypiney.mtt.client.gui.widget

import com.mojang.blaze3d.pipeline.RenderPipeline
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.DrawContext
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

class DynamicWidgets {

	companion object {
		const val c1 = 1.70158f
		const val c2 = c1 * 1.525f
		const val c5 = 0.44444445f * PI.toFloat()

		fun easeInOutBack(s: Float): Float{
			return if (s < .5f) ((2f * s).pow(2f) * ((c2  + 1f) * 2f * s - c2)) * .5f
			else ((2f * s - 2f).pow(2f) * ((c2 + 1f) * (2f * s - 2f) + c2) + 2f) * .5f
		}

		fun easeInOutElastic(s: Float): Float{
			return if(s == 0f) 0f
			else if(s == 1f) 1f
			else if(s < .5f) 	(2f.pow(20f * s - 10f) * sin((20 * s - 11.125f) * c5)) * -.5f
			else 				(2f.pow(10f - s * 20f) * sin((20 * s - 11.125f) * c5)) * .5f + 1f

		}

		fun drawRoundedBorder(ctx: DrawContext, x: Int, y: Int, w: Int, h: Int, colour: Int = -12632257, fill: Int = 0){
			val r = x + w
			val b = y + h
			if(fill != 0) ctx.fill(x + 1, y + 1, r - 1, b - 1, fill)

			ctx.drawHorizontalLine(x + 2, r - 3, y, colour)
			ctx.drawHorizontalLine(x + 2, r - 3, b - 1, colour)
			ctx.drawVerticalLine(x, y + 1, b - 2, colour)
			ctx.drawVerticalLine(r - 1, y + 1, b - 2, colour)

			ctx.fill(x + 1, y + 1, x + 2, y + 2, colour)
			ctx.fill(r - 2, y + 1, r - 1, y + 2, colour)
			ctx.fill(x + 1, b - 2, x + 2, b - 1, colour)
			ctx.fill(r - 2, b - 2, r - 1, b - 1, colour)
		}

		fun drawThinBox(ctx: DrawContext, x: Int, y: Int, w: Int, h: Int, colour: Int = -3750202, topColour: Int = -1, bottomColour: Int = -11184811) {
			if(h <= 1) return

			val r = x + w
			val b = y + h
			if(h == 2){
				ctx.fill(r - 1, y, r, y + 1, colour)
				ctx.fill(x, b - 1, x + 1, b, colour)
				ctx.fill(x, y, r - 1, y + 1, topColour)
				ctx.fill(x + 1, y + 1, r, b, bottomColour)
				return
			}

			ctx.fill(x + 1, y + 1, r - 1, b - 1, colour)
			ctx.fill(r - 1, y, r, y + 1, colour)
			ctx.fill(x, b - 1, x + 1, b, colour)

			ctx.fill(x, y, r - 1, y + 1, topColour)
			ctx.fill(x, y + 1, x + 1, b - 1, topColour)

			ctx.fill(x + 1, b - 1, r, b, bottomColour)
			ctx.fill(r - 1, y + 1, r, b - 1, bottomColour)
		}

		fun drawThickBox(
			ctx: DrawContext,
			x: Int,
			y: Int,
			w: Int,
			h: Int,
			colour: Int = -6052957,
			topColour: Int = -9605779,
			bottomColour: Int = -1,
			pipeline: RenderPipeline = RenderPipelines.GUI
		) {
			if(h <= 1) return

			val r = x + w
			val b = y + h
			if(h <= 4){
				val hy = y + (h + 1).floorDiv(2)
				ctx.fill(pipeline, x + 1, y, r - 2, hy, topColour)
				ctx.fill(pipeline, x + 2, hy, r - 1, b, bottomColour)
				return
			}

			ctx.fill(pipeline, x + 2, y + 2, r - 2, b - 2, colour)
			ctx.fill(pipeline, x + 1, b - 2, x + 2, b - 1, colour)
			ctx.fill(pipeline, r - 2, y + 1, r - 1, y + 2, colour)

			ctx.fill(pipeline, x + 1, y, r - 2, y + 2, topColour)
			ctx.fill(pipeline, x, y + 1, x + 2, b - 2, topColour)
			ctx.fill(pipeline, x + 2, y + 2, x + 3, y + 3, topColour)

			ctx.fill(pipeline, x + 2, b - 2, r - 1, b, bottomColour)
			ctx.fill(pipeline, r - 2, y + 2, r, b - 1, bottomColour)
			ctx.fill(pipeline, r - 3, b - 3, r - 2, b - 2, bottomColour)
		}

		fun drawThickBoxWithBorder(
			ctx: DrawContext,
			x: Int,
			y: Int,
			w: Int,
			h: Int,
			colour: Int = -3750202,
			topColour: Int = -1,
			bottomColour: Int = -11184811,
			borderColour: Int = -16777216,
			pipeline: RenderPipeline = RenderPipelines.GUI
		) {
			drawThickBox(ctx, x + 1, y + 1, w - 2, h - 2, colour, topColour, bottomColour, pipeline)
			val r = x + w
			val b = y + h
			ctx.fill(pipeline, x + 2, y, r - 3, y + 1, borderColour)
			ctx.fill(pipeline, x + 3, b - 1, r - 2, b, borderColour)
			ctx.fill(pipeline, x, y + 2, x + 1, b - 3, borderColour)
			ctx.fill(pipeline, r - 1, y + 3, r, b - 2, borderColour)
			ctx.fill(pipeline, x + 1, y + 1, x + 2, y + 2, borderColour)
			ctx.fill(pipeline, r - 3, y + 1, r - 2, y + 2, borderColour)
			ctx.fill(pipeline, r - 2, y + 2, r - 1, y + 3, borderColour)
			ctx.fill(pipeline, x + 1, b - 3, x + 2, b - 2, borderColour)
			ctx.fill(pipeline, x + 2, b - 2, x + 3, b - 1, borderColour)
			ctx.fill(pipeline, r - 2, b - 2, r - 1, b - 1, borderColour)

		}

		fun drawScroller(ctx: DrawContext, x: Int, y: Int, w: Int, h: Int, colour: Int = -3750202, topColour: Int = -1, bottomColour: Int = -11184811, lineColour: Int = -7631989){
			val r = x + w
			val b = y + h

			ctx.fill(x + 1, y + 1, r - 1, b - 1, colour)

			ctx.fill(x, y, r - 1, y + 1, topColour)
			ctx.fill(x, y + 1, x + 1, b - 1, topColour)

			ctx.fill(x + 1, b - 1, r, b, bottomColour)
			ctx.fill(r - 1, y + 1, r, b - 1, bottomColour)

			val numLines = (h - 3) / 2
			for(line in 1..numLines) ctx.fill(x + 2, y + 2 * line, r - 2, y + 2 * line + 1, lineColour)
		}
	}
}
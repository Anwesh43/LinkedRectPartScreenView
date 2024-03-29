package com.anwesh.uiprojects.rectpartscreenview

/**
 * Created by anweshmishra on 10/09/19.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Color
import android.graphics.Canvas
import android.content.Context
import android.app.Activity

val colors : Array<String> = arrayOf("#01579B", "#f44336", "#00C853", "#FF5722", "#4A148C")
val parts : Int = 5
val scGap : Float = 0.01f / parts
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 5

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n

fun Canvas.drawRectPartScreen(i : Int, sc1 : Float, sc2 : Float, w : Float, h : Float, shouldFill : Boolean, paint : Paint) {
    val sc1i : Float = sc1.divideScale(i, parts)
    val sc2i : Float = sc2.divideScale(i, parts)
    var wi : Float = 0f
    if (sc2i > 0f) {
        wi = w * sc2i
    }
    if (shouldFill) {
        wi = w
    }
    save()
    translate(0f, h * i)
    drawRect(RectF(w * sc1i, 0f, wi, h), paint)
    restore()
}

fun Canvas.drawRectParts(sc1 : Float, sc2 : Float, w : Float, h : Float, shouldFill : Boolean, paint : Paint) {
    for (j in 0..(parts - 1)) {
        drawRectPartScreen(j, sc1, sc2, w, h, shouldFill, paint)
    }
}

fun Canvas.drawRPSNode(i : Int, scale : Float, sc : Float, currI : Int, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val hSize : Float = h / parts
    paint.color = Color.parseColor(colors[i])
    save()
    drawRectParts(scale, sc, w, hSize, i == currI, paint)
    restore()
}

class RectPartScreenView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += dir * scGap
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class RPSNode(var i : Int, val state : State = State()) {

        private var next : RPSNode? = null
        private var prev : RPSNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = RPSNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, sc : Float, currI : Int, paint : Paint) {
            canvas?.drawRPSNode(i, state.scale, sc, currI, paint)
            if (state.scale > 0f) {
                next?.draw(canvas, state.scale, currI, paint)
            }
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : RPSNode {
            var curr : RPSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class RectPartScreen(var i : Int) {

        private val root : RPSNode = RPSNode(0)
        private var curr : RPSNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, 0f, curr.i, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : RectPartScreenView) {

        private val rps : RectPartScreen = RectPartScreen(0)
        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            rps.draw(canvas, paint)
            animator.animate {
                rps.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            rps.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : RectPartScreenView {
            val view : RectPartScreenView = RectPartScreenView(activity)
            activity.setContentView(view)
            return view
        }
    }
}
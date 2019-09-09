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
val parts : Int = 3
val scGap : Float = 0.01f
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 25

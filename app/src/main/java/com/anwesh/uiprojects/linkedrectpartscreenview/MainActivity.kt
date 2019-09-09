package com.anwesh.uiprojects.linkedrectpartscreenview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.rectpartscreenview.RectPartScreenView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RectPartScreenView.create(this)
    }
}

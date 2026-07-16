package io.github.kenneycode.openglespro.samples.fragment

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import io.github.kenneycode.openglespro.R
import io.github.kenneycode.openglespro.samples.renderer.SampleScaleCubeRenderer

/**
 *
 *      Coded by kenney
 *
 *      http://www.github.com/kenneycode
 *
 *      带有刻度的立方体示例 (优化版)
 *      Scale cube sample (Optimized)
 *
 **/

class SampleScaleCube : Fragment() {

    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var renderer: SampleScaleCubeRenderer
    
    private var lastX = 0f
    private var lastY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_sample_scale_cube, container,  false)
        glSurfaceView = rootView.findViewById(R.id.glsurfaceview)
        glSurfaceView.setEGLContextClientVersion(3)
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 8, 0)

        renderer = SampleScaleCubeRenderer()
        glSurfaceView.setRenderer(renderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        val inputX = rootView.findViewById<EditText>(R.id.inputX)
        val inputY = rootView.findViewById<EditText>(R.id.inputY)
        val inputZ = rootView.findViewById<EditText>(R.id.inputZ)
        val setBtn = rootView.findViewById<Button>(R.id.setBtn)

        setBtn.setOnClickListener {
            try {
                val x = inputX.text.toString().toFloat()
                val y = inputY.text.toString().toFloat()
                val z = inputZ.text.toString().toFloat()
                
                // 限制输入范围
                if (x < -10 || x > 10 || y < 0 || y > 30 || z < -10 || z > 10) {
                    Toast.makeText(context, "X:[-10,10], Y:[0,30], Z:[-10,10]", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                renderer.targetX = x
                renderer.targetY = y
                renderer.targetZ = z
                glSurfaceView.requestRender()
            } catch (e: Exception) {
                Toast.makeText(context, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            }
        }

        glSurfaceView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.x
                    lastY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    renderer.rotateY += dx / 5f
                    renderer.rotateX += dy / 5f
                    lastX = event.x
                    lastY = event.y
                    glSurfaceView.requestRender()
                }
            }
            true
        }

        return rootView
    }
}

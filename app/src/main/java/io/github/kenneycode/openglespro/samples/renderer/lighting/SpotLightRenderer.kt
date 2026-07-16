package io.github.kenneycode.openglespro.samples.renderer.lighting

import android.opengl.GLES30
import javax.microedition.khronos.opengles.GL10

/**
 *
 *      Coded by kenney
 *
 *      http://www.github.com/kenneycode
 *
 *      聚光例子
 *      Spot light sample
 *
 **/

class SpotLightRenderer : LightingRenderer("lighting/vertex_spotlight.glsl", "lighting/fragment_spotlightsoftedge.glsl") {
    
    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)
        GLES30.glUniform3f(GLES30.glGetUniformLocation(programId, "lightPos"), 2f, 0f, 0f)
    }

}
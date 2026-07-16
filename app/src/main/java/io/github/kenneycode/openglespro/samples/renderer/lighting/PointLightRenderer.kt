package io.github.kenneycode.openglespro.samples.renderer.lighting

import android.opengl.GLES30
import javax.microedition.khronos.opengles.GL10

/**
 *
 *      Coded by kenney
 *
 *      http://www.github.com/kenneycode
 *
 *      点光例子
 *      Point light sample
 *
 **/

class PointLightRenderer : LightingRenderer("lighting/vertex_pointlight.glsl", "lighting/fragment_pointlight.glsl") {

    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)
        GLES30.glUniform3f(GLES30.glGetUniformLocation(programId, "lightPos"), 5f, 0f, 0f)
    }

}
package io.github.kenneycode.openglespro.samples.renderer.lighting

import android.opengl.GLES30
import android.util.Log
import javax.microedition.khronos.opengles.GL10

/**
 *
 *      Coded by kenney
 *
 *      http://www.github.com/kenneycode
 *
 *      平行光光照例子
 *      Directional light sample
 *
 **/

class DirectionalLightRenderer : LightingRenderer("lighting/vertex_directionallight.glsl", "lighting/fragment_directionallight.glsl") {

    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)
        GLES30.glUniform3f(GLES30.glGetUniformLocation(programId, "lightDirection"), -5f, 0f, 0f)
        Log.i("DirectionalLightRenderer", "onDrawFrame: errorcode: ${GLES30.glGetError()}")
//        assert(GLES30.glGetError() == 0)
    }

}
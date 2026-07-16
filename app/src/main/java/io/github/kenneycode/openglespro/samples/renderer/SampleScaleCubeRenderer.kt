package io.github.kenneycode.openglespro.samples.renderer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SampleScaleCubeRenderer : GLSurfaceView.Renderer, OnParameterChangeCallback {

    private val vertexShaderCode =
        "#version 300 es\n" +
        "layout(location = 0) in vec4 a_position;\n" +
        "layout(location = 1) in vec2 a_texCoord;\n" +
        "uniform mat4 u_mvp;\n" +
        "uniform float u_pointSize;\n" +
        "out vec2 v_texCoord;\n" +
        "void main() {\n" +
        "    gl_Position = u_mvp * a_position;\n" +
        "    gl_PointSize = u_pointSize;\n" +
        "    v_texCoord = a_texCoord;\n" +
        "}"

    private val fragmentShaderCode =
        "#version 300 es\n" +
        "precision mediump float;\n" +
        "in vec2 v_texCoord;\n" +
        "out vec4 fragColor;\n" +
        "uniform vec4 u_color;\n" +
        "uniform sampler2D u_texture;\n" +
        "uniform bool u_useTexture;\n" +
        "void main() {\n" +
        "    if (u_useTexture) {\n" +
        "        fragColor = texture(u_texture, v_texCoord) * u_color;\n" +
        "    } else {\n" +
        "        fragColor = u_color;\n" +
        "    }\n" +
        "}"

    private var programId = 0
    private var uMvpLocation = 0
    private var uColorLocation = 0
    private var uPointSizeLocation = 0
    private var uUseTextureLocation = 0
    private var uTextureLocation = 0

    private var glSurfaceViewWidth = 0
    private var glSurfaceViewHeight = 0

    var rotateX = 20f
    var rotateY = -30f
    
    // 用户输入的坐标 (X: -10 to 10, Y: 0 to 30, Z: -10 to 10)
    var targetX = 0f
    var targetY = 15f
    var targetZ = 0f

    // 立方体在GL坐标系中的范围
    private val glMinX = -10f
    private val glMaxX = 10f
    private val glMinY = -30f
    private val glMaxY = 0f
    private val glMinZ = -10f
    private val glMaxZ = 10f

    // 缓存文本纹理
    private val textTextures = mutableMapOf<String, Int>()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.05f, 0.05f, 0.05f, 1f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        programId = GLES30.glCreateProgram()
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)
        GLES30.glAttachShader(programId, vertexShader)
        GLES30.glAttachShader(programId, fragmentShader)
        GLES30.glLinkProgram(programId)

        uMvpLocation = GLES30.glGetUniformLocation(programId, "u_mvp")
        uColorLocation = GLES30.glGetUniformLocation(programId, "u_color")
        uPointSizeLocation = GLES30.glGetUniformLocation(programId, "u_pointSize")
        uUseTextureLocation = GLES30.glGetUniformLocation(programId, "u_useTexture")
        uTextureLocation = GLES30.glGetUniformLocation(programId, "u_texture")
        
        prepareTextTextures()
    }

    private fun prepareTextTextures() {
        // 预生成需要的数字纹理
        val texts = mutableListOf<String>()
        for (i in -10..10 step 5) texts.add(i.toString())
        for (i in 0..30 step 5) texts.add(i.toString())
        
        texts.distinct().forEach { text ->
            textTextures[text] = createTextTexture(text)
        }
    }

    private fun createTextTexture(text: String): Int {
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 64f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        val width = 128
        val height = 128
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        bitmap.eraseColor(Color.TRANSPARENT)
        val xPos = width / 2f
        val yPos = (height / 2f - (paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(text, xPos, yPos, paint)

        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0])
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()
        return textures[0]
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glSurfaceViewWidth = width
        glSurfaceViewHeight = height
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        val projectionMatrix = FloatArray(16)
        val viewMatrix = FloatArray(16)
        val modelMatrix = FloatArray(16)
        val mvpMatrix = FloatArray(16)

        val ratio = glSurfaceViewWidth.toFloat() / glSurfaceViewHeight
        Matrix.perspectiveM(projectionMatrix, 0, 45f, ratio, 0.1f, 300f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 80f, 0f, -15f, 0f, 0f, 1f, 0f)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, rotateX, 1f, 0f, 0f)
        Matrix.rotateM(modelMatrix, 0, rotateY, 0f, 1f, 0f)

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        GLES30.glUseProgram(programId)
        GLES30.glUniformMatrix4fv(uMvpLocation, 1, false, mvpMatrix, 0)
        GLES30.glUniform1i(uUseTextureLocation, 0)

        // 1. 绘制立方体线框
        drawCubeWireframe()

        // 2. 绘制刻度线
        drawScales()

        // 3. 绘制刻度值 (数字)
        drawScaleValues(mvpMatrix)

        // 4. 绘制目标点 (红色实点)
        drawTargetPoint()
    }

    private fun drawCubeWireframe() {
        val vertices = floatArrayOf(
            glMinX, glMaxY, glMinZ,  glMaxX, glMaxY, glMinZ,
            glMaxX, glMaxY, glMinZ,  glMaxX, glMaxY, glMaxZ,
            glMaxX, glMaxY, glMaxZ,  glMinX, glMaxY, glMaxZ,
            glMinX, glMaxY, glMaxZ,  glMinX, glMaxY, glMinZ,
            glMinX, glMinY, glMinZ,  glMaxX, glMinY, glMinZ,
            glMaxX, glMinY, glMinZ,  glMaxX, glMinY, glMaxZ,
            glMaxX, glMinY, glMaxZ,  glMinX, glMinY, glMaxZ,
            glMinX, glMinY, glMaxZ,  glMinX, glMinY, glMinZ,
            glMinX, glMaxY, glMinZ,  glMinX, glMinY, glMinZ,
            glMaxX, glMaxY, glMinZ,  glMaxX, glMinY, glMinZ,
            glMaxX, glMaxY, glMaxZ,  glMaxX, glMinY, glMaxZ,
            glMinX, glMaxY, glMaxZ,  glMinX, glMinY, glMaxZ
        )
        GLES30.glUniform4f(uColorLocation, 0.5f, 0.5f, 0.5f, 1f)
        drawLines(vertices)
    }

    private fun drawScales() {
        val scaleLines = mutableListOf<Float>()
        // X轴刻度
        for (x in -10..10) {
            val fx = x.toFloat()
            val len = if (x % 5 == 0) 1.2f else 0.6f
            scaleLines.add(fx); scaleLines.add(glMaxY); scaleLines.add(glMaxZ)
            scaleLines.add(fx); scaleLines.add(glMaxY - len); scaleLines.add(glMaxZ)
        }
        // Y轴刻度
        for (y in 0..30) {
            val fy = -y.toFloat()
            val len = if (y % 5 == 0) 1.2f else 0.6f
            scaleLines.add(glMinX); scaleLines.add(fy); scaleLines.add(glMaxZ)
            scaleLines.add(glMinX + len); scaleLines.add(fy); scaleLines.add(glMaxZ)
        }
        // Z轴刻度
        for (z in -10..10) {
            val fz = -z.toFloat()
            val len = if (z % 5 == 0) 1.2f else 0.6f
            scaleLines.add(glMaxX); scaleLines.add(glMaxY); scaleLines.add(fz)
            scaleLines.add(glMaxX - len); scaleLines.add(glMaxY); scaleLines.add(fz)
        }
        GLES30.glUniform4f(uColorLocation, 0f, 1f, 0f, 1f) // 绿色刻度
        drawLines(scaleLines.toFloatArray())
    }

    private fun drawScaleValues(mvpMatrix: FloatArray) {
        GLES30.glUniform1i(uUseTextureLocation, 1)
        GLES30.glUniform4f(uColorLocation, 1f, 1f, 1f, 1f)
        
        // X轴刻度值
        for (x in -10..10 step 5) {
            drawTextLabel(x.toString(), x.toFloat(), glMaxY + 2f, glMaxZ)
        }
        // Y轴刻度值
        for (y in 0..30 step 5) {
            drawTextLabel(y.toString(), glMinX - 2f, -y.toFloat(), glMaxZ)
        }
        // Z轴刻度值
        for (z in -10..10 step 5) {
            drawTextLabel(z.toString(), glMaxX + 2f, glMaxY + 2f, -z.toFloat())
        }
    }

    private fun drawTextLabel(text: String, x: Float, y: Float, z: Float) {
        val textureId = textTextures[text] ?: return
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        
        val size = 2f
        val vertices = floatArrayOf(
            x - size, y - size, z,
            x + size, y - size, z,
            x - size, y + size, z,
            x + size, y + size, z
        )
        val texCoords = floatArrayOf(
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
        )
        
        val vBuffer = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertices)
        vBuffer.position(0)
        val tBuffer = ByteBuffer.allocateDirect(texCoords.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(texCoords)
        tBuffer.position(0)
        
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, vBuffer)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, tBuffer)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun drawTargetPoint() {
        GLES30.glUniform1i(uUseTextureLocation, 0)
        // 映射坐标: User X -> GL X, User Y -> -GL Y, User Z -> -GL Z
        val glX = targetX
        val glY = -targetY
        val glZ = -targetZ
        
        val vertex = floatArrayOf(glX, glY, glZ)
        val buffer = ByteBuffer.allocateDirect(vertex.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertex)
        buffer.position(0)
        
        GLES30.glUniform4f(uColorLocation, 1f, 0f, 0f, 1f)
        GLES30.glUniform1f(uPointSizeLocation, 25.0f)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, buffer)
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 1)
        
        // 辅助虚线
        val guideLines = floatArrayOf(
            glX, glMaxY, glMaxZ, glX, glY, glMaxZ, // X-Y面投影
            glMinX, glY, glMaxZ, glX, glY, glMaxZ,
            glX, glY, glMaxZ, glX, glY, glZ         // 深度线
        )
        GLES30.glUniform4f(uColorLocation, 1f, 0f, 0f, 0.5f)
        drawLines(guideLines)
    }

    private fun drawLines(vertices: FloatArray) {
        val buffer = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertices)
        buffer.position(0)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, buffer)
        GLES30.glDisableVertexAttribArray(1)
        GLES30.glDrawArrays(GLES30.GL_LINES, 0, vertices.size / 3)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)
        return shader
    }

    override fun onParameterChange(parameterKey: String, parameterValue: Float) {
        when (parameterKey) {
            "rotateX" -> rotateX = parameterValue
            "rotateY" -> rotateY = parameterValue
        }
    }

    override fun onParameterReset() {
        rotateX = 20f
        rotateY = -30f
        targetX = 0f
        targetY = 15f
        targetZ = 0f
    }
}

package ru.fierylynx.old43

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.app.use
import java.util.*

class MainMenuScreen : KtxScreen {


    lateinit var texture1 : Texture
    lateinit var texture2 : Texture
    lateinit var texture3 : Texture
    val start = Texture("start.png")
    val dio = Texture("dio.jpg")
    lateinit var batch: SpriteBatch
    lateinit var viewport: Viewport
    var startTimer = 0f
    var startDelta = 0.5f
    var num = 0

    override fun show() {
        viewport = ScreenViewport()
        batch = SpriteBatch()

        val file = Gdx.files.local("you.txt")
        if (file.exists()) {
            val scanner = Scanner(file.read())
            num = scanner.nextInt()
        }

        if (num >= 3) {
            texture1 = Texture(Gdx.files.local("${num-1}.png"))
            texture2 = Texture(Gdx.files.local("${num-2}.png"))
            texture3 = Texture(Gdx.files.local("${num-3}.png"))
        }
    }

    override fun render(delta: Float) {
        clearScreen(0f, 0f, 0f, 1f)
        startTimer += delta
        batch.use {
            if (startTimer % (startDelta * 2) < startDelta)
                drawTexture(start, 0f, Gdx.graphics.height / 2f, Gdx.graphics.width / 2f, Gdx.graphics.height.toFloat())
            drawTexture(dio, 0f, 0f, Gdx.graphics.width / 2f, Gdx.graphics.height / 2f)

            if (num >= 3) {
                drawTexture(texture1, Gdx.graphics.width / 2f, Gdx.graphics.height * 2 / 3f,
                        Gdx.graphics.width * 3 / 4f, Gdx.graphics.height.toFloat())

                drawTexture(texture2, Gdx.graphics.width * 3 / 4f, Gdx.graphics.height / 3f,
                        Gdx.graphics.width.toFloat(), Gdx.graphics.height * 2 / 3f)

                drawTexture(texture3, Gdx.graphics.width / 2f, 0f,
                        Gdx.graphics.width * 3 / 4f, Gdx.graphics.height / 3f)
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            Main.setScreen(GameScreen(), 0.2f)
        }
    }

    private fun drawTexture(texture: Texture, x1: Float, y1: Float, x2: Float, y2: Float) {
        val s = Math.min((x2 - x1) / texture.width,
                (y2 - y1) / texture.height)
        batch.draw(texture, (x1 + x2) / 2 - texture.width * s / 2, (y1 + y2) / 2 - texture.height * s / 2,
                texture.width * s, texture.height * s)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        batch.projectionMatrix = viewport.camera.combined
    }
}
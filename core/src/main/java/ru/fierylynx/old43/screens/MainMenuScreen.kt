package ru.fierylynx.old43.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.app.use
import ru.fierylynx.old43.Main
import ru.fierylynx.old43.assets.Styles
import java.util.*
import kotlin.collections.ArrayList

class MainMenuScreen : KtxScreen {

    lateinit var texture1 : Texture
    lateinit var texture2 : Texture
    lateinit var texture3 : Texture
    lateinit var stage1 : Stage
    lateinit var stage2 : Stage
    lateinit var stage3 : Stage

    val start = Texture("start.png")
    val dio = Texture("dio.jpg")
    val ctrl = Texture("gamepad.png")

    lateinit var batch: SpriteBatch
    lateinit var viewport: Viewport
    var startTimer = 0f
    var startDelta = 0.5f
    var num = 0

    override fun show() {
        viewport = ScreenViewport()
        batch = SpriteBatch()

        var file = Gdx.files.local("you.txt")
        if (file.exists()) {
            val scanner = Scanner(file.read())
            num = scanner.nextInt()
        }

        file = Gdx.files.local("tries.txt")
        var l = ArrayList<Int>()
        if (file.exists()) {
            val scanner = Scanner(file.read())
            while (scanner.hasNextInt())
                l.add(scanner.nextInt())
        }
        var max = ArrayList<Int>()
        var maxIndex = ArrayList<Int>()
        if (num >= 1) {
            max.add(Int.MIN_VALUE)
            maxIndex.add(0)
            for (i in 0 until l.size) {
                if (max[0] < l[i]) {
                    max[0] = l[i]
                    maxIndex[0] = i
                }
            }
            stage1 = createStage(max[0].toString(), false)
            texture1 = Texture(Gdx.files.local("${maxIndex[0]}.png"))
        }
        if (num >= 2) {
            max.add(Int.MIN_VALUE)
            maxIndex.add(0)
            for (i in 0 until l.size) {
                var a = true
                for (j in 0 until 1)
                    if (maxIndex[j] == i)
                        a = false
                if (max[1] < l[i] && a) {
                    max[1] = l[i]
                    maxIndex[1] = i
                }
            }
            stage2 = createStage(max[1].toString(), true)
            texture2 = Texture(Gdx.files.local("${maxIndex[1]}.png"))
        }
        if (num >= 3) {
            max.add(Int.MIN_VALUE)
            maxIndex.add(0)
            for (i in 0 until l.size) {
                var a = true
                for (j in 0 until 2)
                    if (maxIndex[j] == i)
                        a = false
                if (max[2] < l[i] && a) {
                    max[2] = l[i]
                    maxIndex[2] = i
                }
            }
            stage3 = createStage(max[2].toString(), false)
            texture3 = Texture(Gdx.files.local("${maxIndex[2]}.png"))
        }

        Main.mainMusic.isLooping = true
        Main.mainMusic.play()
    }

    private fun createStage(text: String, right: Boolean): Stage {
        val stage = Stage(ScreenViewport())
        stage.apply {
            addActor(Table().apply {
                center()
                if (right)
                    right()
                else
                    left()
                setFillParent(true)

                add(Label("Score:", Styles.labelWhiteStyle))
                row()
                add(Label(text, Styles.labelWhiteStyle))
            })
            setDebugAll(Main.debug)
        }
        return stage
    }

    override fun render(delta: Float) {
        clearScreen(0f, 0f, 0f, 1f)
        startTimer += delta
        batch.use {
            if (startTimer % (startDelta * 2) < startDelta)
                drawTexture(start, 0f, Gdx.graphics.height / 2f, Gdx.graphics.width / 2f, Gdx.graphics.height.toFloat() * 3 / 4)
            drawTexture(ctrl, 0f, Gdx.graphics.height * 3 / 4f, Gdx.graphics.width / 2f, Gdx.graphics.height.toFloat())

            drawTexture(dio, 0f, 0f, Gdx.graphics.width / 2f, Gdx.graphics.height / 2f)

            if (num >= 1) {
                drawTexture(texture1, Gdx.graphics.width / 2f, Gdx.graphics.height * 2 / 3f,
                        Gdx.graphics.width * 3 / 4f, Gdx.graphics.height.toFloat())
            }
            if (num >= 2) {
                drawTexture(texture2, Gdx.graphics.width * 3 / 4f, Gdx.graphics.height / 3f,
                        Gdx.graphics.width.toFloat(), Gdx.graphics.height * 2 / 3f)
            }
            if (num >= 3) {
                drawTexture(texture3, Gdx.graphics.width / 2f, 0f,
                        Gdx.graphics.width * 3 / 4f, Gdx.graphics.height / 3f)
            }
        }
        if (num >= 1) {
            Gdx.gl.glViewport(Gdx.graphics.width * 3 / 4, Gdx.graphics.height * 2 / 3,
                    Gdx.graphics.width / 4, Gdx.graphics.height / 3)
            stage1.draw()
            Gdx.gl.glViewport(0, 0,
                    Gdx.graphics.width, Gdx.graphics.height)
        }
        if (num >= 2) {
            Gdx.gl.glViewport(Gdx.graphics.width / 2, Gdx.graphics.height / 3,
                    Gdx.graphics.width / 4, Gdx.graphics.height / 3)
            stage2.draw()
            Gdx.gl.glViewport(0, 0,
                    Gdx.graphics.width, Gdx.graphics.height)
        }
        if (num >= 3) {
            Gdx.gl.glViewport(Gdx.graphics.width * 3 / 4, 0,
                    Gdx.graphics.width / 4, Gdx.graphics.height / 3)
            stage3.draw()
            Gdx.gl.glViewport(0, 0,
                    Gdx.graphics.width, Gdx.graphics.height)
        }

        if (Main.controls.start()) {
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
        if (num >= 1)
            stage1.viewport.update(width / 4, height / 3, true)
        if (num >= 2)
            stage2.viewport.update(width / 4, height / 3, true)
        if (num >= 3)
            stage3.viewport.update(width / 4, height / 3, true)
    }

    override fun dispose() {
        super.dispose()
        Main.mainMusic.isLooping = false
        Main.mainMusic.stop()
    }
}
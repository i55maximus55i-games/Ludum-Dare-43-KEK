package ru.fierylynx.old43

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.actors.onClick

class Main(val isDebug : Boolean) : Game() {

    companion object {
        lateinit var instanse : Main
        var debug = false

        fun blink(time: Float) {
            instanse.blink(time)
        }

        fun setScreen(screen: Screen, time: Float) {
            instanse.changeScreen(screen, time)
        }
    }

    lateinit var stage : Stage
    private lateinit var shapeRenderer: ShapeRenderer
    private var blickTimer = 0f
    private var blickTime = 0f
    private var blink = false
    private lateinit var changeScreen : Screen
    private var isChangeScreen = false
    private var changed = false

    lateinit var fpsLabel: Label
    lateinit var deltaLabel: Label
    private val frameTimeLen = 1000
    private var frameTime = ArrayList<Float>(frameTimeLen)

    override fun create() {
        instanse = this
        debug = isDebug
        for (i in 0 until frameTimeLen)
            frameTime.add(0f)

        stage = Stage(ScreenViewport())
        createStage()
        shapeRenderer = ShapeRenderer()
        setScreen(GameScreen())
    }

    private fun createStage() {
        deltaLabel = Label("", Styles.labelStyle)
        fpsLabel = Label("", Styles.labelStyle)
        stage.apply {
            addActor(Table().apply {
                top()
                left()
                setFillParent(true)

                add(Label("FPS  : ", Styles.labelStyle))
                add(fpsLabel)
                row()
                add(Label("Time : ", Styles.labelStyle))
                add(deltaLabel)
            })
        }
    }

    override fun render() {
        super.render()

        if (blink) {
            blickTimer += Gdx.graphics.deltaTime
            if (changed) {
                changed = false
                blickTimer -= Gdx.graphics.deltaTime
            }
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color.BLACK
            if (blickTimer < blickTime) {
                shapeRenderer.rect(0f, 0f,
                        Gdx.graphics.width.toFloat(),Gdx.graphics.height.toFloat() * 5 / 8 * blickTimer / blickTime)
                shapeRenderer.rect(0f, Gdx.graphics.height.toFloat(),
                        Gdx.graphics.width.toFloat(), - Gdx.graphics.height.toFloat() * 5 / 8 * blickTimer / blickTime)
            }
            else {
                shapeRenderer.rect(0f, 0f,
                        Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat() * 5 / 8 * (1 - ((blickTimer - blickTime) / blickTime)))
                shapeRenderer.rect(0f, Gdx.graphics.height.toFloat(),
                        Gdx.graphics.width.toFloat(), -Gdx.graphics.height.toFloat() * 5 / 8 * (1 - ((blickTimer - blickTime) / blickTime)))
            }
            shapeRenderer.end()
            if (blickTimer > blickTime && isChangeScreen) {
                screen = changeScreen
                screen.show()
                screen.resize(Gdx.graphics.width, Gdx.graphics.height)
                isChangeScreen = false
                changed = true
            }
            if (blickTimer > blickTime * 2)
                blink = false
        }
        if (debug) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color.RED
            shapeRenderer.rect(0f, Gdx.graphics.height.toFloat(), 208f + frameTimeLen, -72f)
            shapeRenderer.end()

            frameTime.removeAt(frameTime.lastIndex)
            frameTime.add(0, Gdx.graphics.deltaTime * 1000)
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.color = Color.BLACK
            for (i in 0 until frameTimeLen - 1) {
                shapeRenderer.line(Vector2(208f + i, Gdx.graphics.height - 72f + frameTime[i]), Vector2(208f + i + 1, Gdx.graphics.height - 72f + frameTime[i+1]))
            }
            shapeRenderer.end()

            deltaLabel.setText("%.3f".format(Gdx.graphics.deltaTime))
            fpsLabel.setText(Gdx.graphics.framesPerSecond.toString())
            stage.draw()
        }
    }

    fun changeScreen(screen: Screen, time: Float) {
        changeScreen = screen
        isChangeScreen = true
        blink(time)
    }

    fun blink(time: Float) {
        blickTimer = 0f
        blickTime = time
        blink = true
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        shapeRenderer.dispose()
        shapeRenderer = ShapeRenderer()
        stage.viewport.update(width, height, true)
    }
}
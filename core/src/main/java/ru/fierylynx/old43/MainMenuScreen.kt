package ru.fierylynx.old43

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.app.KtxScreen
import ktx.app.clearScreen
import com.github.sarxos.webcam.Webcam
import com.github.sarxos.webcam.WebcamResolution
import com.github.sarxos.webcam.WebcamUtils
import com.github.sarxos.webcam.util.ImageUtils
import ktx.actors.onClick
import ktx.app.use
import java.awt.Dimension

class MainMenuScreen : KtxScreen {


    lateinit var texture : Texture
    var start = Texture("start.png")
    lateinit var batch: SpriteBatch
    lateinit var viewport: Viewport

    override fun show() {
        viewport = ScreenViewport()
        val nonStandardResolutions = arrayOf<Dimension>(
                WebcamResolution.PAL.size,
                WebcamResolution.HD.size,
                WebcamResolution.SXGA.size,
                WebcamResolution.HVGA.size
        )

        val webcam = Webcam.getDefault()
        webcam.setCustomViewSizes(*nonStandardResolutions)
        webcam.viewSize = WebcamResolution.HVGA.size
        webcam.open()
        WebcamUtils.capture(webcam, "test1", ImageUtils.FORMAT_PNG)
        webcam.close()

        batch = SpriteBatch()
        texture = Texture(Gdx.files.local("test1.png"))
    }

    override fun render(delta: Float) {
        clearScreen(1f, 1f, 1f, 1f)
        batch.use {
            var aspectRatio = start.width.toFloat() / start.height.toFloat()
            batch.draw(start, 0f, Gdx.graphics.height.toFloat() * 3 / 4, Gdx.graphics.width.toFloat() / 2, Gdx.graphics.width.toFloat() / 2 / aspectRatio)
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER))
            Main.setScreen(GameScreen(), 0.2f)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        batch.projectionMatrix = viewport.camera.combined
    }

}
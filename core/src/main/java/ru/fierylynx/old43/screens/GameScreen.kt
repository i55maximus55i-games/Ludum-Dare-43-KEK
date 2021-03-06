package ru.fierylynx.old43.screens

import box2dLight.PointLight
import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.sarxos.webcam.Webcam
import com.github.sarxos.webcam.WebcamResolution
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.app.use
import ru.fierylynx.old43.ContactHandler
import ru.fierylynx.old43.Main
import ru.fierylynx.old43.assets.Styles
import ru.fierylynx.old43.objects.Enemy
import ru.fierylynx.old43.objects.Player
import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class GameScreen : KtxScreen {
    private val scale = 16f

    private lateinit var map: TiledMap
    private lateinit var mapRenderer: OrthogonalTiledMapRenderer

    private lateinit var viewport: Viewport
    private lateinit var camera: OrthographicCamera
    private lateinit var batch: SpriteBatch

    private lateinit var world: World
    private lateinit var debugRenderer: Box2DDebugRenderer
    private lateinit var rayHandler: RayHandler

    private lateinit var player: Player
    private var deathTimer = 0f
    var deathlight = false
    lateinit var deathLight: PointLight
    lateinit var deathMusic: Music
    lateinit var pushkaSound: Music

    var num = 0
    var score = 0
    var scoreTimer = 4f
    var timerSpawnEnemy = 10f
    private var gameoverTimer = 60f
    lateinit var img : BufferedImage
    lateinit var light: PointLight

    var enemies = ArrayList<Enemy>()
    private lateinit var labelLives: Label
    private lateinit var labelScore: Label
    private lateinit var labelTimer: Label
    private var fuck = true

    private var stage = Stage(ScreenViewport())

    var parallaxTexture1 = Texture("BG.png")
    var parallaxTexture2 = Texture("BG1.png")
    var parallaxTexture3 = Texture("BG2.png")
    var parallaxTexture4 = Texture("BG3.png")


    override fun show() {
        map = TmxMapLoader().load("map.tmx")
        mapRenderer = OrthogonalTiledMapRenderer(map)

        world = World(Vector2(0f, -9.8f), true).apply {
            setContactListener(ContactHandler())
        }
        debugRenderer = Box2DDebugRenderer()
        rayHandler = RayHandler(world).apply {
            setAmbientLight(0.25f)
        }

        createWalls()
        player = Player(world, Vector2(16f, 32f), scale)
        light = PointLight(rayHandler, 1000).apply {
            attachToBody(player.body)
            distance = 6f
        }
        deathMusic = Gdx.audio.newMusic(Gdx.files.internal("titanic.mp3"))
        pushkaSound = Gdx.audio.newMusic(Gdx.files.internal("pushka.mp3"))

        camera = OrthographicCamera()
        camera.setToOrtho(false)
        batch = SpriteBatch()
        viewport = ScreenViewport(camera)

        val file = Gdx.files.local("you.txt")
        if (file.exists()) {
            val scanner = Scanner(file.read())
            num = scanner.nextInt()
        }

        stage.apply {
            addActor(Table().apply {
                setFillParent(true)
                top()
                right()
                labelLives = Label("", Styles.labelWhiteStyle)
                labelScore = Label("", Styles.labelWhiteStyle)
                labelTimer = Label("", Styles.labelWhiteStyle)
                add(labelLives)
                row()
                add(labelScore)
                row()
                add(labelTimer)
            })
        }

        val nonStandardResolutions = arrayOf<Dimension>(
                WebcamResolution.PAL.size,
                WebcamResolution.HD.size,
                WebcamResolution.SXGA.size,
                WebcamResolution.HVGA.size
        )
        val webcam = Webcam.getDefault()
        webcam.setCustomViewSizes(*nonStandardResolutions)
        webcam.viewSize = WebcamResolution.HD.size
        webcam.open()
        img = webcam.image
        webcam.close()

        Main.gameMusic.isLooping = true
        Main.gameMusic.play()
        Main.gameMusic.volume = 0.25f
    }

    private fun createWalls() {
        val bDef = BodyDef()
        val shape = PolygonShape()
        val fDef = FixtureDef()
        var body: Body

        for (i in map.layers.get("walls").objects.getByType(RectangleMapObject::class.java)) {
            val rect = i.rectangle
            bDef.type = BodyDef.BodyType.StaticBody
            bDef.position.set((rect.getX() + rect.getWidth() / 2) / scale, (rect.getY() + rect.getHeight() / 2) / scale)

            body = world.createBody(bDef)

            shape.setAsBox(rect.getWidth() / 2 / scale, rect.getHeight() / 2 / scale)
            fDef.shape = shape
            fDef.friction = 0f
            fDef.restitution = 0f
            fDef.density = 1f
            body.createFixture(fDef)
            body.userData = "wall"
        }
    }

    override fun render(delta: Float) {
        timerSpawnEnemy += delta
        gameoverTimer -= delta
        score += player.update(delta, enemies)
        scoreTimer -= delta
        if (scoreTimer < 0) {
            score += 30
            scoreTimer = 2f
        }
        world.step(delta, 10, 10)

        if (timerSpawnEnemy > 15f) {
            timerSpawnEnemy = 0f
            for (i in map.layers.get("enemies").objects.getByType(RectangleMapObject::class.java)) {
                val rect = i.rectangle
                enemies.add(Enemy(world, Vector2(rect.x, rect.y), scale))
            }
        }
        for (i in enemies)
            score += i.update(delta, player)

        if (player.alive) {
            val camTarget = player.body.position.scl(scale)
            val camMove = camTarget.sub(Vector2(camera.position.x, camera.position.y)).scl(0.1f)
            camera.position.x += camMove.x
            camera.position.y += camMove.y
        }
        else {
            if (!deathlight) {
                deathlight = true
                light.remove()
                deathLight = PointLight(rayHandler, 1000).apply {
                    attachToBody(player.bodies["head"])
                    distance = 4f
                }
                Main.gameMusic.isLooping = false
                Main.gameMusic.stop()
                if (fuck)
                    deathMusic.play()
            }
            val camTarget = player.bodies["head"]!!.position.scl(scale)
            val camMove = camTarget.sub(Vector2(camera.position.x, camera.position.y)).scl(0.3f)
            camera.position.x += camMove.x
            camera.position.y += camMove.y
            camera.zoom -= 0.2f * delta

            deathTimer += delta
            if (deathTimer >= 4f) {
                deathTimer = -10f
                Main.setScreen(MainMenuScreen(), 0.2f)
            }
        }
        if (Main.controls.start() && player.alive)
            player.death()
        if (gameoverTimer < 0f && fuck && player.alive) {
            fuck = false
            pushkaSound.volume = 0.5f
            pushkaSound.play()
        }
        if (gameoverTimer < -2.6f && player.alive)
            player.death()
        camera.update()

        batch.projectionMatrix = camera.combined
        mapRenderer.setView(camera)

        clearScreen(45f / 255f, 32f / 255f, 92f / 255f, 1f)

        //parallax
        apply {
            camera.zoom /= 2f
            camera.position.x /= 3f
            camera.position.y /= 3f
            camera.update()
            batch.projectionMatrix = camera.combined
            batch.use {
                for (i in -1 until 6) {
                    batch.draw(parallaxTexture1, parallaxTexture1.width.toFloat() * i, 0f)
                }
            }
            camera.zoom *= 2f
            camera.position.x *= 3f
            camera.position.y *= 3f
            camera.update()
            batch.projectionMatrix = camera.combined
            mapRenderer.setView(camera)
        }
        apply {
            camera.zoom /= 2f
            camera.position.x /= 2.7f
            camera.position.y /= 2.7f
            camera.update()
            batch.projectionMatrix = camera.combined
            batch.use {
                for (i in -1 until 6) {
                    batch.draw(parallaxTexture2, parallaxTexture1.width.toFloat() * i, 0f)
                }
            }
            camera.zoom *= 2f
            camera.position.x *= 2.7f
            camera.position.y *= 2.7f
            camera.update()
            batch.projectionMatrix = camera.combined
            mapRenderer.setView(camera)
        }
        apply {
            camera.zoom /= 2f
            camera.position.x /= 2.5f
            camera.position.y /= 2.5f
            camera.update()
            batch.projectionMatrix = camera.combined
            batch.use {
                for (i in -1 until 6) {
                    batch.draw(parallaxTexture3, parallaxTexture1.width.toFloat() * i, 0f)
                }
            }
            camera.zoom *= 2f
            camera.position.x *= 2.5f
            camera.position.y *= 2.5f
            camera.update()
            batch.projectionMatrix = camera.combined
            mapRenderer.setView(camera)
        }
        apply {
            camera.zoom /= 2f
            camera.position.x /= 2.2f
            camera.position.y /= 2.2f
            camera.update()
            batch.projectionMatrix = camera.combined
            batch.use {
                for (i in -1 until 6) {
                    batch.draw(parallaxTexture4, parallaxTexture1.width.toFloat() * i, 0f)
                }
            }
            camera.zoom *= 2f
            camera.position.x *= 2.2f
            camera.position.y *= 2.2f
            camera.update()
            batch.projectionMatrix = camera.combined
            mapRenderer.setView(camera)
        }

        mapRenderer.render()
        batch.use {
            for (i in enemies)
                i.draw(batch)
            player.draw(batch)
        }
        camera.zoom /= scale
        camera.position.x /= scale
        camera.position.y /= scale
        camera.update()
        rayHandler.setCombinedMatrix(camera)
        rayHandler.updateAndRender()
        camera.zoom *= scale
        camera.position.x *= scale
        camera.position.y *= scale

        if (score < 0)
            score = 0
        if (player.alive) {
            labelLives.setText("Lives: ${player.lives}")
            labelScore.setText("Score: $score")
            labelTimer.setText("Time: ${gameoverTimer.toInt()}")
            stage.act()
            stage.draw()
        }

        if (Main.debug) {
            camera.zoom /= scale
            camera.position.x /= scale
            camera.position.y /= scale
            camera.update()
            debugRenderer.render(world, camera.combined)
            camera.zoom *= scale
            camera.position.x *= scale
            camera.position.y *= scale
        }
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        val scale = 196f

        val s = 2
        rayHandler.resizeFBO(width * s, height * s)

        viewport.update(width, height)
        camera.viewportWidth = scale * width / height
        camera.viewportHeight = scale

        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        ImageIO.write(img, "png", File("$num.png"))
        num++
        var file = Gdx.files.local("you.txt")
        file.writeString(num.toString(), false)

        file = Gdx.files.local("tries.txt")
        file.writeString("$score\n",true)

        map.dispose()
        mapRenderer.dispose()
        batch.dispose()
        world.dispose()
        debugRenderer.dispose()
        rayHandler.dispose()
        player.dispose()
        deathMusic.dispose()
    }

}
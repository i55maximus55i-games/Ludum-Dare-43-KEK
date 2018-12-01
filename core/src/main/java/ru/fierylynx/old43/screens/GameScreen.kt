package ru.fierylynx.old43.screens

import box2dLight.PointLight
import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.OrthographicCamera
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

    var num = 0
    var score = 0
    var timerSpawnEnemy = 5f
    lateinit var img : BufferedImage
    lateinit var light: PointLight

    var enemies = ArrayList<Enemy>()
    lateinit var label: Label

    private var stage = Stage(ScreenViewport())

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
                label = Label("", Styles.labelWhiteStyle)
                add(label)
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
        player.update(delta, enemies)
        world.step(delta, 10, 10)

        if (timerSpawnEnemy > 5f) {
            timerSpawnEnemy = 0f
            for (i in map.layers.get("enemies").objects.getByType(RectangleMapObject::class.java)) {
                val rect = i.rectangle
                enemies.add(Enemy(world, Vector2(rect.x, rect.y), scale))
            }
        }
        for (i in enemies)
            i.update(delta, player)

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
        camera.update()

        batch.projectionMatrix = camera.combined
        mapRenderer.setView(camera)

        clearScreen(0f, 0.84f, 1f, 1f)
        mapRenderer.render()
        batch.use {
            player.draw(batch)
            for (i in enemies)
                i.draw(batch)
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

        if (player.alive) {
            label.setText("Lives: ${player.lives}")
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
package ru.fierylynx.old43

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.app.use

class GameScreen : KtxScreen {
    private val scale = 16f

    private lateinit var map: TiledMap
    private lateinit var mapRenderer: OrthogonalTiledMapRenderer

    private lateinit var viewport: Viewport
    private lateinit var camera: OrthographicCamera
    private lateinit var batch: SpriteBatch

    private lateinit var world: World
    private lateinit var debugRenderer: Box2DDebugRenderer

    private lateinit var player: Player

    override fun show() {
        map = TmxMapLoader().load("map.tmx")
        mapRenderer = OrthogonalTiledMapRenderer(map)

        world = World(Vector2(0f, -9.8f), true)
        debugRenderer = Box2DDebugRenderer()
        createWalls()
        player = Player(world, Vector2(16f, 32f), scale)

        camera = OrthographicCamera()
        camera.setToOrtho(false)
        batch = SpriteBatch()
        viewport = ScreenViewport(camera)
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
            body.createFixture(fDef)
            body.userData = "wall"
        }
    }

    override fun render(delta: Float) {
        player.update(delta)
        world.step(delta, 10, 10)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            Main.setScreen(MainMenuScreen(), 0.2f)


        val camTarget = player.body.position.scl(scale)
        val camMove = camTarget.sub(Vector2(camera.position.x, camera.position.y)).scl(0.1f)
        camera.position.x += camMove.x
        camera.position.y += camMove.y
        camera.update()

        batch.projectionMatrix = camera.combined
        mapRenderer.setView(camera)


        clearScreen(0f, 0.84f, 1f, 1f)
        mapRenderer.render()
        batch.use {
            player.draw(batch)
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

        viewport.update(width, height)
        camera.viewportWidth = scale * width / height
        camera.viewportHeight = scale
    }
}
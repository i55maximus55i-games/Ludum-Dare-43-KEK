package ru.fierylynx.old43

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.Disposable

class Player(world: World, position: Vector2, private val scale: Float) : Disposable{

    var body: Body

    private val jumpMaxCount = 2
    private var jump = 0
    private var jumpTimer = 0f
    private var jumpMaxTime = 0.3f
    private var stand = false
    private var jumping = false
    private var jumpUnpressed = true

    private val width = 12
    private val height = 28

    private val anim = Anim("player")

    private var timer = 0f

    init {
        val bDef = BodyDef()
        val shape = PolygonShape()
        val fDef = FixtureDef()

        bDef.type = BodyDef.BodyType.DynamicBody
        bDef.position.set((position.x + width / 2) / scale, (position.y + width / 2) / scale)

        body = world.createBody(bDef)

        shape.setAsBox(width / 2 / scale, height / 2 / scale)
        fDef.shape = shape
        fDef.friction = 0f
        body.createFixture(fDef)
        body.userData = "player"
    }

    fun update(delta: Float) {
        var x = 0f
        if (Gdx.input.isKeyPressed(Input.Keys.A))
            x -= 1
        if (Gdx.input.isKeyPressed(Input.Keys.D))
            x += 1
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))
            body.setLinearVelocity(x * 12, body.linearVelocity.y)
        else
            body.setLinearVelocity(x * 5, body.linearVelocity.y)

        if (Gdx.input.isKeyPressed(Input.Keys.W) && jump < jumpMaxCount && !jumping && jumpUnpressed) {
            jumping = true
            jumpTimer = 0f
            jump++
            jumpUnpressed = false
        }
        if (jumping && Gdx.input.isKeyPressed(Input.Keys.W) && jumpTimer < jumpMaxTime) {
            jumpTimer += delta
            body.setLinearVelocity(body.linearVelocity.x, 7f)
        }
        else
            jumping = false
        if (!jumpUnpressed && !Gdx.input.isKeyPressed(Input.Keys.W))
            jumpUnpressed = true

        if (body.linearVelocity.y == 0f && stand)
            jump = 0
        if (body.linearVelocity.y != 0f && jump == 0)
            jump++
        stand = body.linearVelocity.y == 0f

        timer += delta
    }

    fun draw(batch: SpriteBatch) {
        anim.draw(body.position.x * scale - width / 2, body.position.y * scale - height / 2, "lol", timer, batch, 1f)
    }

    override fun dispose() {
        anim.dispose()
    }
}
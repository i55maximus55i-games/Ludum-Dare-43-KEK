package ru.fierylynx.old43.objects

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.Disposable
import ru.fierylynx.old43.Main
import ru.fierylynx.old43.assets.Anim

class Player(world: World, position: Vector2, private val scale: Float) : Disposable {

    var body: Body

    private val jumpMaxCount = 2
    private var jump = 0
    private var jumpTimer = 0f
    private var jumpMaxTime = 0.3f
    private var stand = false
    private var jumping = false
    private var jumpUnpressed = true

    var alive = true
    var bodies = HashMap<String, Body>()

    private val width = 16
    private val height = 32

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
        fDef.restitution = 0f
        body.createFixture(fDef)
        body.userData = "player"
    }

    fun update(delta: Float) {
        if (alive) {
            val x = Main.controls.playerMove()
            if (Main.controls.playerRun())
                body.setLinearVelocity(x * 12, body.linearVelocity.y)
            else
                body.setLinearVelocity(x * 5, body.linearVelocity.y)

            if (Main.controls.playerJump()) {
                if (jump < jumpMaxCount && !jumping && jumpUnpressed) {
                    jumping = true
                    jumpTimer = 0f
                    jump++
                    jumpUnpressed = false
                }
                if (jumping && jumpTimer < jumpMaxTime) {
                    jumpTimer += delta
                    body.setLinearVelocity(body.linearVelocity.x, 7f)
                } else
                    jumping = false
            }
            else if (!jumpUnpressed)
                jumpUnpressed = true

            if (body.linearVelocity.y == 0f && stand)
                jump = 0
            if (body.linearVelocity.y != 0f && jump == 0)
                jump++
            stand = body.linearVelocity.y == 0f

            timer += delta
        }
    }

    fun death() {
        alive = false
        val timer = timer % anim.animations["lol"]!!.times.last()
        var index = 0
        for (i in anim.animations["lol"]!!.times) {
            if (anim.animations["lol"]!!.times[index] < timer)
                index++
        }
        val a = if (index == 0)
            anim.animations["lol"]!!.times[index]
        else
            anim.animations["lol"]!!.times[index - 1]
        val b = anim.animations["lol"]!!.times[index]
        for (bone in anim.bonesNames) {
            val a1 = anim.animations["lol"]!!.states[a]!![bone]!!.angle
            val a2 = anim.animations["lol"]!!.states[b]!![bone]!!.angle
            val b1 = (a2 - a1)
            val b2 = if (Math.abs((a2 - a1) - 360) < Math.abs(360 - (a1 - a2)))
                (a2 - a1) - 360
            else
                360 - (a1 - a2)

            var angle: Float
            angle = if (Math.abs(b1) < Math.abs(b2)) {
                b1 * (timer - a) / (b - a)
            } else {
                b2 * (timer - a) / (b - a)
            }

            val bDef = BodyDef()
            val shape = PolygonShape()
            val fDef = FixtureDef()

            val position = body.position

            bDef.type = BodyDef.BodyType.DynamicBody
            bDef.position.set(
                    position.x - width / 2 / scale + anim.bones[bone]!!.width / 2 / scale +
                            anim.animations["lol"]!!.states[a]!![bone]!!.x / scale + (anim.animations["lol"]!!.states[b]!![bone]!!.x / scale - anim.animations["lol"]!!.states[a]!![bone]!!.x / scale) * (timer - a) / (b - a),
                    position.y - height / 2 / scale + anim.bones[bone]!!.height / 2 / scale +
                            anim.animations["lol"]!!.states[a]!![bone]!!.y / scale + (anim.animations["lol"]!!.states[b]!![bone]!!.y / scale - anim.animations["lol"]!!.states[a]!![bone]!!.y / scale) * (timer - a) / (b - a)
            )
            bDef.angle = (anim.animations["lol"]!!.states[a]!![bone]!!.angle + angle) * MathUtils.degreesToRadians
            bodies[bone] = body.world.createBody(bDef)

            shape.setAsBox(anim.bones[bone]!!.width / 2 / scale, anim.bones[bone]!!.height / 2 / scale)
            fDef.shape = shape
            fDef.friction = 0f
            fDef.density = 1f
            fDef.restitution = 0.4f
            bodies[bone]!!.createFixture(fDef)
            bodies[bone]!!.userData = "playerBone"
            bodies[bone]!!.linearVelocity = body.linearVelocity
        }
        body.world.destroyBody(body)
    }

    fun draw(batch: SpriteBatch) {
        if (alive)
            anim.draw(body.position.x * scale - width / 2, body.position.y * scale - height / 2, "lol", timer, batch, 1f)
        else {
            for (bone in anim.bonesNames) {
                batch.draw(anim.bones[bone]!!.textureRegion, bodies[bone]!!.position.x * scale - anim.bones[bone]!!.width / 2, bodies[bone]!!.position.y * scale - anim.bones[bone]!!.height / 2,
                        anim.bones[bone]!!.width / 2, anim.bones[bone]!!.height / 2,
                        anim.bones[bone]!!.width, anim.bones[bone]!!.height,
                        1f, 1f,
                        bodies[bone]!!.angle * MathUtils.radiansToDegrees)
            }
        }
    }

    override fun dispose() {
        anim.dispose()
    }
}
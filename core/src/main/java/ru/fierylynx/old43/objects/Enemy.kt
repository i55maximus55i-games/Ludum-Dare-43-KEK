package ru.fierylynx.old43.objects

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.Disposable
import ru.fierylynx.old43.Main
import ru.fierylynx.old43.assets.Anim

class Enemy(world: World, position: Vector2, private val scale: Float) : Disposable {

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

    var goback = 0f
    var lives = 3

    private val width = 16
    private val height = 32
    private val assetScale = 32f

    private val anim = Anim("enemy")
    private var selectedAnim = "skibidi"

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
        body.userData = "enemy"
    }

    fun update(delta: Float, player: Player) {
        if (alive) {
            val mustJump = if (goback < 0)
                Math.abs(body.linearVelocity.x) < 4f
            else
                Math.abs(body.linearVelocity.x) < 15f

            if (mustJump && jump < jumpMaxCount && !jumping && jumpUnpressed) {
                jumping = true
                jumpTimer = 0f
                jump++
                jumpUnpressed = false
            }
            if (mustJump && jumping && jumpTimer < jumpMaxTime) {
                jumpTimer += delta
                body.setLinearVelocity(body.linearVelocity.x, 7f)
            } else
                jumping = false
            if (!mustJump && !jumpUnpressed)
                jumpUnpressed = true

            if (body.linearVelocity.y == 0f && stand)
                jump = 0
            if (body.linearVelocity.y != 0f && jump == 0)
                jump++
            stand = body.linearVelocity.y == 0f

            if (goback >= 0f) {
                if (body.position.x > player.body.position.x) {
                    body.setLinearVelocity(16f, body.linearVelocity.y)
                }
                else {
                    body.setLinearVelocity(-16f, body.linearVelocity.y)
                }
            }
            else {
                if (body.position.x > player.body.position.x) {
                    body.setLinearVelocity(-5f, body.linearVelocity.y)
                }
                else {
                    body.setLinearVelocity(5f, body.linearVelocity.y)
                }
                if (player.body.position.dst(body.position) < 2f) {
                    player.lives--
                    goback = 1f
                }
            }
            if (body.linearVelocity.x >= 0) {
                if (goback >= 0 && selectedAnim != "run_right") {
                    selectedAnim = "run_right"
                }
                if (goback < 0 && selectedAnim != "walk_right") {
                    selectedAnim = "walk_right"
                }
            }
            else {
                if (goback >= 0 && selectedAnim != "run_left") {
                    selectedAnim = "run_left"
                }
                if (goback < 0 && selectedAnim != "walk_left") {
                    selectedAnim = "walk_left"
                }
            }
            if (!player.alive) {
                body.linearVelocity = Vector2()
                selectedAnim = "skibidi"
            }

            timer += delta
            goback -= delta

            if (lives <= 0)
                death()
        }
    }

    fun death() {
        alive = false
        val timer = timer % anim.animations[selectedAnim]!!.times.last()
        var index = 0
        for (i in anim.animations[selectedAnim]!!.times) {
            if (anim.animations[selectedAnim]!!.times[index] < timer)
                index++
        }
        val a = if (index == 0)
            anim.animations[selectedAnim]!!.times[index]
        else
            anim.animations[selectedAnim]!!.times[index - 1]
        val b = anim.animations[selectedAnim]!!.times[index]
        for (bone in anim.bonesNames) {
            val a1 = anim.animations[selectedAnim]!!.states[a]!![bone]!!.angle
            val a2 = anim.animations[selectedAnim]!!.states[b]!![bone]!!.angle
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
                    position.x - width / 2 / scale + anim.bones[bone]!!.width / 2 / scale / assetScale +
                            anim.animations[selectedAnim]!!.states[a]!![bone]!!.x / scale / assetScale + (anim.animations[selectedAnim]!!.states[b]!![bone]!!.x / scale / assetScale - anim.animations[selectedAnim]!!.states[a]!![bone]!!.x / scale / assetScale) * (timer - a) / (b - a),
                    position.y - height / 2 / scale + anim.bones[bone]!!.height / 2 / scale / assetScale +
                            anim.animations[selectedAnim]!!.states[a]!![bone]!!.y / scale / assetScale + (anim.animations[selectedAnim]!!.states[b]!![bone]!!.y / scale / assetScale - anim.animations[selectedAnim]!!.states[a]!![bone]!!.y / scale / assetScale) * (timer - a) / (b - a)
            )
            bDef.angle = (anim.animations[selectedAnim]!!.states[a]!![bone]!!.angle + angle) * MathUtils.degreesToRadians
            bodies[bone] = body.world.createBody(bDef)

            shape.setAsBox(anim.bones[bone]!!.width / 2 / scale / assetScale, anim.bones[bone]!!.height / 2 / scale / assetScale)
            fDef.shape = shape
            fDef.friction = 0f
            fDef.density = 1f
            fDef.restitution = 0.6f
            bodies[bone]!!.createFixture(fDef)
            bodies[bone]!!.userData = "death"
            bodies[bone]!!.linearVelocity = body.linearVelocity
        }
        body.world.destroyBody(body)
    }

    fun draw(batch: SpriteBatch) {
        if (alive)
            anim.draw(body.position.x * scale - width / 2, body.position.y * scale - height / 2, selectedAnim, timer, batch, 1f / assetScale)
        else {
            for (bone in anim.bonesNames) {
                batch.draw(anim.bones[bone]!!.textureRegion, bodies[bone]!!.position.x * scale - anim.bones[bone]!!.width / 2  / assetScale, bodies[bone]!!.position.y * scale - anim.bones[bone]!!.height / 2 / assetScale,
                        anim.bones[bone]!!.width / 2 / assetScale, anim.bones[bone]!!.height / 2 / assetScale,
                        anim.bones[bone]!!.width / assetScale, anim.bones[bone]!!.height / assetScale,
                        1f, 1f,
                        bodies[bone]!!.angle * MathUtils.radiansToDegrees)
            }
        }
    }

    override fun dispose() {
        anim.dispose()
    }
}
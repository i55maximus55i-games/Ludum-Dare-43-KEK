package ru.fierylynx.old43.assets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Disposable
import java.util.*

class Anim(val name: String) : Disposable {

    var skin = Skin()
    var textureAtlas = TextureAtlas(Gdx.files.local("$name.pack"))

    var animations = HashMap<String, Animation>()
    var bones = HashMap<String, Bone>()
    var bonesNames = ArrayList<String>()

    class Animation {
        val times = ArrayList<Float>()
        val states = HashMap<Float, HashMap<String, BoneAnim>>()
    }

    init {
        skin.addRegions(textureAtlas)
        load()
    }

    class Bone {
        lateinit var textureRegion : TextureRegion
        var width = 1f
        var height = 1f
    }

    class BoneAnim {
        var x = 0f
        var y = 0f
        var angle = 0f
    }

    fun draw(x0: Float, y0: Float, anim: String, time: Float, batch: SpriteBatch, scale: Float) {
        val timer = time % animations[anim]!!.times.last()
        var index = 0
        for (i in animations[anim]!!.times) {
            if (animations[anim]!!.times[index] < timer)
                index++
        }
        val a = if (index == 0)
            animations[anim]!!.times[index]
        else
            animations[anim]!!.times[index - 1]
        val b = animations[anim]!!.times[index]
        for (bone in bonesNames) {
            val a1 = animations[anim]!!.states[a]!![bone]!!.angle
            val a2 = animations[anim]!!.states[b]!![bone]!!.angle
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
            batch.draw(bones[bone]!!.textureRegion,
                    x0 + animations[anim]!!.states[a]!![bone]!!.x * scale + (animations[anim]!!.states[b]!![bone]!!.x * scale - animations[anim]!!.states[a]!![bone]!!.x * scale) * (timer - a) / (b - a),
                    y0 + animations[anim]!!.states[a]!![bone]!!.y * scale + (animations[anim]!!.states[b]!![bone]!!.y * scale - animations[anim]!!.states[a]!![bone]!!.y * scale) * (timer - a) / (b - a),
                    bones[bone]!!.width * scale / 2,
                    bones[bone]!!.height * scale / 2,
                    bones[bone]!!.width * scale,
                    bones[bone]!!.height * scale,
                    1f, 1f,
                    animations[anim]!!.states[a]!![bone]!!.angle + angle)
        }
    }

    private fun load() {
        val file = Gdx.files.internal("$name.anim")
        val input = Scanner(file.read())
        while (input.hasNext()) {
            val x = input.next()
            if (x == "bones") {
                input.next()
                var bon = input.next()
                while (bon != "}") {
                    bonesNames.add(bon)
                    bones[bon] = Bone()
                    bones[bon]!!.textureRegion = skin.getRegion(bon)
                    bones[bon]!!.width = bones[bon]!!.textureRegion.regionWidth.toFloat()
                    bones[bon]!!.height = bones[bon]!!.textureRegion.regionHeight.toFloat()
                    bon = input.next()
                }
            }
            else {
                animations[x] = Animation()
                input.next()
                var tm = input.next()
                while (tm != "}") {
                    input.next()
                    animations[x]!!.times.add(tm.toFloat())
                    animations[x]!!.states[tm.toFloat()] = HashMap()
                    var bn = input.next()
                    while (bn != "}") {
                        animations[x]!!.states[tm.toFloat()]!![bn] = BoneAnim()
                        animations[x]!!.states[tm.toFloat()]!![bn]!!.x = input.next().toFloat()
                        animations[x]!!.states[tm.toFloat()]!![bn]!!.y = input.next().toFloat()
                        animations[x]!!.states[tm.toFloat()]!![bn]!!.angle = input.next().toFloat()
                        bn = input.next()
                    }
                    tm = input.next()
                }
            }
        }
    }

    override fun dispose() {
        skin.dispose()
        textureAtlas.dispose()
    }
}
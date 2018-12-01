package ru.fierylynx.old43

import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold

class ContactHandler : ContactListener {
    override fun endContact(contact: Contact?) {

    }

    override fun beginContact(contact: Contact?) {

    }

    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {
        if (contact!!.fixtureA.body.userData == "death" && contact!!.fixtureB.body.userData != "wall") {
            contact.isEnabled = false
        }
        if (contact!!.fixtureB.body.userData == "death" && contact!!.fixtureA.body.userData != "wall") {
            contact.isEnabled = false
        }
        if (contact!!.fixtureB.body.userData == "death" && contact!!.fixtureA.body.userData == "death") {
            contact.isEnabled = true
        }
    }

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {

    }

}
package body

import java.awt.Graphics

class BodyManager {
    var globalTime = 0
    var bodyArray = arrayListOf<Body>()

    fun tick() {
        for(body in bodyArray) {
            for(other in bodyArray)
                if (body!=other) {
                    body.applyForce(other)
                    body.shouldMove(other)
                    body.isMouseAboveMe()
                }
                body.tick()
        }

    }

    fun render(g: Graphics) {
        for(body in bodyArray) {
            body.render(g)
        }
    }
}
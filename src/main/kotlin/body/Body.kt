package body

import Screen.bodyManager
import Screen.mouseInsideScreenPosX
import Screen.mouseInsideScreenPosY
import java.awt.Color
import java.awt.Graphics
import java.awt.Image
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.pow

class Body(
    namePlanet: String,
    colourPlanet: String,
    radioBody: Int,
    var posX: Long,
    var posY: Long,
    private var bodyMass: Int,
    var resultantVelX: Double = 0.0,
    var resultantVelY: Double = 0.0,
    private var shouldMove: Boolean = true,
    private var isMovable: Boolean = true,
    Image: Image
)
{
    private var oldPosX: Long = 0L
    private var oldPosY: Long = 0L

    var deltaPosX: Long = 0L
    var deltaPosY: Long = 0L

    var nome = namePlanet
    private var cor = colourPlanet
    private var radio = radioBody
    private val constG = 0.5
    private var image = Image

    private var resultantAceX = 0.0
    private var resultantAceY = 0.0
    private var resultantVel = (resultantVelX.pow(2) + resultantVelY.pow(2)).pow(0.5)

    fun tick() {

        oldPosX = posX
        oldPosY = posY

        if (shouldMove && isMovable) {
            //println(bodyManager.globalTime)
            posX += (resultantVelX * bodyManager.globalTime).toInt()
            posY += (resultantVelY * bodyManager.globalTime).toInt()

            //posX += (resultantVelX * bodyManager.globalTime).toInt() + (resultantAceX * bodyManager.globalTime * bodyManager.globalTime / 2).toInt()
            //posY += (resultantVelY * bodyManager.globalTime).toInt() + (resultantAceY * bodyManager.globalTime * bodyManager.globalTime / 2).toInt()
        }

        deltaPosX = posX - oldPosX
        deltaPosY = posY - oldPosY

    }

    fun applyForce(other: Body) {

        var distance = ((this.posX-other.posX).toDouble().pow(2) + (this.posY-other.posY).toDouble().pow(2)).pow(0.5)
        if (distance < 500.0) {distance = 500.0}


        val force = -constG*this.bodyMass*other.bodyMass/distance

        val acceleration = force/this.bodyMass

        val posVectorX = (this.posX-other.posX)/distance
        val posVectorY = (this.posY-other.posY)/distance

        val vel = acceleration*bodyManager.globalTime

        resultantAceX =+ acceleration*posVectorX
        resultantAceY =+ acceleration*posVectorY

        resultantVelX += vel*posVectorX*bodyManager.globalTime
        resultantVelY += vel*posVectorY*bodyManager.globalTime

        if (resultantVelX > 10.0) {resultantVelX = 10.0}
        if (resultantVelY > 10.0) {resultantVelY = 10.0}


    }

    fun shouldMove(other: Body) {
        val hypotenuseAlpha = ((this.posX-other.posX).toDouble().pow(2) + (this.posY-other.posY).toDouble().pow(2)).pow(0.5)
        val sideCosAlpha = (other.posX-this.posX).toDouble()
        val cosAlpha = sideCosAlpha/hypotenuseAlpha

        val hypotenuseBeta = ((this.resultantVelX).pow(2)+(this.resultantVelY).pow(2)).pow(0.5)
        val sideCosBeta = (this.resultantVelX)
        val cosBeta = sideCosBeta/hypotenuseBeta

        val alfa = acos(cosAlpha)
        val beta = acos(cosBeta)

        val directionalVector = cos(beta-alfa)

        shouldMove = hypotenuseAlpha > this.radio+other.radio+resultantVel*directionalVector
    }

    fun isMouseAboveMe(): Boolean {
        if (mouseInsideScreenPosX+Camera.x-16 < this.posX+this.radio &&
            mouseInsideScreenPosX+Camera.x+16 > this.posX-this.radio &&
            mouseInsideScreenPosY+Camera.y-32 < this.posY+this.radio &&
            mouseInsideScreenPosY+Camera.y-32 > this.posY-this.radio
        ) {
            return true
        }
        return false
    }

    fun render(g: Graphics) {
        /*
        if (isMouseAboveMe()) {
            g.color = Color.blue
        }
        else {
            g.color = Color.white
        }
        g.fillOval((posX-radio-Camera.x).toInt(), posY.toInt()-radio-Camera.y, radio*2, radio*2)

        g.color = Color.red
        g.drawLine(
            (pos_x-Camera.x.toLong()).toInt(), pos_y.toInt()-Camera.y,
            (pos_x+(resultantVelX)).toInt()-Camera.x, (pos_y+resultantVelY).toInt()-Camera.y)
        */
        g.color = Color.white
        g.drawImage(image, (posX-radio-Camera.x).toInt(), posY.toInt()-radio-Camera.y, null)
    }

}
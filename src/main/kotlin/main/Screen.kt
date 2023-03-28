import body.Body
import body.BodyManager
import sun.font.TrueTypeFont
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JFrame
import java.awt.image.BufferedImage
import java.lang.NullPointerException
import javax.imageio.ImageIO


object Screen : Canvas(), Runnable, MouseListener, KeyListener {

    private var earthOnFocus: Boolean = false
    private var moonOnFocus: Boolean = false
    private var wasMousePressed: Boolean = false
    private const val NAME = "Simulation"

    private val frame = JFrame(NAME)

    const val WIDTH = 1280
    const val HEIGHT = 1280
    private const val SCALE = 1
    private var isRunning = false

    var firstScene = "FORCE"

    var bodyManager = BodyManager()

    private val cursorImg = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)

    var isTimeFrozen = false

    private val blankCursor: Cursor = Toolkit.getDefaultToolkit().createCustomCursor(
        cursorImg, Point(0, 0), "blank cursor"
    )

    val earthImage: Image = ImageIO.read(javaClass.getResource("/earth.png")).getScaledInstance(50, 50, 0)
    val moonImage = ImageIO.read(javaClass.getResource("/moon.png")).getScaledInstance(12, 12, 0)
    val marsImage = ImageIO.read(javaClass.getResource("/mars.png")).getScaledInstance(40, 40, 0)

    var Terra = Body("terra", "azul", 25, this.WIDTH.toLong(), this.HEIGHT.toLong(),
        810, 0.0, isMovable = false, Image = earthImage)

    var Marte = Body("marte", "vermelho", 20, 2000, 900,
        19000, isMovable = false, Image = marsImage)

    var Lua = Body("lua","cinza",6, 1000, 900,
        100, 20.0, Image = moonImage)

    var arrayOfStars = arrayListOf<Array<Int>>()

    var mouseInsideScreenPosX = 0
    var mouseInsideScreenPosY = 0

    init {

        addMouseListener(this)
        addKeyListener(this)

        minimumSize = Dimension(WIDTH*SCALE, HEIGHT*SCALE)
        maximumSize = Dimension(WIDTH*SCALE, HEIGHT*SCALE)
        preferredSize = Dimension(WIDTH*SCALE, HEIGHT*SCALE)

        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.layout = BorderLayout()
        frame.add(this, BorderLayout.CENTER)
        frame.isResizable = false
        frame.isAutoRequestFocus = true
        frame.pack()
        frame.isVisible = true

        bodyManager.bodyArray.add(Terra)
        bodyManager.bodyArray.add(Lua)
        //bodyManager.bodyArray.add(Marte)

    }

    fun start() {
        this.isRunning = true
        Thread(this).start()
    }

    fun stop() {
        this.isRunning = false
    }

    override fun run() {
        var lastTime: Long = System.nanoTime()
        var nsPerTick: Double = 1000000000.0/60.0

        var ticks = 0
        var frames = 0

        var lastTimer: Long = System.currentTimeMillis()
        var delta = 0.0



        while (isRunning) {
            var now: Long = System.nanoTime()
            delta += (now - lastTime)/nsPerTick
            lastTime = now

            var shouldRender = false

            while (delta >= 1) {

                ticks++
                delta -= 1
                shouldRender = true
            }
            if (shouldRender) {
                frames++
                this.render()
                this.tick()
            }
            if ((System.currentTimeMillis() - lastTimer) >= 1000) {
                lastTimer += 1000
                bodyManager.globalTime++

                //println("ticks: "+ticks+"frames: "+frames+"globalTime "+bodyManager.globalTime)
                frames = 0
                ticks = 0
            }
        }
    }

    private fun tick() {

        try {
            mouseInsideScreenPosX = frame.mousePosition.location.x
            mouseInsideScreenPosY = frame.mousePosition.location.y
        } catch (e: NullPointerException) {
            mouseInsideScreenPosX = 0
            mouseInsideScreenPosY = 0
        }

        if (!isTimeFrozen) {
            bodyManager.tick()
        }

        Camera.tick()

        if (moonOnFocus) {
            Camera.x = Lua.posX.toInt()-WIDTH/2
            Camera.y = Lua.posY.toInt()-HEIGHT/2+300
        }

        if (earthOnFocus) {
            Camera.x = Terra.posX.toInt()-WIDTH/2
            Camera.y = Terra.posY.toInt()-HEIGHT/2+300
        }

        if (Camera.isPressed) {
            frame.contentPane.cursor = blankCursor
        } else {
            frame.contentPane.cursor = Cursor.getDefaultCursor()
        }

        for (body in bodyManager.bodyArray) {
            if (body.isMouseAboveMe() && wasMousePressed) {
                Camera.isPressed = false
                body.posX = mouseInsideScreenPosX.toLong()+Camera.x-8
                body.posY = mouseInsideScreenPosY.toLong()+Camera.y-32
            }
        }

    }

    private fun render() {

        val bs = bufferStrategy
        if (bs == null) {
            createBufferStrategy(3)
            return
        }
        var g: Graphics = bs.drawGraphics
        g.color = Color.black
        g.fillRect(0,0, WIDTH* SCALE,  HEIGHT* SCALE)
        g.color = Color.WHITE


        bodyManager.render(g)

        g.color = Color.RED
        g.drawLine(Terra.posX.toInt()-Camera.x, Terra.posY.toInt()-Camera.y,Lua.posX.toInt()-Camera.x, Lua.posY.toInt()-Camera.y)
        g.color = Color.YELLOW
        g.drawLine(Lua.posX.toInt()-Camera.x, Lua.posY.toInt()-Camera.y,
                   Lua.posX.toInt()-Camera.x+Lua.deltaPosX.toInt(),Lua.posY.toInt()-Camera.y+Lua.deltaPosY.toInt())


        g.dispose()
        bs.show()
    }

    override fun mouseClicked(e: MouseEvent?) {
    }

    override fun mousePressed(e: MouseEvent?) {
        wasMousePressed = true
        Camera.isPressed = true
    }

    override fun mouseReleased(e: MouseEvent?) {

        wasMousePressed = false
        Camera.isPressed = false
    }

    override fun mouseEntered(e: MouseEvent?) {
    }

    override fun mouseExited(e: MouseEvent?) {
    }

    override fun keyTyped(e: KeyEvent) {


    }

    override fun keyPressed(e: KeyEvent?) {
        if (e?.keyCode == KeyEvent.VK_SPACE) {
            isTimeFrozen = !isTimeFrozen
            println(isTimeFrozen)
        }
        if (e?.keyCode == KeyEvent.VK_L) {
            moonOnFocus = !moonOnFocus
        }
        if (e?.keyCode == KeyEvent.VK_T) {
            earthOnFocus = !earthOnFocus
        }
    }

    override fun keyReleased(e: KeyEvent?) {
    }

}


object Camera {
    var x = Screen.WIDTH/2
    var y = Screen.HEIGHT/2
    var isPressed = false

    var oldMousePosX = 0
    var oldMousePosY = 0
    var newMousePosX = 0
    var newMousePosY = 0

    fun tick() {
        update_pos()
    }

    fun update_pos() {
        //println("Mouse "+Screen.mouseInsideScreenPosX)
        //println("Camera "+this.x)
        //println("Terra "+Terra.posX)
        var mouse_x = MouseInfo.getPointerInfo().location.x
        var mouse_y = MouseInfo.getPointerInfo().location.y
        if (isPressed) {
            newMousePosX = mouse_x
            newMousePosY = mouse_y
            this.x -= newMousePosX - oldMousePosX
            this.y -= newMousePosY - oldMousePosY
        }
        oldMousePosX = MouseInfo.getPointerInfo().location.x
        oldMousePosY = MouseInfo.getPointerInfo().location.y

    }

}

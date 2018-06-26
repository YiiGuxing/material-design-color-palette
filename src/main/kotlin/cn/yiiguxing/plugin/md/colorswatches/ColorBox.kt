package cn.yiiguxing.plugin.md.colorswatches

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import kotlin.properties.Delegates

class ColorBox(val color: Color) : JComponent() {

    private var isHovering: Boolean = false

    private var onCheckedChangedHandler: ((ColorBox, Boolean) -> Unit)? = null

    var isChecked: Boolean by Delegates.observable(false) { _, oldValue: Boolean, newValue: Boolean ->
        if (oldValue != newValue) {
            onCheckedChangedHandler?.invoke(this, newValue)
            repaint()
        }
    }

    init {
        isOpaque = false
        isDoubleBuffered = true
        addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                isHovering = true
                repaint()
            }

            override fun mouseExited(e: MouseEvent) {
                isHovering = false
                repaint()
            }

            override fun mouseReleased(e: MouseEvent) {
                if (e.button == MouseEvent.BUTTON1
                        && !isChecked
                        && e.x in 0..width
                        && e.y in 0..height) {
                    isChecked = true
                }
            }
        })
    }

    fun onCheckedChanged(handler: (colorBox: ColorBox, isChecked: Boolean) -> Unit) {
        onCheckedChangedHandler = handler
    }

    override fun getComponentGraphics(g: Graphics): Graphics {
        return super.getComponentGraphics(g).apply {
            this as Graphics2D
            setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        g.color = color
        when {
            isChecked -> g.fillOval(0, 0, width, height)
            isHovering -> g.fillRoundRect(0, 0, width, height, width / 2, height / 2)
            else -> g.fillRect(0, 0, width, height)
        }
    }

}
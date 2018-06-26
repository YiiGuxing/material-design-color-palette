package cn.yiiguxing.plugin.md.colorswatches

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridConstraints.*
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.border.LineBorder

/**
 * MaterialPaletteDialog
 *
 * Created by Yii.Guxing on 2018/06/25
 */
class MaterialPaletteDialog(project: Project?) : DialogWrapper(project) {

    private val form = MaterialPaletteForm()
    private val group = ColorBoxGroup { form.preview(it) }

    init {
        title = "Material Palette"
        form.initPalette()
        init()
    }

    override fun createCenterPanel(): JComponent = form.rootPanel

    private fun MaterialPaletteForm.initPalette() {
        MATERIAL_COLOR_PALETTE.entries.forEachIndexed { row, (name, colors) ->
            val nameCons = GridConstraints(row, 0, 1, 1,
                    ANCHOR_EAST, FILL_NONE, SIZEPOLICY_FIXED, SIZEPOLICY_FIXED,
                    null, JBDimension(-1, 50), null)
            namesPanel.add(JLabel(name), nameCons)

            colors.forEachIndexed { column, color ->
                val colorCons = GridConstraints(row, column, 1, 1,
                        ANCHOR_CENTER, FILL_NONE, SIZEPOLICY_FIXED, SIZEPOLICY_FIXED,
                        null, JBDimension(50, 50), null)
                val colorBox = ColorBox(color)
                group.add(colorBox)
                contentPanel.add(colorBox, colorCons)
            }
        }

        headerPanel.apply {
            for (i in 0 until componentCount) {
                getComponent(i).preferredSize = JBDimension(50, -1)
            }
        }

        syncScroll()
        initPreviewComponents()
    }

    private fun MaterialPaletteForm.initPreviewComponents() {
        primaryPreviewTitle.apply { font = font.deriveFont(JBUI.scale(14f)) }
        primaryColorLabel.apply {
            setCopyable(true)
            font = font.deriveFont(JBUI.scale(14f))
        }
        lightColorLabel.setCopyable(true)
        darkColorLabel.setCopyable(true)

        preview(null)
    }

    private fun MaterialPaletteForm.syncScroll() {
        leftScrollPane.verticalScrollBar.preferredSize = Dimension(0, 0)

        val namesViewport = leftScrollPane.viewport
        val colorsViewport = contentScrollPane.viewport
        namesViewport.apply {
            addChangeListener { colorsViewport.viewPosition = viewPosition }
        }
        colorsViewport.apply {
            addChangeListener { namesViewport.viewPosition = viewPosition }
        }
    }

    private fun MaterialPaletteForm.preview(color: Color?) {
        val light = color?.brighten()
        val dark = color?.darken()
        val contentColor = color?.contentColor
        val lightContentColor = light?.contentColor
        val darkContentColor = dark?.contentColor

        val hasColor = color != null
        val paneBorder = if (hasColor) null else LineBorder(Color.GRAY)
        primaryPreviewPanel.apply {
            border = paneBorder
            isOpaque = hasColor
            background = color
        }
        lightPreviewPanel.apply {
            border = paneBorder
            isOpaque = hasColor
            background = light
        }
        darkPreviewPanel.apply {
            border = paneBorder
            isOpaque = hasColor
            background = dark
        }

        primaryColorLabel.apply {
            text = color?.hex ?: ""
            isVisible = hasColor
            foreground = contentColor
        }
        lightColorLabel.apply {
            text = light?.hex ?: ""
            isVisible = hasColor
            foreground = lightContentColor
        }
        darkColorLabel.apply {
            text = dark?.hex ?: ""
            isVisible = hasColor
            foreground = darkContentColor
        }

        primaryPreviewTitle.apply {
            foreground = contentColor ?: Color.GRAY
        }
        lightPreviewTitle.apply {
            foreground = lightContentColor ?: Color.GRAY
        }
        darkPreviewTitle.apply {
            foreground = darkContentColor ?: Color.GRAY
        }
    }
}
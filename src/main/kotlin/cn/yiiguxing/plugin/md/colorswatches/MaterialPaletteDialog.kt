package cn.yiiguxing.plugin.md.colorswatches

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.ui.border.CustomLineBorder
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridConstraints.*
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Dimension
import java.awt.Insets
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
        form.init()
        init()
    }

    override fun createCenterPanel(): JComponent = form.rootPanel

    private fun MaterialPaletteForm.init() {
        colorPalettePanel.border = LineBorder(BORDER_COLOR)
        leftScrollPane.border = CustomLineBorder(BORDER_COLOR, Insets(0, 0, 0, 1))
        contentScrollPane.border = null

        initColorPalette()
        initHeader()
        initPreviewComponents()
        syncScroll()
    }

    private fun MaterialPaletteForm.initColorPalette() {
        MATERIAL_COLOR_PALETTE.entries.forEachIndexed { row, (name, colors) ->
            val nameCons = GridConstraints(row, 0, 1, 1,
                    ANCHOR_EAST, FILL_NONE, SIZEPOLICY_FIXED, SIZEPOLICY_FIXED,
                    null, JBDimension(-1, COLOR_BOX_SIZE), null)
            namesPanel.add(JLabel(name), nameCons)

            colors.forEachIndexed { column, color ->
                val colorCons = GridConstraints(row, column, 1, 1,
                        ANCHOR_CENTER, FILL_NONE, SIZEPOLICY_FIXED, SIZEPOLICY_FIXED,
                        null, JBDimension(COLOR_BOX_SIZE, COLOR_BOX_SIZE), null)
                val colorBox = ColorBox(color)
                group.add(colorBox)
                contentPanel.add(colorBox, colorCons)
            }
        }
    }

    private fun MaterialPaletteForm.initHeader() {
        headerSpace.border = CustomLineBorder(BORDER_COLOR, Insets(0, 0, 1, 1))
        headerPanel.border = CustomLineBorder(BORDER_COLOR, Insets(0, 0, 1, 0))

        saturationPanel.apply {
            for (i in 0 until componentCount) {
                getComponent(i).preferredSize = JBDimension(COLOR_BOX_SIZE, -1)
            }
        }
        saturationAPanel.apply {
            for (i in 0 until componentCount) {
                getComponent(i).preferredSize = JBDimension(COLOR_BOX_SIZE, -1)
            }

            border = CustomLineBorder(BORDER_COLOR, Insets(0, 1, 0, 0))
        }
    }

    private fun MaterialPaletteForm.initPreviewComponents() {
        previewPanel.border = LineBorder(BORDER_COLOR)
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
        val paneBorder = if (hasColor) null else LineBorder(BORDER_COLOR)
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

    companion object {
        private const val COLOR_BOX_SIZE = 35
        private val BORDER_COLOR = JBColor(0xB3B3B3, 0x232323)
    }
}
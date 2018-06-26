package cn.yiiguxing.plugin.md.colorswatches

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.Color
import javax.swing.JComponent
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
        form.initPalette()
        init()
    }

    private fun MaterialPaletteForm.initPalette() {
        preview(null)
    }

    override fun createCenterPanel(): JComponent = form.rootPanel

    private fun MaterialPaletteForm.preview(color: Color?) {
        val light = color?.brighten()
        val dark = color?.darken()
        val contentColor = color?.contentColor
        val lightContentColor = light?.contentColor
        val darkContentColor = dark?.contentColor

        val hasColor = color != null
        val paneBorder = if (hasColor) null else LineBorder(Color.WHITE)
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
            text = color?.hex
            isVisible = hasColor
            foreground = contentColor
        }
        lightColorLabel.apply {
            text = light?.hex
            isVisible = hasColor
            foreground = lightContentColor
        }
        darkColorLabel.apply {
            text = dark?.hex
            isVisible = hasColor
            foreground = darkContentColor
        }

        primaryPreviewTitle.apply {
            isVisible = hasColor
            foreground = contentColor
        }
        lightPreviewTitle.apply {
            isVisible = hasColor
            foreground = lightContentColor
        }
        darkPreviewTitle.apply {
            isVisible = hasColor
            foreground = darkContentColor
        }
    }
}
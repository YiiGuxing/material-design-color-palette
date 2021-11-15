package cn.yiiguxing.plugin.md.palette

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import javax.swing.JLabel
import javax.swing.JPanel

internal class MaterialPaletteUI {
    lateinit var rootPanel: JPanel
    lateinit var headerPanel: JPanel
    lateinit var leftScrollPane: JBScrollPane
    lateinit var namesPanel: JPanel
    lateinit var contentScrollPane: JBScrollPane
    lateinit var contentPanel: JPanel
    lateinit var primaryPreviewPanel: JPanel
    lateinit var lightPreviewPanel: JPanel
    lateinit var darkPreviewPanel: JPanel
    lateinit var primaryColorLabel: JBLabel
    lateinit var lightColorLabel: JBLabel
    lateinit var darkColorLabel: JBLabel
    lateinit var primaryPreviewTitle: JLabel
    lateinit var lightPreviewTitle: JLabel
    lateinit var darkPreviewTitle: JLabel
    lateinit var colorPalettePanel: JPanel
    lateinit var headerSpace: JPanel
    lateinit var saturationPanel: JPanel
    lateinit var saturationAPanel: JPanel
    lateinit var previewPanel: JPanel

    val messageLabel: JLabel = JLabel(" ").apply { foreground = JBColor.GRAY }
    val messagePanel = JPanel().apply { add(messageLabel) }
}
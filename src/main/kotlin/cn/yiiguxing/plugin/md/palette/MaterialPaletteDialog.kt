package cn.yiiguxing.plugin.md.palette

import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.JBColor
import com.intellij.ui.PopupMenuListenerAdapter
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.border.CustomLineBorder
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridConstraints.*
import com.intellij.util.Alarm
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.LineBorder
import javax.swing.event.PopupMenuEvent

/**
 * MaterialPaletteDialog
 *
 * Created by Yii.Guxing on 2018/06/25
 */
class MaterialPaletteDialog(project: Project?) : DialogWrapper(project) {

    private val ui = MaterialPaletteUI()
    private val group = ColorBoxGroup { ui.preview(it) }

    private var currentMessageBalloon: Balloon? = null
    private val alarm: Alarm = Alarm(disposable)

    init {
        title = "Material Palette"
        isModal = false
        setResizable(false)

        registerShortcuts()
        ui.init()
        init()
    }

    override fun createSouthAdditionalPanel(): JPanel = ui.messagePanel

    private fun registerShortcuts() {
        DumbAwareAction.create { copyColor(group.checkedColor, ColorType.HEX) }
            .registerCustomShortcutSet(CustomShortcutSet.fromString("ctrl 1"), rootPane, disposable)
        DumbAwareAction.create { copyColor(group.checkedColor?.brighten(), ColorType.HEX) }
            .registerCustomShortcutSet(CustomShortcutSet.fromString("ctrl 2"), rootPane, disposable)
        DumbAwareAction.create { copyColor(group.checkedColor?.darken(), ColorType.HEX) }
            .registerCustomShortcutSet(CustomShortcutSet.fromString("ctrl 3"), rootPane, disposable)
    }

    override fun createCenterPanel(): JComponent = ui.rootPanel

    override fun createActions(): Array<Action> = arrayOf(okAction, ResetAction())

    private fun MaterialPaletteUI.init() {
        colorPalettePanel.border = LineBorder(BORDER_COLOR)
        leftScrollPane.border = CustomLineBorder(BORDER_COLOR_FIXED, Insets(0, 0, 0, 1))
        contentScrollPane.border = null

        initColorPalette()
        initHeader()
        initPreviewComponents()
        syncScroll()
    }

    private fun MaterialPaletteUI.initColorPalette() {
        contentPanel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        MATERIAL_COLOR_PALETTE.entries.forEachIndexed { row, (name, colors) ->
            val nameCons = GridConstraints(
                row, 0, 1, 1,
                ANCHOR_EAST, FILL_NONE, SIZEPOLICY_FIXED, SIZEPOLICY_FIXED,
                null, JBDimension(-1, COLOR_BOX_SIZE), null
            )
            namesPanel.add(JLabel(name), nameCons)

            colors.forEachIndexed { column, color ->
                val colorCons = GridConstraints(
                    row, column, 1, 1,
                    ANCHOR_CENTER, FILL_NONE, SIZEPOLICY_FIXED, SIZEPOLICY_FIXED,
                    null, JBDimension(COLOR_BOX_SIZE, COLOR_BOX_SIZE), null
                )
                val colorBox = ColorBox(color).apply {
                    addMouseListener(HintHandler(color, name, VARIANTS[column]))
                }
                group.add(colorBox)
                contentPanel.add(colorBox, colorCons)
            }
        }
    }

    private fun MaterialPaletteUI.initHeader() {
        headerSpace.border = CustomLineBorder(BORDER_COLOR, Insets(0, 0, 1, 0))
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

    private fun MaterialPaletteUI.initPreviewComponents() {
        previewPanel.border = LineBorder(BORDER_COLOR)
        primaryPreviewTitle.apply { font = font.deriveFont(JBUI.scale(15f)) }
        primaryColorLabel.apply {
            font = font.deriveFont(JBUI.scale(15f))
        }

        val defaultColorType = ColorType.HEX
        fun JPanel.initListeners(name: String, shortcut: String, getColor: () -> Color?) {
            setCopyAction(defaultColorType, getColor)
            addMouseListener(object : MouseAdapter() {
                override fun mouseEntered(e: MouseEvent?) {
                    val color = getColor()
                    this@MaterialPaletteDialog.ui.messageLabel.text =
                        if (color != null) "$name ${defaultColorType.getColorValue(color)} : Click to copy ($shortcut)" else " "
                }

                override fun mouseExited(e: MouseEvent?) {
                    this@MaterialPaletteDialog.ui.messageLabel.text = " "
                }
            })
        }

        primaryPreviewPanel.initListeners("Primary", "Ctrl+1") { group.checkedColor }
        lightPreviewPanel.initListeners("Light", "Ctrl+2") { group.checkedColor?.brighten() }
        darkPreviewPanel.initListeners("Dark", "Ctrl+3") { group.checkedColor?.darken() }

        preview(null)
    }

    private fun JPanel.setCopyAction(colorType: ColorType = ColorType.HEX, getColor: () -> Color?) {
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.button == MouseEvent.BUTTON1 && e.clickCount == 1) {
                    copyColor(getColor(), colorType)
                }
            }
        })

        val menuItems = ColorType.values().map { type ->
            JBMenuItem("Copy as $type").apply { addActionListener { copyColor(getColor(), type) } }
        }
        componentPopupMenu = JBPopupMenu().apply {
            for (item in menuItems) {
                add(item)
            }
            addPopupMenuListener(object : PopupMenuListenerAdapter() {
                override fun popupMenuWillBecomeVisible(e: PopupMenuEvent) {
                    val hasColor = getColor() != null
                    for (item in menuItems) {
                        item.isEnabled = hasColor
                    }
                }
            })
        }
    }

    private fun MaterialPaletteUI.syncScroll() {
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

    private fun MaterialPaletteUI.preview(color: Color?) {
        val light = color?.brighten()
        val dark = color?.darken()
        val contentColor = color?.contentColor
        val lightContentColor = light?.contentColor
        val darkContentColor = dark?.contentColor

        val hasColor = color != null
        val paneBackground = JBUI.CurrentTheme.CustomFrameDecorations.paneBackground()
        previewPanel.cursor = if (hasColor) {
            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        } else {
            Cursor.getDefaultCursor()
        }
        primaryPreviewPanel.background = color ?: Color.BLACK.alphaBlend(paneBackground, 0.2f)
        lightPreviewPanel.background = light ?: Color.BLACK.alphaBlend(paneBackground, 0.1f)
        darkPreviewPanel.background = dark ?: Color.BLACK.alphaBlend(paneBackground, 0.3f)

        primaryColorLabel.apply {
            text = color?.let { "#${it.hex}" } ?: ""
            isVisible = hasColor
            foreground = contentColor
        }
        lightColorLabel.apply {
            text = light?.let { "#${it.hex}" } ?: ""
            isVisible = hasColor
            foreground = lightContentColor
        }
        darkColorLabel.apply {
            text = dark?.let { "#${it.hex}" } ?: ""
            isVisible = hasColor
            foreground = darkContentColor
        }

        primaryPreviewTitle.foreground = contentColor ?: JBColor.GRAY
        lightPreviewTitle.foreground = lightContentColor ?: JBColor.GRAY
        darkPreviewTitle.foreground = darkContentColor ?: JBColor.GRAY
    }

    private fun copyColor(color: Color?, colorType: ColorType) {
        color ?: return
        CopyPasteManager.getInstance().setContents(StringSelection(colorType.getColorValue(color)))
        if (currentMessageBalloon == null) {
            currentMessageBalloon = JBPopupFactory.getInstance().let { factory ->
                factory.createHtmlTextBalloonBuilder("Copied to clipboard.", MessageType.INFO, null)
                    .createBalloon().also { balloon ->
                        balloon.addListener(object : JBPopupListener {
                            override fun onClosed(event: LightweightWindowEvent) {
                                currentMessageBalloon = null
                                alarm.cancelAllRequests()
                            }
                        })
                        Disposer.register(disposable) { balloon.hide() }
                        val point = Point(ui.previewPanel.width / 2, ui.previewPanel.height - 3)
                        balloon.show(RelativePoint(ui.previewPanel, point), Balloon.Position.above)
                    }
            }
        }
        alarm.cancelAllRequests()
        alarm.addRequest({ currentMessageBalloon?.hide() }, 3000)
    }

    private inner class ResetAction : DialogWrapperAction("Reset") {
        override fun doAction(e: ActionEvent) {
            group.checkedBox = null
        }
    }

    private inner class HintHandler(
        color: Color,
        private val colorName: String,
        private val variant: String
    ) : MouseAdapter() {
        private val color: String = ColorType.HEX.getColorValue(color)

        override fun mouseEntered(e: MouseEvent?) {
            ui.messageLabel.text = "$colorName - $variant : $color"
        }

        override fun mouseExited(e: MouseEvent?) {
            ui.messageLabel.text = " "
        }
    }

    companion object {
        private const val COLOR_BOX_SIZE = 40
        private val BORDER_COLOR = JBColor(0xB3B3B3, 0x232323)
        private val BORDER_COLOR_FIXED = JBColor(0xB3B3B3, 0x000000)

        private val sDialogMap = HashMap<Project, MaterialPaletteDialog>()

        fun show(project: Project) {
            val dialog = synchronized(sDialogMap) {
                sDialogMap.getOrPut(project) {
                    MaterialPaletteDialog(project).apply {
                        Disposer.register(disposable) {
                            synchronized(sDialogMap) {
                                sDialogMap.remove(project)
                            }
                        }
                        project.let { Disposer.register(it, disposable) }
                    }
                }
            }

            if (!dialog.isShowing) {
                dialog.show()
            } else {
                IdeFocusManager.getInstance(project).requestFocus(dialog.window, true)
            }
        }
    }
}
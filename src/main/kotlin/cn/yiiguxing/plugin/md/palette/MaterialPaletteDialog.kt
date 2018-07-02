package cn.yiiguxing.plugin.md.palette

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.JBColor
import com.intellij.ui.PopupMenuListenerAdapter
import com.intellij.ui.border.CustomLineBorder
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridConstraints.*
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Insets
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

    private val form = MaterialPaletteForm()
    private val group = ColorBoxGroup { form.preview(it) }

    init {
        title = "Material Palette"
        isModal = false
        setResizable(false)

        peer.setAppIcons()
        form.init()
        init()
    }

    override fun createCenterPanel(): JComponent = form.rootPanel

    override fun createActions(): Array<Action> = arrayOf(okAction, ResetAction())

    private fun MaterialPaletteForm.init() {
        colorPalettePanel.border = LineBorder(BORDER_COLOR)
        leftScrollPane.border = CustomLineBorder(BORDER_COLOR_FIXED, Insets(0, 0, 0, 1))
        contentScrollPane.border = null

        initColorPalette()
        initHeader()
        initPreviewComponents()
        syncScroll()
    }

    private fun MaterialPaletteForm.initColorPalette() {
        contentPanel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
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

    private fun MaterialPaletteForm.initPreviewComponents() {
        previewPanel.border = LineBorder(BORDER_COLOR)
        primaryPreviewTitle.apply { font = font.deriveFont(JBUI.scale(15f)) }
        primaryColorLabel.apply {
            setCopyable(true)
            font = font.deriveFont(JBUI.scale(15f))
        }
        lightColorLabel.setCopyable(true)
        darkColorLabel.setCopyable(true)

        primaryPreviewPanel.setCopyAction { group.checkedColor }
        lightPreviewPanel.setCopyAction { group.checkedColor?.brighten() }
        darkPreviewPanel.setCopyAction { group.checkedColor?.darken() }

        preview(null)
    }

    private fun JPanel.setCopyAction(getColor: () -> Color?) {
        fun copyColor() {
            getColor()?.let {
                CopyPasteManager.getInstance().setContents(StringSelection(it.hex))
            }
        }

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.button == MouseEvent.BUTTON1 && e.clickCount == 1) {
                    copyColor()
                }
            }
        })

        val copyItem = JBMenuItem("Copy Color", AllIcons.Actions.Copy)
                .apply {
                    addActionListener { copyColor() }
                }
        componentPopupMenu = JBPopupMenu().apply {
            add(copyItem)
            addPopupMenuListener(object : PopupMenuListenerAdapter() {
                override fun popupMenuWillBecomeVisible(e: PopupMenuEvent) {
                    copyItem.isEnabled = group.checkedColor != null
                }
            })
        }
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
        previewPanel.cursor = if (hasColor) {
            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        } else {
            Cursor.getDefaultCursor()
        }
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

    private inner class ResetAction : DialogWrapperAction("Reset") {
        override fun doAction(e: ActionEvent) {
            group.checkedBox = null
        }
    }

    companion object {
        private const val COLOR_BOX_SIZE = 40
        private val BORDER_COLOR = JBColor(0xB3B3B3, 0x232323)
        private val BORDER_COLOR_FIXED = JBColor(0xB3B3B3, 0x000000)

        private val sDialogMap = HashMap<Project?, MaterialPaletteDialog>()

        fun show(project: Project?) {
            val dialog = synchronized(sDialogMap) {
                sDialogMap.getOrPut(project) {
                    MaterialPaletteDialog(project).apply {
                        Disposer.register(disposable, Disposable {
                            synchronized(sDialogMap) {
                                sDialogMap.remove(project)
                            }
                        })
                        project?.let { Disposer.register(it, disposable) }
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
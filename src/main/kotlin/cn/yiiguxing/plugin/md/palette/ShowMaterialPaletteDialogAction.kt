package cn.yiiguxing.plugin.md.palette

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
 * ShowMaterialPaletteDialogAction
 *
 * Created by Yii.Guxing on 2018/06/25
 */
class ShowMaterialPaletteDialogAction : AnAction(icon) {

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.let { MaterialPaletteDialog.show(it) }
    }

    companion object {
        val icon: Icon = IconLoader.getIcon("/icon.png")
    }
}
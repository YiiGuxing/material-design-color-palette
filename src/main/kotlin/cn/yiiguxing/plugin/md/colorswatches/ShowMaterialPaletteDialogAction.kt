package cn.yiiguxing.plugin.md.colorswatches

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * ShowMaterialPaletteDialogAction
 *
 * Created by Yii.Guxing on 2018/06/25
 */
class ShowMaterialPaletteDialogAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        MaterialPaletteDialog(e.project).show()
    }
}
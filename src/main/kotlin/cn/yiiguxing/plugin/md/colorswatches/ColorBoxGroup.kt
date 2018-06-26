package cn.yiiguxing.plugin.md.colorswatches

import java.awt.Color
import java.util.*
import kotlin.properties.Delegates

class ColorBoxGroup(private val onColorChanged: (color: Color?) -> Unit) {

    private val boxes = Vector<ColorBox>()

    var checkedBox: ColorBox? by Delegates.observable(null) { _, oldValue: ColorBox?, newValue: ColorBox? ->
        oldValue?.isChecked = false
        if (oldValue !== newValue) {
            onColorChanged(checkedColor)
        }
    }

    val checkedColor: Color? get() = checkedBox?.color

    private val onBoxCheckedChangedHandler = { colorBox: ColorBox, isChecked: Boolean ->
        if (isChecked) {
            checkedBox = colorBox
        } else if (colorBox === checkedBox) {
            checkedBox = null
        }
    }

    fun add(box: ColorBox) {
        boxes.addElement(box)
        box.onCheckedChanged(onBoxCheckedChangedHandler)
    }

}
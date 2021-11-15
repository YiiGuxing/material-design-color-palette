package cn.yiiguxing.plugin.md.palette

import java.awt.Color

enum class ColorType(private val displayName: String) {
    HEX("HEX") {
        override fun getColorValue(color: Color): String = "#${color.hex}"
    },

    RGB("RGB") {
        override fun getColorValue(color: Color): String = "rgb(${color.red}, ${color.green}, ${color.blue})"
    },

    HSL("HSL") {
        override fun getColorValue(color: Color): String = color.toHSL().let { (h, s, l) ->
            "hsl($h, $s%, $l%)"
        }
    };

    abstract fun getColorValue(color: Color): String

    override fun toString(): String = displayName

}
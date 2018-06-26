/*
 * Colors
 *
 * Created by Yii.Guxing on 2018/06/24
 */
package cn.yiiguxing.plugin.md.colorswatches

import java.awt.Color
import java.lang.Math.*


// LAB CONSTANTS
private const val Kn = 18.0f
private const val Xn = 0.950470f
private const val Yn = 1.0f
private const val Zn = 1.088830f
private const val t0 = 0.137931034f
private const val t1 = 0.206896552f
private const val t2 = 0.12841855f
private const val t3 = 0.008856452f


private val Int.rgb_xyz: Float
    get() {
        val v = this / 255.0f
        return if (v <= 0.04045f) {
            v / 12.92f
        } else {
            pow((v + 0.055) / 1.055, 2.4).toFloat()
        }
    }

private val Float.xyz_lab: Float
    get() = if (this > t3) {
        pow(this.toDouble(), 1.0 / 3.0).toFloat()
    } else {
        this / t2 + t0
    }

private val Float.lab_xyz: Float
    get() = if (this > t1) {
        this * this * this
    } else {
        t2 * (this - t0)
    }

private val Float.xyz_rgb: Float
    get() = if (this <= 0.00304) {
        max(0.0f, min(this * 12.92f, 1.0f))
    } else {
        max(0.0f, min(1.055f * pow(this.toDouble(), 1 / 2.4).toFloat() - 0.055f, 1.0f))
    }

private fun Color.toXYZ(): FloatArray {
    val r = red.rgb_xyz
    val g = green.rgb_xyz
    val b = blue.rgb_xyz
    val x = ((0.4124564f * r + 0.3575761f * g + 0.1804375f * b) / Xn).xyz_lab
    val y = ((0.2126729f * r + 0.7151522f * g + 0.0721750f * b) / Yn).xyz_lab
    val z = ((0.0193339f * r + 0.1191920f * g + 0.9503041f * b) / Zn).xyz_lab

    return floatArrayOf(x, y, z)
}

fun getLabColor(l: Float, a: Float, b: Float, alpha: Float = 1.0f): Color {
    var y = (l + 16f) / 116f
    var x = y + a / 500
    var z = y - b / 200

    x = Xn * x.lab_xyz
    y = Yn * y.lab_xyz
    z = Zn * z.lab_xyz

    val red = (3.2404542f * x - 1.5371385f * y - 0.4985314f * z).xyz_rgb
    val green = (-0.9692660f * x + 1.8760108f * y + 0.0415560f * z).xyz_rgb
    val blue = (0.0556434f * x - 0.2040259f * y + 1.0572252f * z).xyz_rgb

    return Color(red, green, blue, alpha)
}

fun Color.toLab(): FloatArray {
    val (x, y, z) = toXYZ()

    val l = 116.0f * y - 16.0f
    val a = 500.0f * (x - y)
    val b = 200.0f * (y - z)

    return floatArrayOf(l, a, b)
}

fun Color.brighten(amount: Float = 1f): Color {
    if (amount == 0f) {
        return this
    }

    var (l, a, b) = toLab()
    l += Kn * amount

    return getLabColor(l, a, b, alpha / 255f)
}

fun Color.darken(amount: Float = 1f): Color = brighten(-amount)

val Color.contentColor: Color get() = if (toLab()[0] > 50) Color.BLACK else Color.WHITE

private val hexChars = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

val Color.hex: String
    get() = String(CharArray(6) { hexChars[(rgb shr (20 - 4 * (it - 1))) and 0xF] })

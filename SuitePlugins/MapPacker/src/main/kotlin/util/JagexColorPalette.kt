package util

class JagexColorPalette(brightness: Double, var2: Int, var3: Int) {
    val colorPalette: IntArray

    init {
        colorPalette = buildColorPalettee(brightness, var2, var3)
    }

    private fun buildColorPalettee(brightness: Double, var2: Int, var3: Int): IntArray {
        val colorPalette = IntArray(65536)
        var var4 = var2 * 128
        for (var5 in var2 until var3) {
            val var6 = (var5 shr 3).toDouble() / 64.0 + 0.0078125
            val var8 = (var5 and 7).toDouble() / 8.0 + 0.0625
            for (var10 in 0..127) {
                val var11 = var10.toDouble() / 128.0
                var var13 = var11
                var var15 = var11
                var var17 = var11
                if (var8 != 0.0) {
                    var var19: Double
                    var19 = if (var11 < 0.5) {
                        var11 * (1.0 + var8)
                    } else {
                        var11 + var8 - var11 * var8
                    }
                    val var21 = 2.0 * var11 - var19
                    var var23 = var6 + 0.3333333333333333
                    if (var23 > 1.0) {
                        --var23
                    }
                    var var27 = var6 - 0.3333333333333333
                    if (var27 < 0.0) {
                        ++var27
                    }
                    var13 = if (6.0 * var23 < 1.0) {
                        var21 + (var19 - var21) * 6.0 * var23
                    } else if (2.0 * var23 < 1.0) {
                        var19
                    } else if (3.0 * var23 < 2.0) {
                        var21 + (var19 - var21) * (0.6666666666666666 - var23) * 6.0
                    } else {
                        var21
                    }
                    var15 = if (6.0 * var6 < 1.0) {
                        var21 + (var19 - var21) * 6.0 * var6
                    } else if (2.0 * var6 < 1.0) {
                        var19
                    } else if (3.0 * var6 < 2.0) {
                        var21 + (var19 - var21) * (0.6666666666666666 - var6) * 6.0
                    } else {
                        var21
                    }
                    var17 = if (6.0 * var27 < 1.0) {
                        var21 + (var19 - var21) * 6.0 * var27
                    } else if (2.0 * var27 < 1.0) {
                        var19
                    } else if (3.0 * var27 < 2.0) {
                        var21 + (var19 - var21) * (0.6666666666666666 - var27) * 6.0
                    } else {
                        var21
                    }
                }
                val var29 = (var13 * 256.0).toInt()
                val var20 = (var15 * 256.0).toInt()
                val var30 = (var17 * 256.0).toInt()
                var var22 = var30 + (var20 shl 8) + (var29 shl 16)
                var22 = adjustRGB(var22, brightness)
                if (var22 == 0) {
                    var22 = 1
                }
                colorPalette[var4++] = var22
            }
        }
        return colorPalette
    }

    companion object {
        private fun adjustRGB(var0: Int, var1: Double): Int {
            var var3 = (var0 shr 16).toDouble() / 256.0
            var var5 = (var0 shr 8 and 255).toDouble() / 256.0
            var var7 = (var0 and 255).toDouble() / 256.0
            var3 = Math.pow(var3, var1)
            var5 = Math.pow(var5, var1)
            var7 = Math.pow(var7, var1)
            val var9 = (var3 * 256.0).toInt()
            val var10 = (var5 * 256.0).toInt()
            val var11 = (var7 * 256.0).toInt()
            return var11 + (var10 shl 8) + (var9 shl 16)
        }

        fun packHsl(var0: Int, var1: Int, var2: Int): Int {
            var var1 = var1
            if (var2 > 179) {
                var1 /= 2
            }
            if (var2 > 192) {
                var1 /= 2
            }
            if (var2 > 217) {
                var1 /= 2
            }
            if (var2 > 243) {
                var1 /= 2
            }
            return (var1 / 32 shl 7) + (var0 / 4 shl 10) + var2 / 2
        }

        fun getJagexColorIndex(var0: Int, var1: Int): Int {
            var var1 = var1
            return if (var0 == -1) {
                12345678
            } else {
                var1 = (var0 and 127) * var1 / 128
                if (var1 < 2) {
                    var1 = 2
                } else if (var1 > 126) {
                    var1 = 126
                }
                (var0 and 65408) + var1
            }
        }
    }
}
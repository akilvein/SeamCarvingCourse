package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.security.MessageDigest
import javax.imageio.ImageIO


class Image {
    private val image: BufferedImage

    val width: Int
        get() = image.width

    val height: Int
        get() = image.height

    constructor(width: Int, height: Int, init: (Int, Int) -> Color) {
        image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until height) {
            for (x in 0 until width) {
                set(x, y, init(x, y))
            }
        }
    }

    constructor(filename: String) {
        val file = File(filename)
        image = ImageIO.read(file)
    }

    operator fun get(x: Int, y: Int): Color {
        if (x < 0 || x >= image.width) {
            throw IndexOutOfBoundsException("x must be between 0 and ${image.width - 1}")
        }
        if (y < 0 || y >= image.height) {
            throw IndexOutOfBoundsException("y must be between 0 and ${image.height - 1}")
        }
        return Color(image.getRGB(x, y))
    }

    operator fun set(x: Int, y: Int, color: Color) {
        if (x < 0 || x >= image.width) {
            throw IndexOutOfBoundsException("x must be between 0 and ${image.width - 1}")
        }
        if (y < 0 || y >= image.height) {
            throw IndexOutOfBoundsException("y must be between 0 and ${image.height - 1}")
        }
        image.setRGB(x, y, color.rgb)
    }

    fun save(filename: String) {
        val file = File(filename)
        val extension = file.extension
        ImageIO.write(image, extension, file);
    }

    private fun pixelEnergy(x: Int, y: Int): Double {
        fun dx2(x: Int, y: Int): Int =
                when (x) {
                    0 -> dx2(x + 1, y)
                    width - 1 -> dx2(x - 1, y)
                    else -> {
                        val l = get(x - 1, y)
                        val r = get(x + 1, y)
                        (l.red - r.red) * (l.red - r.red) + (l.green - r.green) * (l.green - r.green) + (l.blue - r.blue) * (l.blue - r.blue)
                    }
                }

        fun dy2(x: Int, y: Int): Int =
                when (y) {
                    0 -> dy2(x, y + 1)
                    height - 1 -> dy2(x, y - 1)
                    else -> {
                        val t = get(x, y - 1)
                        val b = get(x, y + 1)
                        (t.red - b.red) * (t.red - b.red) + (t.green - b.green) * (t.green - b.green) + (t.blue - b.blue) * (t.blue - b.blue)
                    }
                }

        return kotlin.math.sqrt(dx2(x, y).toDouble() + dy2(x, y).toDouble())
    }

    fun energyImage(): Image {
        var maxEnergyValue = 0.0

        val energy = Array(width) { x ->
            DoubleArray(height) { y ->
                val e = pixelEnergy(x, y)
                maxEnergyValue = kotlin.math.max(e, maxEnergyValue)
                e
            }
        }

        return Image(width, height) { x, y ->
            val intensity = (255.0 * energy[x][y] / maxEnergyValue).toInt()
            Color(intensity, intensity, intensity)
        }
    }

    fun getVerticalSeam() : Array<Int> {
        val imageSize = width * height
        val preceding = kotlin.IntArray(imageSize) { -1 }

        val weights = kotlin.DoubleArray(imageSize) { index ->
            val x = index % width
            val y = index / width
            pixelEnergy(x, y)
        }

        for (y in height - 2 downTo 0) {
            for (x in  0 until width) {
                val index = y * width + x

                val bottomIndex = index + width
                var minIndex = bottomIndex
                if (x > 0 && weights[bottomIndex - 1] < weights[minIndex]) {
                    minIndex = bottomIndex - 1
                }
                if (x < width - 1 && weights[bottomIndex + 1] < weights[minIndex]) {
                    minIndex = bottomIndex + 1
                }

                weights[index] += weights[minIndex]
                preceding[index] = minIndex
            }
        }

        var minIndex = 0
        for (index in 0 until width) {
            if (weights[index] < weights[minIndex]) {
                minIndex = index
            }
        }

        return Array(height) {
            val current = minIndex
            minIndex = preceding[minIndex]
            current % width
        }
    }
}

fun Image.drawSeam() {
    val seam = getVerticalSeam()
    for (y in seam.indices) {
        val x = seam[y]
        set(x, y, Color.RED)
    }
}

fun getMD5(path: String) =
        MessageDigest.getInstance("MD5")
                .digest(File(path).readBytes())
                .joinToString("") { "%02x".format(it) }

fun main(args: Array<String>) {
    var inFilename = "amsterdam-small.png"
    var outFilename = "amsterdam-small_seam.png"

    for (i in args.indices) {
        when(args[i]) {
            "-in" -> inFilename = args[i + 1]
            "-out" -> outFilename = args[i + 1]
        }
    }

    val image = Image(inFilename)

    image.drawSeam()

    image.save(outFilename)

    println("($outFilename) -> (${getMD5(outFilename)})")
}


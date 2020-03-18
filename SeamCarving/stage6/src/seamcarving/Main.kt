package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.security.MessageDigest
import java.time.Duration
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis


open class Image {
    private val image: BufferedImage

    open val width: Int
        get() = image.width

    open val height: Int
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

    constructor(image: BufferedImage) {
        this.image = image
    }

    open operator fun get(x: Int, y: Int): Color {
        if (x < 0 || x >= image.width) {
            throw IndexOutOfBoundsException("x must be between 0 and ${image.width - 1}")
        }
        if (y < 0 || y >= image.height) {
            throw IndexOutOfBoundsException("y must be between 0 and ${image.height - 1}")
        }
        return Color(image.getRGB(x, y))
    }

    open operator fun set(x: Int, y: Int, color: Color) {
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

    private class Transposed(image: Image) : Image(image.image) {
        override val width: Int
            get() = super.height

        override val height: Int
            get() = super.width

        override operator fun get(x: Int, y: Int): Color {
            return super.get(y, x)
        }

        override operator fun set(x: Int, y: Int, color: Color) {
            super.set(y, x, color)
        }
    }

    fun getHorizontalSeam() = Transposed(this).getVerticalSeam()

    fun reduce(width: Int, height: Int): Image {
        var result: Image = this

        repeat(width) {
            result = result.removeVerticalSeam()
        }

        repeat(height) {
            result = result.removeHorizontalSeam()
        }

        return result
    }

    fun drawVerticalSeam() {
        val seam = getVerticalSeam()
        for (y in seam.indices) {
            val x = seam[y]
            set(x, y, Color.RED)
        }
    }

    fun removeVerticalSeam() : Image {
        val seam = getVerticalSeam()
        return Image(width - 1, height) { x, y ->
            if (x < seam[y]) {
                get(x, y)
            }
            else {
                get(x + 1, y)
            }
        }
    }

    fun removeHorizontalSeam() : Image {
        val seam = getHorizontalSeam()
        return Image(width, height - 1) { x, y ->
            if (y < seam[x]) {
                get(x, y)
            }
            else {
                get(x, y + 1)
            }
        }
    }

    fun drawHorizontalSeam() {
        val seam = getHorizontalSeam()
        for (x in seam.indices) {
            val y = seam[x]
            set(x, y, Color.RED)
        }
    }
}



fun measure(block: () -> Unit): String {
    val duration = Duration.ofMillis(measureTimeMillis(block))

    val min = duration.toMinutesPart()
    val sec = duration.toSecondsPart()
    val mil = duration.toMillisPart()

    return "$min min. $sec sec. $mil ms."
}

fun main(args: Array<String>) {
    var inFilename = "amsterdam.png"
    var outFilename = "amsterdam_reduced.png"

    var widthReduce = 250
    var heightReduce = 100

    for (i in args.indices) {
        when(args[i]) {
            "-in" -> inFilename = args[i + 1]
            "-out" -> outFilename = args[i + 1]
            "-width" -> widthReduce = args[i + 1].toInt()
            "-height" -> heightReduce = args[i + 1].toInt()
        }
    }

    val image = Image(inFilename)

    var reduced: Image? = null

    println(measure {
        reduced = image.reduce(widthReduce, heightReduce)
    })

    reduced!!.save(outFilename)
}


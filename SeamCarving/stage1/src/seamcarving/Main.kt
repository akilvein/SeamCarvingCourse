package seamcarving

import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO


class Image {
    private val image: BufferedImage

    constructor(width: Int, height: Int, init: (Int, Int) -> Color) {
        image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until height) {
            for (x in 0 until width) {
                set(x, y, init(x, y))
            }
        }
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

    val width: Int
        get() = image.width

    val height: Int
        get() = image.height

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

    val graphics: Graphics
        get() = image.graphics
}


fun main(args: Array<String>) {
    val scanner = Scanner(System.`in`)
    println("Enter rectangle width:")
    val width = scanner.nextInt()
    println("Enter rectangle height:")
    val height = scanner.nextInt()
    println("Enter output image name:")
    val filename = scanner.next()

    val image = Image(width, height) { _, _ -> Color.BLACK }

    val g = image.graphics
    g.color = Color.RED
    g.drawLine(0, 0, image.width - 1, image.height - 1)
    g.drawLine(0, image.height - 1, image.width - 1, 0)

    image.save(filename)
}


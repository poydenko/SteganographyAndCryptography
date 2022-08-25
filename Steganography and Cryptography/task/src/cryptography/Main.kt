package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

//Project: Steganography and Cryptography
fun main() {
    var flag = true
    while (flag) {
        println("Task (hide, show, exit):")
        var input = readln()
        when (input) {
            "hide" -> hideImage()
            "show" -> showImage()
            "exit" -> {
                println("Bye!")
                flag = !flag
            }
            else -> println("Wrong task: $input")
        }
    }
}
//Hide message in the image
fun hideImage() {
    println("Input image file:")
    val inImageFilePath = readln()
    println("Output image file:")
    val outImageFilePath = readln()
    println("Message to hide:")
    val inMessage = readln()
    println("Password:")
    val password = readln()
    val encMessage = encodeMessage(inMessage, password)


    if (inImageFilePath != null && outImageFilePath != null) {

        val inImageFile = File(inImageFilePath)
        val outImageFile = File(outImageFilePath)
        println("Input Image: ${inImageFile.path.replace(File.separator, "\\")}")
        println("Output Image: ${outImageFile.path.replace(File.separator, "\\")}")
        try {
            val inputImage = ImageIO.read(inImageFile)
            val outputImage =
                BufferedImage(inputImage.width, inputImage.height, BufferedImage.TYPE_INT_RGB)
            changePixel(inputImage, outputImage, encMessage)
            ImageIO.write(outputImage, "png", outImageFile)
        } catch (e: Exception) {
            println(e.message)
        }
        println("Message saved in $outImageFilePath image.")
    }

}

//Encode the informaton, in last bit of blue color, in the image
fun changePixel(inputImage: BufferedImage, outputImage: BufferedImage, inMessage: String) {
    val inByteArray = inMessage.encodeToByteArray() + byteArrayOf(0, 0, 3)
    var inBitArray = mutableListOf<Int>()
    var inBitIndices = 0
    inByteArray.forEach {
        inBitArray.addAll(convertStrToIntArray(it.toString(2).padStart(8, '0')))
    }

    if (inBitArray.size <= (inputImage.width * inputImage.height)) {
        for (j in 0 until inputImage.height) {
            for (i in 0 until inputImage.width) {
                /* Logic */
                val color = Color(inputImage.getRGB(i, j))
                if (inBitIndices < inBitArray.size) {
                    outputImage.setRGB(
                        i, j, Color(
                            color.red, color.green,
                            color.blue.and(254).or(inBitArray[inBitIndices++])
                        ).rgb
                    )
                } else {
                    outputImage.setRGB(i, j, color.rgb)
                }
            }
        }
    } else println("The input image is not large enough to hold this message.")
}

//Decode information from image
fun showImage() {
    println("Input image file:")
    val inImageFilePath = readln()
    println("Password:")
    val password = readln()

    val inImageFile = File(inImageFilePath)
    var inBitArray = mutableListOf<Int>()
    try {
        val inputImage = ImageIO.read(inImageFile)
        for (j in 0 until inputImage.height) {
            for (i in 0 until inputImage.width) {
                // Logic
                val bit = Color(inputImage.getRGB(i, j)).blue.and(1)
                inBitArray.add(bit)
            }
        }
        println("Message:")
        val outMessage = convertIntArrayToString(inBitArray)
        println(encodeMessage(outMessage, password))
    } catch (e: Exception) {
        println(e.message)
    }
}

//Convert string to array of Int
private fun convertStrToIntArray(inByte: String): MutableList<Int> {
    val intList = mutableListOf<Int>()
    for (i in inByte.indices) {
        intList.add(inByte[i].digitToIntOrNull() ?: -1)
    }
    return intList
}

//Convert Int array to string
private fun convertIntArrayToString(inBitArray: MutableList<Int>): String {
    var outPhrase = inBitArray
        .joinToString(separator = "")
        .split("000000000000000000000011")
        .toTypedArray()[0].chunked(8)
        // Convert binary to string
        .map { binary -> binary.toInt(2) }
        .joinToString(separator = "", transform = Character::toString)
    return outPhrase
}

//Cut the string for a chunks
fun String.chunked(size: Int): List<String> {
    val nChunks = length / size
    return (0 until nChunks).map { substring(it * size, (it + 1) * size) }
}

//XOR for two strings
infix fun String.xor(that: String) = mapIndexed { index, c ->
    that[index].code.xor(c.code)
}.joinToString(separator = "") {
    it.toChar().toString()
}

//This function encode message with XOR, previously prepared password length
fun encodeMessage(message: String, password: String) : String {
    // Here the point is, to "copy" or "repeat" to be equal for message.length
    var passForEncription = ""
    if (password.length < message.length) {
        passForEncription = password.repeat(message.length/password.length + 1)
    } else passForEncription = password
    passForEncription = passForEncription.substring(0, message.length)
    return message xor passForEncription
}

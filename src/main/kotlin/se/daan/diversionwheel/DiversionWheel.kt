import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.apache.pdfbox.util.Matrix
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

const val PT_PER_INCH = 72f
const val CM_PER_INCH = 2.54f
const val SCALE = 1/250000f
const val M_PER_NM = 1852f
const val CM_PER_M = 100f

const val PT_PER_CM = PT_PER_INCH / CM_PER_INCH
const val PT_PER_NM = 1f * SCALE * M_PER_NM * CM_PER_M * PT_PER_CM

const val PAGE_WIDTH = 21f * PT_PER_CM
const val PAGE_HEIGHT = 29.7f * PT_PER_CM

const val RAD_PER_DEG = PI / 180f

fun main() {
    val flipped = false
    val speed = 107f
    val windDirection = 130f
    val windSpeed = 20f
    val variation = 2f

    val document = PDDocument()
    val font = PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN)

    val pdPage = PDPage(PDRectangle(PAGE_WIDTH, PAGE_HEIGHT))
    document.addPage(pdPage)

    val stream = PDPageContentStream(document, pdPage)
    stream.transform(
        Matrix(
            if(flipped) { -1f } else { 1f }, 0f,
            0f, 1f,
            PAGE_WIDTH / 2, PAGE_HEIGHT / 2
        )
    )

    val r4min = speed / 60f * 4f * PT_PER_NM
    stream.circle(r4min)
    stream.stroke()

    val r2min = speed / 60f * 2f * PT_PER_NM
    stream.circle(r2min)
    stream.stroke()

//    stream.circle(0.1f * PT_PER_CM)
//    stream.stroke()

    for(i in 0..355 step 5) {
        val angle = (i + variation) * RAD_PER_DEG
        stream.saveGraphicsState()

        stream.transform(
            Matrix(
                cos(angle).toFloat(), -sin(angle).toFloat(),
                sin(angle).toFloat(), cos(angle).toFloat(),
                0f, 0f
            )
        )

        val text = when {
            i == 0 -> "N"
            i == 90 -> "E"
            i == 180 -> "S"
            i == 270 -> "W"
            i % 30 == 0 -> (i / 10).toString()
            else -> null
        }

        val height = if(i % 10 == 0) {
            0.4f
        } else {
            0.2f
        }

        stream.moveTo(0f, r4min)
        stream.lineTo(0f, r4min - height * PT_PER_CM)
        stream.stroke()

        if(i % 10 == 0) {
            val height = if(i % 30 == 0) {
                0.4f
            } else {
                0.2f
            }

            stream.moveTo(0f, r2min)
            stream.lineTo(0f, r2min - height * PT_PER_CM)
            stream.stroke()
        }

        if(text != null) {
            val xOffset = -font.getStringWidth(text) / 1000f * 18f / 2f
            stream.beginText()
            stream.setFont(font, 18f)
            stream.setTextMatrix(
                Matrix(
                    1f, 0f,
                    0f, 1f,
                    xOffset, r4min - 1.0f * PT_PER_CM
                )
            )

            stream.showText(text)
            stream.endText()
            stream.fill()
        }
        stream.restoreGraphicsState()
    }

    val angle = windDirection * RAD_PER_DEG
    stream.saveGraphicsState()
    stream.transform(Matrix(
        cos(angle).toFloat(), -sin(angle).toFloat(),
        sin(angle).toFloat(), cos(angle).toFloat(),
        0f, 0f
    ))
    stream.transform(Matrix(
        1f, 0f,
        0f, 1f,
        0f, windSpeed / 60f * 4f * PT_PER_NM
    ))

    stream.circle(0.1f * PT_PER_CM)
    stream.stroke()
    stream.restoreGraphicsState()

    stream.saveGraphicsState()
    stream.transform(Matrix(
        cos(angle).toFloat(), -sin(angle).toFloat(),
        sin(angle).toFloat(), cos(angle).toFloat(),
        0f, 0f
    ))
    stream.transform(Matrix(
        1f, 0f,
        0f, 1f,
        0f, windSpeed / 60f * 2f * PT_PER_NM
    ))

    stream.circle(0.1f * PT_PER_CM)
    stream.stroke()
    stream.restoreGraphicsState()


    for (i in -8..8 step 2) {
        val x = i * PT_PER_NM
        stream.moveTo(x, -8 * PT_PER_NM)
        stream.lineTo(x, 8 * PT_PER_NM)
        stream.stroke()
        stream.moveTo(-8 * PT_PER_NM, x)
        stream.lineTo(8 * PT_PER_NM, x)
        stream.stroke()
    }

    stream.close()

    document.save(FileOutputStream("/tmp/diversion-wheel.pdf"))
    document.close()
}

fun PDPageContentStream.circle(radius: Float) {
    this.moveTo(radius, 0f)
    for (i in 1..360) {
        this.lineTo(radius * cos(i * RAD_PER_DEG).toFloat(), radius * sin(i * RAD_PER_DEG).toFloat())
    }
}
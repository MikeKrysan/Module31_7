package com.mikekrysan.module31_7

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.*

class ClockView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) : View(context, attributeSet) {
    private var radius: Float = 0f  //радиус будем получать в переопределенном методе onSizedChanged
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var scaleSize = 60f

    private var isStaticPictureDrawn: Boolean = false   //переменная отвечает за то, отрисована ли у нас статичная картинка
    private lateinit var bitmap: Bitmap
    private lateinit var staticCanvas: Canvas

    private var dashColor = Color.WHITE
    private var digitColor = Color.WHITE
    private var arrowColor = Color.RED

    private lateinit var paintClockCircle: Paint
    private lateinit var dashPaintThin: Paint
    private lateinit var clockPaint: Paint

    private val rect = Rect()

    private val numerals = arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 )

    init{
        initDrawingTools()
    }

    //В методе onMeasure получаем объекты MeasureSpec, в которых закодированы два параметра в Int: режим(Mode) и размер(Size)
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //Получаем режим и размер ширины и высоты:
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        //Далее на основании режима и размера получаем конкретную ширину и высоту, делаем это с помощью chooseDimension
        val chosenWidth = chooseDimension(widthMode, widthSize)
        val chosenHeight = chooseDimension(heightMode, heightSize)

        //В переменную minSide выбираем минимальное значение между шириной и высотой, это нужно для того, чтобы поскольку экран вытянут, то нужно чтобы циферблат был круглый, и чтобы он вписывался в меньшую сторону экрана
        val minSide = Math.min(chosenWidth, chosenHeight)
        centerX = minSide.div(2f)
        centerY = minSide.div(2f)

        //Полученные размеры применяем к view, для того, чтобы в полях нашего класса ClockView были эти значения
        setMeasuredDimension(minSide, minSide)
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        //получаем минимальную сторону, сравнивая ширину с высотой
        radius = if(width > height) {
            height.div(2f)
        } else {
            width.div(2f)
        }
    }

    //переопеделим метод onDraw():
    override fun onDraw(canvas:Canvas) {
        if(!isStaticPictureDrawn) {
            drawStaticPicture()
        }
        canvas.drawBitmap(bitmap, centerX - radius, centerY - radius, null)
        drawHands(canvas)

        postInvalidateDelayed(500)  //вызваем перерисовку часовых стрелок. Можно вызывать через invalidate и бесконечный цикл, но есть готовое решение - удобный метод postInvalidateDelayed(), - этот метод запускает метод invalidate через каждые 500мс в нашем случае
    }

    //рисуем стрелки часов
    private fun drawHands(canvas: Canvas) {
        canvas.save()
        //смещаем канвас в центр экрана, чтобы нам проще было отрисовывать наши стрелки по окружности
        canvas.translate(centerX, centerY)

        //создается календарь, так лучше не делать, потому что косвенно создается объект календаря в методе onDraw()
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)  //получаем час из нашего календаря

        drawHand(
            canvas,
            //значение, которое будет влиять на позицию нашей стрелки: (чем больше минут, тем ближе часовая стрелка к следующему часу)
            ((hour + calendar.get(Calendar.MINUTE) / 60.0) * 5f),
            //третим параметром приходит константа, которая отвечает за то, которую стрелку мы хотим нарисовать
            HOUR_HAND
        )
        drawHand(canvas, calendar.get(Calendar.MINUTE).toDouble(), MINUTE_HAND)
        drawHand(canvas, calendar.get(Calendar.SECOND).toDouble(), SECOND_HAND)

        canvas.restore()
    }

    private fun drawHand(canvas: Canvas, loc: Double, hand: Int) {
        //Первым действием создается краска
        val paintHands = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            isAntiAlias = true

            //в цикл when передаются цифры от 1 до 3 обозначающие соответсвующую стрелку
            //каждая стрелка будет разной толщины
            when (hand) {
                HOUR_HAND -> strokeWidth = scaleSize * 0.5f
                MINUTE_HAND -> strokeWidth = scaleSize * 0.3f
                SECOND_HAND -> {
                    strokeWidth = scaleSize * 0.2f
                    color = arrowColor
                }
            }
        }
        //Прощитываем угол
        val angle = Math.PI * loc / 30 - Math.PI / 2
        //длина стрелки зависит от радиуса циферблата
        val handRadius = if (hand == HOUR_HAND) {
            radius * 0.7
        } else {
            radius * 0.9
        }
        //отрисовываем на канвас при помощи метода drawLine
        canvas.drawLine(
            0f, 0f,
            //конечные координаты расчитываются по формуле:
            (Math.cos(angle) * handRadius).toFloat(),
            (Math.sin(angle) * handRadius).toFloat(),
            paintHands
        )
    }

    //создадим краски - объекты класса Paint
    private fun initDrawingTools() {
        dashPaintThin = Paint().apply {
            color = dashColor
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 0.01f
            isAntiAlias = true
        }
        clockPaint = Paint(dashPaintThin).apply {
            strokeWidth = 2f
            textSize = scaleSize * 1.5f
            color = digitColor
            isAntiAlias = true
        }
        paintClockCircle = Paint().apply {
            color = Color.BLACK
            strokeWidth = 10f
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }

    private fun drawStaticPicture() {
        //т.о создается bitmap:
        bitmap = Bitmap.createBitmap(
            (centerX *2 ).toInt(),
            (centerY * 2).toInt(),
            Bitmap.Config.ARGB_8888 //третий параметр -  конфигурация, по каким правилам и по каким цветам будет отрисовываться картинка
        )
        staticCanvas = Canvas(bitmap)   //это такой же canvas, какой приходит в метод onDraw(), но на нем мы будем рисовать статичную картинку
        drawClock(staticCanvas) //метод который рисует наш циферблат

        isStaticPictureDrawn = true
    }

    private fun drawClock(canvas: Canvas) {
        canvas.save()

        //смещаем начало координат в центр поля где будет располагаться View
        canvas.translate(centerX, centerY)

        canvas.drawCircle(0f, 0f, radius, paintClockCircle)

        //массив numerals отвечает за цифры на циферблате
        for (number in numerals) {
            val text = number.toString()    //получаем сначала нашу цифру
            val digitOffset = 0.9f      //указано смещение, на которое будет смещаться наша цифра

            clockPaint.getTextBounds(text, 0, text.length, rect)    //получаем границы символа, чтобы мы могли его выставить по центру

            //Делим окружность на сектора, в этом методе каждая цифра получает свой угол, и координата выставляется таким образом, чтобы это все было похоже на циферблат
            val angle = Math.PI / 6 * (number - 3)

            val x = (Math.cos(angle) * radius * digitOffset - rect.width() / 2).toFloat()
            val y = (Math.sin(angle) * radius * digitOffset + rect.height() / 2).toFloat()

            //по координатам которые мы получаем в цикле и рисуется наш текст
            canvas.drawText(text, x, y, clockPaint)
        }
    }


    private fun chooseDimension(mode: Int, size: Int) =
        when(mode)  {
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> size
            else -> 300
        }

    companion object {
        const val HOUR_HAND = 1
        const val MINUTE_HAND = 2
        const val SECOND_HAND = 3
    }
}
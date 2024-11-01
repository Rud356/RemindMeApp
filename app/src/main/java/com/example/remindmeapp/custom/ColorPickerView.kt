package com.example.remindmeapp

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout

class ColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var defaultWidth : Int = 35
    private var defaultHeight : Int = 35
    private var selectedImageView: ImageView? = null

    private val colorList = mutableListOf<String>()

    override fun onFinishInflate() {
        super.onFinishInflate()
        setupImageViews()
    }

    // Метод для установки слушателей для существующих ImageView
    private fun setupImageViews() {

        val child = getChildAt(0) as ImageView
        defaultWidth = child.layoutParams.width
        defaultHeight = child.layoutParams.height

        handleImageClick(getChildAt((childCount / 2).toInt()) as ImageView)

        for (i in 0 until childCount) {
            val child = getChildAt(i)

            if (child is ImageView) {
                colorList.add(getColor(child))
                child.setOnClickListener {
                    handleImageClick(child)
                }
            }
        }
    }

    private fun handleImageClick(imageView: ImageView) {
        // Если другой ImageView уже был выбран, возвращаем его к исходному размеру
        selectedImageView?.let {
            it.layoutParams = it.layoutParams.apply {
                width = defaultWidth // Исходный размер
                height = defaultHeight
            }
            it.requestLayout()
        }

        // Увеличиваем выбранный ImageView
        imageView.layoutParams = imageView.layoutParams.apply {
            width = (defaultWidth * 1.3).toInt()
            height = (defaultHeight * 1.3).toInt()
        }
        imageView.requestLayout()

        selectedImageView = imageView
    }

    fun setColor(color: String) {
        val index = colorList.indexOf(color)

        if (index == -1)
            return

        val imageView = getChildAt(index) as? ImageView
        imageView?.let {
            handleImageClick(it)
        }
    }

    fun getColor() : String {
        return getColor(selectedImageView!!)
    }

    private fun getColor(imageView: ImageView): String {
        val background = imageView.background
        if (background is GradientDrawable) {
            val colorStateList = background.color
            val colorInt = colorStateList?.defaultColor ?: 0
            val color = String.format("#%06X", 0xFFFFFF and colorInt)
            return color
        }
        return "#000000" // Если не нашли подходящий Drawable, возвращаем черный цвет
    }

}

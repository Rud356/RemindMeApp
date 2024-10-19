package com.example.remindmeapp

import android.content.Context
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

    fun getColor() : String {
        return getColorFromTag(selectedImageView!!)
    }

    private fun getColorFromTag(imageView: ImageView): String {
        val colorTag = imageView.tag as? String
        return colorTag ?: "#000000" // Если tag не установлен, возвращаем черный
    }
}

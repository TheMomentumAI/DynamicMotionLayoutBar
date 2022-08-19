package com.applover.dynamicmotionlayoutbar.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.Space
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getIntOrThrow
import androidx.core.widget.ImageViewCompat
import com.applover.dynamicmotionlayoutbar.R
import com.applover.dynamicmotionlayoutbar.utils.createConstraintSet

@Suppress("SpellCheckingInspection")
open class StepProgressBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : MotionLayout(context, attrs, defStyleAttr) {

    private var activeTint: Int = -1
    private var inactiveTint: Int = -1
    private var animationDuration = DEFAULT_ANIMATION_SPEED

    private val stepViews = mutableListOf<StepView>()
    private var inactiveBarId: Int = -1

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.StepProgressBarView,
            0,
            0,
        ).apply {
            try {
                activeTint = getColorOrThrow(R.styleable.StepProgressBarView_active_tint)
                inactiveTint = getColorOrThrow(R.styleable.StepProgressBarView_inactive_tint)
                animationDuration = getIntOrThrow(R.styleable.StepProgressBarView_duration)
            } finally {
                recycle()
            }
        }
    }

    fun initialize(steps: List<Step>) {
        resetViews()
        steps.forEach {
            stepViews.add(createStepView(it))
        }
        createInactiveBar()

        val constraintSet = createConstraintSet()
        constraintSet.createConstraints()
        constraintSet.applyTo(this)

        invalidate()
    }

    private fun resetViews() {
        removeAllViews()
        stepViews.clear()
    }

    private fun ConstraintSet.createConstraints() {
        createConstrainsForAllSteps()
        setConstraintsForAllAnchors()
        setConstraintsForInactiveBar()
    }

    private fun ConstraintSet.createConstrainsForAllSteps() {
        val viewIds = stepViews.map { it.imageViewId }.toIntArray()
        createHorizontalChain(ConstraintSet.PARENT_ID, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, viewIds, null, ConstraintSet.CHAIN_SPREAD)
        viewIds.forEach {
            connect(it, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(it, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        }
    }

    private fun ConstraintSet.setConstraintsForAllAnchors() {
        stepViews.forEach {
            connect(it.anchorViewId, ConstraintSet.TOP, it.imageViewId, ConstraintSet.BOTTOM)
            connect(it.anchorViewId, ConstraintSet.START, it.imageViewId, ConstraintSet.START)
            connect(it.anchorViewId, ConstraintSet.END, it.imageViewId, ConstraintSet.END)
        }
    }

    private fun ConstraintSet.setConstraintsForInactiveBar() {
        val firstAnchor = stepViews.first().anchorViewId
        val lastAnchor = stepViews.last().anchorViewId

        connect(inactiveBarId, ConstraintSet.TOP, firstAnchor, ConstraintSet.BOTTOM)
        connect(inactiveBarId, ConstraintSet.START, firstAnchor, ConstraintSet.START)
        connect(inactiveBarId, ConstraintSet.END, lastAnchor, ConstraintSet.END)
    }

    private fun ConstraintLayout.createInactiveBar() {
        val layoutParams = LayoutParams(LayoutParams.MATCH_CONSTRAINT, 4)
        layoutParams.setMargins(0, 16, 0, 16)

        val imageView = ImageView(context)
        val imageViewId = generateViewId()
        inactiveBarId = imageViewId
        imageView.id = imageViewId
        imageView.setImageResource(R.drawable.bar)
        ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(inactiveTint))
        addView(imageView, layoutParams)
    }

    private fun ConstraintLayout.createStepView(step: Step): StepView {
        val imageView = ActivableImageView(
            context,
            drawableRes = step.drawableRes,
            activeTint = step.activeTint,
            inactiveTint = step.inactiveTint,
            animationDuration = animationDuration
        )
        val imageViewId = generateViewId()
        imageView.id = imageViewId
        val layoutParams = LayoutParams(48, 48)
        addView(imageView, layoutParams)

        val layoutParamsWrapMarginMedium = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParamsWrapMarginMedium.setMargins(16, 16, 16, 16)

        val activableImageView = ActivableImageView(
            context,
            drawableRes = step.drawableRes,
            activeTint = step.activeTint,
            inactiveTint = step.inactiveTint,
            animationDuration = animationDuration
        )
        val activableImageViewId = generateViewId()
        activableImageView.id = activableImageViewId
        addView(activableImageView, layoutParamsWrapMarginMedium)

        val anchor = Space(context)
        val anchorId = generateViewId()
        anchor.id = anchorId
        addView(anchor, layoutParamsWrapMarginMedium)

        return StepView(imageViewId, anchorId, activableImageView)
    }

    private data class StepView(val imageViewId: Int, val anchorViewId: Int, private val activableImageView: ActivableImageView)

    data class Step(@DrawableRes val drawableRes: Int, @ColorRes val activeTint: Int, @ColorRes val inactiveTint: Int)

    companion object {
        private const val DEFAULT_ANIMATION_SPEED = 500
    }
}
package com.sendbird.android.sample.utils

import com.sendbird.android.sample.R

object SubjectImageUtils {

    fun getSubjectImageRes(subject: String, isActive: Boolean): Int {
        return when(subject.lowercase()) {
            "mathematics" -> if (isActive) R.drawable.ic_math_active else R.drawable.ic_math_inactive
            "physics" -> if (isActive) R.drawable.ic_physics_active else R.drawable.ic_physics_inactive
            "biology" -> if (isActive) R.drawable.ic_biology_active else R.drawable.ic_biology_inactive
            "chemistry" -> if (isActive) R.drawable.ic_chemistry_active else R.drawable.ic_chemistry_inactive
            else -> if (isActive) R.drawable.ic_math_active else R.drawable.ic_math_inactive
        }
    }
}
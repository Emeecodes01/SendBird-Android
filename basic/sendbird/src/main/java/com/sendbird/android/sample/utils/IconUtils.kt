package com.sendbird.android.sample.utils

import com.sendbird.android.sample.R

object IconUtils {

    fun getSubjectIconWithThemeKey(themeKey: String): Pair<Int, Int> {
        return when(themeKey) {
            "mathematics_english" -> Pair(R.drawable.ic_maths_fill, R.drawable.ic_maths_grey_fill)
            "physics_english" -> Pair(R.drawable.ic_physics_fill, R.drawable.ic_physics_grey_fill)
            "chemistry_english" -> Pair(R.drawable.ic_chemistry_fill, R.drawable.ic_chemistry_grey_fill)
            "biology_english" -> Pair(R.drawable.ic_biology_fill, R.drawable.ic_biology_grey_fill)
            "mathematics_english_jss" -> Pair(R.drawable.ic_maths_js_fill, R.drawable.ic_maths_js_grey_fill)
            "basic_technology_english" -> Pair(R.drawable.ic_basic_tech_fill, R.drawable.ic_basic_tech_grey_fill)
            "basic_science_english" -> Pair(R.drawable.ic_basic_science_fill, R.drawable.ic_basic_science_grey_fill)
            "business_studies_english" -> Pair(R.drawable.ic_business_studies_fill, R.drawable.ic_business_studies_grey_fill)
            "english_english" -> Pair(R.drawable.ic_english_fill, R.drawable.ic_english_grey_fill)
            "english_english_jss" -> Pair(R.drawable.ic_english_fill, R.drawable.ic_english_grey_fill)
            "primary_english_english" -> Pair(R.drawable.ic_english_primary_fill, R.drawable.ic_english_primary_grey_fill)
            "primary_mathematics_english" -> Pair(R.drawable.ic_maths_primary_fill, R.drawable.ic_maths_primary_grey_fill)
            "primary_basic_science_english" -> Pair(R.drawable.ic_basic_science_primary_fill, R.drawable.ic_basic_science_primary_grey_fill)
            else -> Pair(R.drawable.ic_maths_fill, R.drawable.ic_maths_grey_fill)
        }
    }
}
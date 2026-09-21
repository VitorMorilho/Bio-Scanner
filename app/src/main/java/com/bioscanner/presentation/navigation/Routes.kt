package com.bioscanner.presentation.navigation

object Routes {
    const val HOME = "home"
    const val CAPTURE = "capture"

    const val RESULT_ARG_IMAGE_URI = "imageUri"
    const val RESULT = "result/{$RESULT_ARG_IMAGE_URI}"
    fun result(encodedImageUri: String) = "result/$encodedImageUri"

    const val DETAIL_ARG_SPECIES_ID = "speciesId"
    const val DETAIL = "detail/{$DETAIL_ARG_SPECIES_ID}"
    fun detail(speciesId: String) = "detail/$speciesId"
}

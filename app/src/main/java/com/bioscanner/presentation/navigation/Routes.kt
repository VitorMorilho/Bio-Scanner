package com.bioscanner.presentation.navigation

object Routes {
    const val INTRO = "intro"
    const val HOME = "home"
    const val GLOSSARY = "glossary"
    const val RECORDS = "records"
    const val CAPTURE = "capture"
    const val AUTH = "auth"

    const val REGISTER_ARG_PHOTO_URI = "photoUri"
    const val REGISTER_ARG_FROM_CAMERA = "fromCamera"
    const val REGISTER = "register/{$REGISTER_ARG_PHOTO_URI}/{$REGISTER_ARG_FROM_CAMERA}"
    fun register(encodedPhotoUri: String, fromCamera: Boolean) = "register/$encodedPhotoUri/$fromCamera"

    const val OBSERVATION_ARG_ID = "observationId"
    const val OBSERVATION = "observation/{$OBSERVATION_ARG_ID}"
    fun observation(id: Long) = "observation/$id"

    const val DETAIL_ARG_SPECIES_ID = "speciesId"
    const val DETAIL = "detail/{$DETAIL_ARG_SPECIES_ID}"
    fun detail(speciesId: String) = "detail/$speciesId"
}

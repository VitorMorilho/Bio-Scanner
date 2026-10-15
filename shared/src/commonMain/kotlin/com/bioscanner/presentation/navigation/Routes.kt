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

/**
 * Codifica um valor para caber num segmento de rota (como o Uri.encode do Android): a URI da foto
 * tem "/" e ":". O Navigation devolve o argumento já decodificado no SavedStateHandle.
 */
fun encodeRouteArgument(value: String): String = buildString {
    for (byte in value.encodeToByteArray()) {
        val c = byte.toInt().toChar()
        if (c.isLetterOrDigit() && c.code < 128 || c in "-_.!~*'()") {
            append(c)
        } else {
            append('%')
            append(HEX[(byte.toInt() shr 4) and 0xF])
            append(HEX[byte.toInt() and 0xF])
        }
    }
}

private const val HEX = "0123456789ABCDEF"

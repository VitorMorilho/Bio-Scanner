package com.bioscanner.domain.model

/**
 * Padrão de cor visível da espécie, usado no filtro do Glossário. Uma espécie
 * pode ter mais de um (ex.: uma coral é vermelha e anelada; uma cobra-verde
 * pode ser verde e uniforme).
 *
 * O nome da constante é o que vai no seed (tools/data/build_species_data.py
 * converte os valores em português do CSV) — renomear exige mudar o script.
 */
enum class ColorPattern(val label: String) {
    GREEN("Verde"),
    UNIFORM("Uniforme"),
    LINED("Lineado"),
    RED_CORAL("Vermelho / coral"),
    VARIEGATED("Variegado"),
}

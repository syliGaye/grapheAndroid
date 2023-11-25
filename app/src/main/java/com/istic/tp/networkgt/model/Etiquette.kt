package com.istic.tp.networkgt.model

import com.google.gson.annotations.SerializedName

data class Etiquette(
    @SerializedName("valeur") var nom: String,
    @SerializedName("position") var position: Position,
    @SerializedName("taille") var taille: Float
)
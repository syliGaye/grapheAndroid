package com.istic.tp.networkgt.model

import com.google.gson.annotations.SerializedName

data class Couleur(@SerializedName("nom") var nom: String,
                   @SerializedName("valeur") var valeur: Int)
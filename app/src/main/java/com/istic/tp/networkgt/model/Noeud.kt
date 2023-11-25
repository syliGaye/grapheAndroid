package com.istic.tp.networkgt.model

import com.google.gson.annotations.SerializedName
import com.istic.tp.networkgt.R

data class Noeud(@SerializedName("id") var id: Int,
                 @SerializedName("etiquette") var etiquette: Etiquette,
                 @SerializedName("position") var position: Position,
                 @SerializedName("couleur") var couleur: Couleur,
                 @SerializedName("taille") var taille: Float,
                 @SerializedName("imageResourceId") var imageResourceId: Int = R.drawable.house,
                 @SerializedName("isImageNode") var isImageNode: Boolean = false)
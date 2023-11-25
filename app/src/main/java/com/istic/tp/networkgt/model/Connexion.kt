package com.istic.tp.networkgt.model

import com.google.gson.annotations.SerializedName

data class Connexion(@SerializedName("noeudDepart") var noeudDepart: Noeud,
                     @SerializedName("noeudArrive") var noeudArrive: Noeud,
                     @SerializedName("etiquette") var etiquette: Etiquette,
                     @SerializedName("couleur") var couleur: Couleur,
                     @SerializedName("taille") var taille: Float,
                     @SerializedName("courbure") var courbure: Float = 0.0f)
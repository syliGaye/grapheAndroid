package com.istic.tp.networkgt.model

import com.google.gson.annotations.SerializedName

data class Position(@SerializedName("abscisse") var abscisse: Float,
                    @SerializedName("ordonnee") var ordonnee: Float)
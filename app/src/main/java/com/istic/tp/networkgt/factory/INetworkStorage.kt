package com.istic.tp.networkgt.factory

import com.istic.tp.networkgt.model.Graph

interface INetworkStorage {
    /**
     * Sauvegarde le graphe actuel dans la mémoire interne du téléphone.
     * @param graph Le graphe actuel (réquis).
     */
    fun save(graph: Graph)

    /**
     * Charge le graphe sauvegardé dans la mémoire interne du téléphone
     */
    fun load(): Graph
}
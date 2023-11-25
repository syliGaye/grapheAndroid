package com.istic.tp.networkgt.model

enum class GraphMode {
    /**
     * Initialiser le Graphe
     */
    INIT,

    /**
     * Ajouter un noeud
     */
    ADD_NODE,

    /**
     * Ajouter une connexion
     */
    ADD_CONNECTION,

    /**
     * Déplacer un noeud (éventuellement avec ses connexions)
     */
    MOVE_NODE,

    /**
     * Modifier un graphe
     */
    UPDATE_GRAPH,

    /**
     * Sauvegarder un graphe dans la mémoire interne (en un fichier JSON)
     */
    SAVE_GRAPH,

    /**
     * Afficher un graphe sauvegardé dans la mémoire interne
     */
    OPEN_GRAPH,

    /**
     * Envoyer le graphe par mail
     */
    SEND_GRAPH
}
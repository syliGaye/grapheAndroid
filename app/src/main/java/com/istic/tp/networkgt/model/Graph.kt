package com.istic.tp.networkgt.model

import com.google.gson.annotations.SerializedName

data class Graph(@SerializedName("noeuds") var noeuds: MutableList<Noeud> = mutableListOf(),
                 //@SerializedName("couleur") var couleur: Couleur,
                 @SerializedName("connexions") var connexions: MutableList<Connexion> = mutableListOf()
) {
    private var noeudId = 1

    fun ajouteNoeud(noeud : Noeud?) {
        if (noeud != null && noeud.id != 0) noeuds.add(noeud)
    }

    fun existeNoeud(noeud: Noeud): Boolean{
        return this.noeuds?.find { (it.position.abscisse + it.taille) == (noeud.position.abscisse + noeud.taille) && (it.position.ordonnee + it.taille) == (noeud.position.ordonnee + noeud.taille) } != null
    }

    fun existeConnexion(connexion: Connexion): Boolean{
        return this.connexions?.find {
            it.noeudDepart.position.abscisse == connexion.noeudDepart.position.abscisse && it.noeudDepart.position.ordonnee == connexion.noeudDepart.position.ordonnee && it.noeudArrive.position.abscisse == connexion.noeudArrive.position.abscisse && it.noeudArrive.position.ordonnee == connexion.noeudArrive.position.ordonnee
        } != null
    }

    fun supprimerNoeud(noeud: Noeud) {
        // Remove the node from the list if it exists
        val removed = noeuds.remove(noeud)

        // If the node was removed, update any connections in the graph
        if (removed) {
            // Remove connections related to the deleted node
            connexions.removeAll { it.noeudDepart == noeud || it.noeudArrive == noeud }

            // Update connections that reference other nodes in the graph
            connexions.forEach { connexion ->
                if (connexion.noeudDepart == noeud || connexion.noeudArrive == noeud) {
                    connexions.remove(connexion)
                }
            }
        }
    }
    fun supprimerConnexion(connexion: Connexion) {
        // Remove the connection from the list if it exists
        connexions.remove(connexion)
    }
}
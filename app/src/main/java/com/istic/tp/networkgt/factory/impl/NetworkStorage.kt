package com.istic.tp.networkgt.factory.impl

import android.content.Context
import android.graphics.Color
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.istic.tp.networkgt.factory.INetworkStorage
import com.istic.tp.networkgt.model.Couleur
import com.istic.tp.networkgt.model.Graph
import java.io.File
import java.io.IOException

class NetworkStorage(private val context: Context): INetworkStorage {
    companion object{
        private const val FILENAME = "graph_data.json"
    }

    override fun save(graph: Graph) {
        try {
            val gson = Gson()
            val json = gson.toJson(graph)
            context.openFileOutput(FILENAME, Context.MODE_PRIVATE).use { output ->
                output.write(json.toByteArray())
                Toast.makeText(context, "Fichier téléchargé", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Toast.makeText(context, "Erreur dans la sauvegarde du fichier", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun load(): Graph {
        val gson = Gson()
        val graphType = object : TypeToken<Graph>() {}.type
        try {
            val file = File(context.filesDir, FILENAME)
            if (file.exists()) {
                val json = file.readText()
                return gson.fromJson(json, graphType)
                Toast.makeText(context, "Fichier chargé", Toast.LENGTH_SHORT).show()
            }
            else Toast.makeText(context, "Fichier introuvable", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(context, "Erreur dans le chargement du fichier", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
        return Graph(noeuds = mutableListOf(), connexions = mutableListOf())
    }
}
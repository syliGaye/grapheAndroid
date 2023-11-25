package com.istic.tp.networkgt

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.istic.tp.networkgt.factory.impl.MailSenderService
import com.istic.tp.networkgt.factory.impl.NetworkStorage
import com.istic.tp.networkgt.model.GraphMode
import com.istic.tp.networkgt.view.GraphView
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


class MainActivity : AppCompatActivity() {
    companion object{
        private const val REQUEST_SCREENSHOT = 1
        private const val IMAGE_NAME = "screenshot.jpg"
    }

    private lateinit var graphView : GraphView
    private lateinit var networkStorage: NetworkStorage
    private var mainMode: GraphMode
    private var message: String = ""
    private lateinit var container: FrameLayout
    private lateinit var rootView: View
    // on below line we are creating variable for our view.
    lateinit var containerRL: RelativeLayout

    init {
        mainMode = GraphMode.INIT
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rootView = findViewById<View>(android.R.id.content)
        graphView = GraphView(this)
        networkStorage = NetworkStorage(this)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        title = "Network Gaye/Tien"

        container = findViewById<FrameLayout>(R.id.contener)
        container.addView(graphView)

        containerRL = findViewById(R.id.idRLContainer)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_restart -> {
                mainMode = GraphMode.INIT  // On réinitialise le graphe
                initGraph()
                message = getString(R.string.init_graph_msg)
                showToast(message)
            }
            R.id.submenu_ajoutObjet -> {
                mainMode = GraphMode.ADD_NODE  // On passe en mode "Ajout de Noeud"
                message = getString(R.string.add_node_msg)
                showToast(message)
            }
            R.id.submenu_ajoutConnexion -> {
                mainMode = GraphMode.ADD_CONNECTION  // On passe en mode "Ajout de Connexion"
                message = getString(R.string.add_connection_msg)
                showToast(message)
            }
            R.id.submenu_moveNode -> {
                mainMode = GraphMode.MOVE_NODE
                message = getString(R.string.move_node_msg)
                showToast(message)
            }
            R.id.submenu_modif -> {
                mainMode = GraphMode.UPDATE_GRAPH  // On passe en mode "Modification de Noeud"
                message = getString(R.string.update_graph_msg)
                showToast(message)
            }
            R.id.submenu_save -> {
                mainMode = GraphMode.SAVE_GRAPH
                networkStorage.save(graphView.getGraph())
            }
            R.id.submenu_open -> {
                mainMode = GraphMode.OPEN_GRAPH
                graphView.setGraph(networkStorage.load())
            }
            R.id.submenu_send -> {
                mainMode = GraphMode.SEND_GRAPH
                if (!graphView.isGraphEmpty()){
                    rootView.postDelayed({
                        storeImage(loadBitmapFromView(container))
                    }, 1000)
                    val mailSenderService = MailSenderService(this)
                    mailSenderService.send(getOutputMediaFile(IMAGE_NAME))
                } else showToast("Aucun graphe à envoyer par mail.")
            }
            R.id.menu_changeplan -> {
                showChangePlanDialog()
            }
            else -> {super.onOptionsItemSelected(item)}
        }
        graphView.setMode(mainMode)

        return super.onOptionsItemSelected(item)
    }

    private fun showChangePlanDialog() {
        val plans = arrayOf("Plan 1", "Plan 2", "Plan 3")
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.menu_changeplan))

        // Set up the Spinner
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, plans)

        builder.setView(spinner)

        // Set up the buttons
        builder.setPositiveButton(getString(R.string.ok_btn)) { _, _ ->
            val selectedPlan = spinner.selectedItem as String
            when (selectedPlan) {
                "Plan 1" -> containerRL.background = resources.getDrawable(R.drawable.planappartement)
                "Plan 2" -> containerRL.background = resources.getDrawable(R.drawable.planappartement2)
                "Plan 3" -> containerRL.background = resources.getDrawable(R.drawable.planappartement3)
                else -> containerRL.background = resources.getDrawable(R.drawable.planappartement)
            }
        }
        builder.setNegativeButton(getString(R.string.cancel_btn)) { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun initGraph(){
        graphView.clearGraph()
        container.removeAllViews()
        container.addView(graphView)
    }

    private fun loadBitmapFromView(v: View): Bitmap {
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        val bgDrawable = v.background
        if (bgDrawable != null) bgDrawable.draw(c)
        else c.drawColor(Color.WHITE)

        v.draw(c)
        Log.i("loadBitmapFromView", "La vue est ${b.height}")
        return b
    }

    private fun storeImage(image: Bitmap) {
        val pictureFile: File? = getOutputMediaFile(IMAGE_NAME)
        if (pictureFile == null) {
            Log.d(
                "storeImage",
                "Error creating media file, check storage permissions: "
            )
            return
        }
        try {
            val fos = FileOutputStream(pictureFile)
            image.compress(Bitmap.CompressFormat.PNG, 10, fos)
            fos.close()
            Log.i("storeImage", "File found: ${image.byteCount}")
        } catch (e: FileNotFoundException) {
            Log.d("storeImage", "File not found: " + e.message)
        } catch (e: IOException) {
            Log.d("storeImage", "Error accessing file: " + e.message)
        }
    }

    /** Create a File for saving an image or video  */
    private fun getOutputMediaFile(filename: String): File? {
        val dir = this.getExternalFilesDir(null)

        Log.i("getOutputMediaFile", if (dir != null) dir.absolutePath else "rien dir")


        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (dir == null) return null
        else if (!dir.exists()) {
            if (!dir.mkdirs()) return null
        }
        return File(dir.path + File.separator + filename)
    }
}
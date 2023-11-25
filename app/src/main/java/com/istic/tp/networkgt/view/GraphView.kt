package com.istic.tp.networkgt.view

import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.istic.tp.networkgt.R
import com.istic.tp.networkgt.model.Connexion
import com.istic.tp.networkgt.model.Couleur
import com.istic.tp.networkgt.model.Etiquette
import com.istic.tp.networkgt.model.Graph
import com.istic.tp.networkgt.model.GraphMode
import com.istic.tp.networkgt.model.Noeud
import com.istic.tp.networkgt.model.Position
import kotlin.math.pow

class GraphView(context: Context): View(context), View.OnLongClickListener {
    private var couleur = Couleur("Bleu", Color.BLUE)
    private var graph = Graph(noeuds = mutableListOf(), connexions = mutableListOf())
    // Créez un objet Paint pour personnaliser l'apparence de votre vue
    var paint = Paint()

    private var connexion: Connexion? = null
    private var noeud: Noeud? = null
    private var tap: Position? = null
    private var mode: GraphMode = GraphMode.INIT
    private val rayon = 30f  // Rayon du Noeud
    private val epaisseur = 10f  // Epaisseur des connexions
    private var lastNodePosition = FloatArray(2)

    init {
        /**
         * Personnaliser l'apparence de notre vue (Noeud)
         */
        paint.color = couleur.valeur  // Prendre la couleur attribuée à notre graphe
        paint.style = Paint.Style.FILL

        setOnLongClickListener(this)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val mode_1 = this.mode

        when(mode_1){
            GraphMode.ADD_NODE -> {  // Nous sommes en mode ajout de Nœuds
                if (event.action == MotionEvent.ACTION_DOWN){  // Lorsque le doigt/stylet est enfoncé sur l'écran
                    // On récupère les coordonnées de l'évènement
                    lastNodePosition[0] = event.x
                    lastNodePosition[1] = event.y
                }
            }
            GraphMode.ADD_CONNECTION -> {  // Nous somme en mode ajout de connexions
                if (event.action == MotionEvent.ACTION_DOWN) this.noeud = this.getNode(event, graph)

                if (event.action == MotionEvent.ACTION_MOVE){
                    if (this.noeud != null) this.tap = Position(event.x, event.y)
                }

                if (event.action == MotionEvent.ACTION_UP){
                    val noeud_2 = this.getNode(event, graph)
                    val noeud_1 = this.noeud
                    if (noeud_1 != null && noeud_2 != null && noeud_1.id != noeud_2.id) this.createConnection(noeud_1, noeud_2)
                    this.tap = null
                    this.noeud = null
                }

            }
            GraphMode.MOVE_NODE -> {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    this.noeud = this.getNode(event, graph)
                    this.connexion = this.getConnexion(event, graph)
                }

                if (event.action == MotionEvent.ACTION_MOVE) {
                    val _noeud = this.noeud
                    val _connexion = this.connexion
                    if (_noeud != null) {
                        _noeud.position = Position(event.x, event.y)
                        _noeud.position = this.stayWithinScreenBoundaries(_noeud.position)

                        updateConnectionEtiquettePositions(_noeud)

                        this.noeud = _noeud
                    }
                    else if (_connexion != null){
                        _connexion.etiquette.position = Position(event.x, event.y)
                        _connexion.courbure = calculateCurvature(_connexion)
                        this.connexion = _connexion
                    }

                }

                if (event.action == MotionEvent.ACTION_UP){
                    val _noeud = this.noeud
                    val _connexion = this.connexion
                    if (_noeud != null) {
                        graph.noeuds?.find { it.id == _noeud.id }?.position = Position(_noeud.position.abscisse, _noeud.position.ordonnee)
                    }

                    this.noeud = null
                    this.connexion = null
                }

            }
            GraphMode.UPDATE_GRAPH -> {
                if (event.action == MotionEvent.ACTION_DOWN) {  // Lorsque le doigt/stylet est enfoncé sur l'écran
                    // On récupère les coordonnées de l'évènement
                    lastNodePosition[0] = event.x
                    lastNodePosition[1] = event.y

                    this.noeud = getNode(event, graph)
                    this.connexion = getConnexion(event, graph)
                }
            }
            else -> {}
        }
        invalidate()

        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val _noeud = this.noeud
        val _tap = this.tap
        val lesNoeud = this.graph.noeuds
        val lesConnections = this.graph.connexions
        val textPaint = Paint()

        textPaint.color = Color.BLACK
        textPaint.textSize = 30f

        if (lesNoeud.isNotEmpty()){

            lesNoeud.forEach { noeud ->
                val imageResourceId = noeud.imageResourceId
                val isImageNode = noeud.isImageNode
                val etiquette = drawText(noeud.etiquette.nom, noeud.position, noeud.taille, textPaint)

                canvas?.drawText(etiquette.nom, etiquette.position.abscisse, etiquette.position.ordonnee, textPaint)
                noeud.etiquette = etiquette

                if (isImageNode) {
                    val imageDrawable = ContextCompat.getDrawable(context, imageResourceId)
                    imageDrawable?.setBounds(
                        (noeud.position.abscisse - noeud.taille).toInt(),
                        (noeud.position.ordonnee - noeud.taille).toInt(),
                        (noeud.position.abscisse + noeud.taille).toInt(),
                        (noeud.position.ordonnee + noeud.taille).toInt()
                    )
                    if (canvas != null) {
                        imageDrawable?.draw(canvas)
                    }
                }
                else {
                    paint.color = noeud.couleur.valeur
                    paint.style = Paint.Style.FILL
                    canvas?.drawCircle(
                        noeud.position.abscisse,
                        noeud.position.ordonnee,
                        noeud.taille,
                        paint
                    )
                }
            }
            if (lesConnections.isNotEmpty()){
                lesConnections.forEach {
                    paint.color = it.couleur.valeur
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = it.taille

                    val etiquette = drawText(it.etiquette.nom, it.etiquette.position, it.etiquette.taille, textPaint)
                    canvas?.drawText(etiquette.nom, etiquette.position.abscisse, etiquette.position.ordonnee, textPaint)

                    if (it.courbure != 0f) drawCurvedLine(canvas, it, paint)
                    else canvas?.drawLine(it.noeudDepart.position.abscisse, it.noeudDepart.position.ordonnee, it.noeudArrive.position.abscisse, it.noeudArrive.position.ordonnee, paint)
                }
            }
        }

        if (_noeud != null && _tap != null) {
            val tmpPaint = Paint()
            tmpPaint.color = Color.GRAY
            tmpPaint.style = Paint.Style.FILL
            tmpPaint.strokeWidth = 10F

            canvas?.drawLine(
                _noeud.position.abscisse,
                _noeud.position.ordonnee,
                _tap.abscisse,
                _tap.ordonnee,
                tmpPaint
            )
        }
    }

    private fun getNode(event: MotionEvent, graph: Graph): Noeud?{
        val minX = event.x - rayon
        val maxX = event.x + rayon
        val minY = event.y - rayon
        val maxY = event.y + rayon

        return graph.noeuds.findLast { ((it.position.abscisse <= maxX) && (it.position.abscisse >= minX)) && ((it.position.ordonnee <= maxY) && (it.position.ordonnee >= minY)) }
    }

    private fun getConnexion(event: MotionEvent, graph: Graph): Connexion? {
        return graph.connexions.findLast {
            val etiquettePosition = it.etiquette.position
            val clickedX = event.x
            val clickedY = event.y
            val etiquetteX = etiquettePosition.abscisse
            val etiquetteY = etiquettePosition.ordonnee

            val distance = calculateDistance(clickedX, clickedY, etiquetteX, etiquetteY)
            distance <= rayon + 20
        }
    }

    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return kotlin.math.sqrt((dx * dx) + (dy * dy))
    }

    private fun calculateDistance(point1: Position, point2: Position): Float {
        return kotlin.math.sqrt((point2.abscisse - point1.abscisse).pow(2) + (point2.ordonnee - point1.ordonnee).pow(2))
    }

    fun setMode(graphMode: GraphMode){
        this.mode = graphMode
    }

    override fun onLongClick(p0: View?): Boolean {
        if (p0 != null){
            val _mode = this.mode

            when(_mode){
                GraphMode.ADD_NODE -> {
                    this.createNode()
                    this.noeud = null
                }
                GraphMode.UPDATE_GRAPH -> {
                    if (this.noeud != null) {
                        openNodeMenu(this.noeud!!)
                    } else if(this.connexion !=null) {
                        openConnexionMenu(this.connexion)
                    }
                    this.noeud = null
                }
                else -> {}
            }
        }
        return true
    }

    private fun createNode() {
        val i = graph.noeuds.size + 1
        buildText(i)
    }

    private fun buildText(index: Int){
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.node_name_attr))
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText("${context.getString(R.string.node_name_real)} $index") // Set default name

        builder.setView(input)

        builder.setPositiveButton(context.getString(R.string.ok_btn)) { dialog, which ->
            val nomNoeud = "${input.text}"
            addNodeToGraph(index, nomNoeud)
        }

        builder.setNegativeButton(context.getString(R.string.cancel_btn)) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun addNodeToGraph(index: Int, nodeName: String){
        if (nodeName.isNotEmpty()) {
            val etiquette = Etiquette(nodeName, Position(0f, 0f), 0f)
            this.noeud = Noeud(index, etiquette, stayWithinScreenBoundaries(Position(lastNodePosition[0], lastNodePosition[1])), couleur, rayon)
            val _noeud = this.noeud
            if (_noeud != null && !this.graph.existeNoeud(_noeud)) graph.ajouteNoeud(this.noeud)  // On ajoute le noeud créé à notre graphe
            invalidate()
        }
    }

    private fun createConnection(node1: Noeud, node2: Noeud) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.connection_name_attr))

        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText("${context.getString(R.string.connection_name_real)} ${graph.connexions.size + 1}") // Set default name

        builder.setView(input)

        builder.setPositiveButton(context.getString(R.string.ok_btn)) { _, _ ->
            val connectionName = "${input.text}"
            addConnectionToGraph(connectionName, node1, node2)
        }

        builder.setNegativeButton(context.getString(R.string.cancel_btn)) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun addConnectionToGraph(connectionName: String, node1: Noeud, node2: Noeud) {
        val newConnection = Connexion(node1, node2, calculateEtiquettePoint(connectionName, node1, node2), couleur, epaisseur)

        if (!graph.existeConnexion(newConnection)) {
            graph.connexions.add(newConnection)
            paint.strokeWidth = newConnection.taille
        }

        this.tap = null
        this.noeud = null
        invalidate()
    }

    private fun calculateEtiquettePoint(connectionName: String, node1: Noeud, node2: Noeud): Etiquette {
        val middleX = (node1.position.abscisse + node2.position.abscisse) / 2
        val middleY = (node1.position.ordonnee + node2.position.ordonnee) / 2

        return Etiquette(connectionName, Position(middleX, middleY), 0f)
    }

    private fun updateConnectionEtiquettePositions(node: Noeud) {
        val connectionsToUpdate = graph.connexions.filter { it.noeudDepart == node || it.noeudArrive == node }

        connectionsToUpdate.forEach {
            it.etiquette = calculateEtiquettePoint(it.etiquette.nom, it.noeudDepart, it.noeudArrive)
        }
    }

    private fun openNodeMenu(node: Noeud) {
        // Create a PopupWindow with a custom layout for the menu
        val popupView = LayoutInflater.from(context).inflate(R.layout.noeud_window, null)

        // Customize the UI elements in the popup menu
        val nodeNameTextView: TextView = popupView.findViewById(R.id.nodeNameTextView)
        nodeNameTextView.text = node.etiquette.nom

        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        // Buttons
        val btnSupprimer: Button = popupView.findViewById(R.id.btnSupprimer)
        val btnEtiquette: Button = popupView.findViewById(R.id.btnEtiquette)
        val btnCouleur: Button = popupView.findViewById(R.id.btnCouleur)
        val btnImages: Button = popupView.findViewById(R.id.btnImages)

        // Set click listeners for the buttons
        btnSupprimer.setOnClickListener {
            graph.supprimerNoeud(node)
            invalidate()
            popupWindow.dismiss()
        }

        btnEtiquette.setOnClickListener {
            showChangeNameDialog(node)
            popupWindow.dismiss()
        }

        btnCouleur.setOnClickListener {
            showChangeColorDialog(node)
            popupWindow.dismiss()
        }

        btnImages.setOnClickListener {
            showChangeImageDialog(node)
            popupWindow.dismiss()
        }

        popupWindow.showAtLocation(this, Gravity.CENTER, 0, 0)
    }

    private fun showChangeNameDialog(node: Noeud) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.node_name_attr))

        val input = EditText(context)
        input.setText(node.etiquette.nom)
        builder.setView(input)

        builder.setPositiveButton(context.getString(R.string.ok_btn)) { _, _ ->
            val newName = input.text.toString()
            node.etiquette.nom = newName
            invalidate()
        }

        builder.setNegativeButton(context.getString(R.string.cancel_btn)) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun showChangeColorDialog(node: Noeud) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.node_color_attr))

        // Create a spinner with a list of color options
        val colors = arrayOf(
            context.getString(R.string.red),
            context.getString(R.string.green),
            context.getString(R.string.blue),
            context.getString(R.string.orange),
            context.getString(R.string.cyan),
            context.getString(R.string.magenta),
            context.getString(R.string.black))
        val colorSpinner = Spinner(context)
        colorSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, colors)

        builder.setView(colorSpinner)

        builder.setPositiveButton(context.getString(R.string.ok_btn)) { _, _ ->
            // Get the selected color from the spinner
            val selectedColor = colors[colorSpinner.selectedItemPosition]

            // Update the node's color based on the selected color
            node.couleur = when (selectedColor) {
                context.getString(R.string.red) -> Couleur("Red", Color.RED)
                context.getString(R.string.green) -> Couleur("Green", Color.GREEN)
                context.getString(R.string.blue) -> Couleur("Blue", Color.BLUE)
                context.getString((R.string.orange))-> Couleur("Orange", Color.rgb(255, 165, 0))
                context.getString(R.string.cyan) -> Couleur("Cyan", Color.CYAN)
                context.getString((R.string.magenta)) -> Couleur("Magenta", Color.MAGENTA)
                context.getString(R.string.black) -> Couleur("Black", Color.BLACK)
                else -> node.couleur // Default to the current color if not recognized
            }

            invalidate()
        }

        builder.setNegativeButton(context.getString(R.string.cancel_btn)) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun showChangeImageDialog(node: Noeud) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.node_image_attr))

        // Create a spinner with a list of image options
        val imageOptions = arrayOf(
            context.getString(R.string.house),
            context.getString(R.string.printer),
            context.getString(R.string.television),
            context.getString(R.string.node))
        val imageSpinner = Spinner(context)
        imageSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, imageOptions)

        builder.setView(imageSpinner)

        builder.setPositiveButton(context.getString(R.string.ok_btn)) { _, _ ->
            // Get the selected image option from the spinner
            val selectedImageOption = imageOptions[imageSpinner.selectedItemPosition]

            // Map the selected image option to the corresponding resource ID
            val imageResourceId = when (selectedImageOption) {
                context.getString(R.string.house) -> R.drawable.house
                context.getString(R.string.printer) -> R.drawable.printer
                context.getString(R.string.television) -> R.drawable.television
                else -> R.drawable.house // Default to a default image if not recognized
            }

            // Update the node's image with the selected image resource ID
            node.imageResourceId = imageResourceId

            // Set the isImageNode flag to true
            node.isImageNode = true

            when (selectedImageOption) {
                context.getString(R.string.node) -> node.isImageNode = false
            }

            invalidate()
        }

        builder.setNegativeButton(context.getString(R.string.cancel_btn)) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun openConnexionMenu(connexion: Connexion?) {
        if (connexion != null) {
            // Create a PopupWindow with a custom layout for the menu
            val popupView = LayoutInflater.from(context).inflate(R.layout.connexion_window, null)

            // Customize the UI elements in the popup menu
            val connexionNameTextView: TextView = popupView.findViewById(R.id.connexionNameTextView)
            connexionNameTextView.text = connexion.etiquette.nom

            val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

            // Buttons
            val btnSupprimer: Button = popupView.findViewById(R.id.btnSupprimer)
            val btnEtiquette: Button = popupView.findViewById(R.id.btnEtiquette)
            val btnCouleur: Button = popupView.findViewById(R.id.btnCouleur)
            val btnEpaisseur: Button = popupView.findViewById(R.id.btnEpaisseur)

            // Set click listeners for the buttons
            btnSupprimer.setOnClickListener {
                graph.supprimerConnexion(connexion)
                invalidate()
                popupWindow.dismiss()
            }

            btnEtiquette.setOnClickListener {
                showChangeNameDialogConnexion(connexion)
                popupWindow.dismiss()
            }

            btnCouleur.setOnClickListener {
                showChangeColorDialogConnexion(connexion)
                popupWindow.dismiss()
            }

            btnEpaisseur.setOnClickListener {
                showChangeEpaisseurDialog(connexion)
                popupWindow.dismiss()
            }

            popupWindow.showAtLocation(this, Gravity.CENTER, 0, 0)
        }
    }

    private fun showChangeNameDialogConnexion(connexion: Connexion) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.connexion_name_attr))

        val input = EditText(context)
        input.setText(connexion.etiquette.nom)
        builder.setView(input)

        builder.setPositiveButton(context.getString(R.string.ok_btn)) { _, _ ->
            val newName = input.text.toString()
            connexion.etiquette.nom = newName
            invalidate()
        }

        builder.setNegativeButton(context.getString(R.string.cancel_btn)) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun showChangeColorDialogConnexion(connexion: Connexion) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.connexion_color_attr))

        // Create a spinner with a list of color options
        val colors = arrayOf(
            context.getString(R.string.red),
            context.getString(R.string.green),
            context.getString(R.string.blue),
            context.getString(R.string.orange),
            context.getString(R.string.cyan),
            context.getString(R.string.magenta),
            context.getString(R.string.black))
        val colorSpinner = Spinner(context)
        colorSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, colors)

        builder.setView(colorSpinner)

        builder.setPositiveButton(context.getString(R.string.ok_btn)) { _, _ ->
            // Get the selected color from the spinner
            val selectedColor = colors[colorSpinner.selectedItemPosition]

            // Update the connexion's color based on the selected color
            connexion.couleur = when (selectedColor) {
                context.getString(R.string.red) -> Couleur("Red", Color.RED)
                context.getString(R.string.green) -> Couleur("Green", Color.GREEN)
                context.getString(R.string.blue) -> Couleur("Blue", Color.BLUE)
                context.getString((R.string.orange))-> Couleur("Orange", Color.rgb(255, 165, 0))
                context.getString(R.string.cyan) -> Couleur("Cyan", Color.CYAN)
                context.getString((R.string.magenta)) -> Couleur("Magenta", Color.MAGENTA)
                context.getString(R.string.black) -> Couleur("Black", Color.BLACK)
                else -> connexion.couleur // Default to the current color if not recognized
            }

            invalidate()
        }

        builder.setNegativeButton(context.getString(R.string.cancel_btn)) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun showChangeEpaisseurDialog(connexion: Connexion) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.connexion_epaisseur_attr))

        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.setText(connexion.taille.toString())
        builder.setView(input)

        builder.setPositiveButton(context.getString(R.string.ok_btn)) { _, _ ->
            // Get the entered value and update the connexion's thickness
            val newEpaisseur = input.text.toString().toFloatOrNull()
            if (newEpaisseur != null) {
                connexion.taille = newEpaisseur
                invalidate()
            } else {
                // Handle invalid input (non-numeric)
                Toast.makeText(context, "Invalid input. Please enter a numeric value.", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton(context.getString(R.string.cancel_btn)) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun stayWithinScreenBoundaries(position: Position): Position{
        // Ensure the node stays within the screen boundaries
        val maxX = width.toFloat() - rayon
        val maxY = height.toFloat() - rayon
        return Position(position.abscisse.coerceIn(rayon, maxX), position.ordonnee.coerceIn(rayon, maxY))
    }

    fun clearGraph(){
        this.graph = Graph(noeuds = mutableListOf(), connexions = mutableListOf())
    }

    fun getGraph() = this.graph

    fun setGraph(graph: Graph){
        this.graph = graph

        if (this.graph.noeuds.isNotEmpty()) Toast.makeText(context, "Grahe non vide", Toast.LENGTH_SHORT).show()
        else Toast.makeText(context, "Graphe vide", Toast.LENGTH_SHORT).show()

        invalidate()
    }

    fun isGraphEmpty() = this.graph == null || this.graph == Graph(noeuds = mutableListOf(), connexions = mutableListOf())

    private fun drawText(nom: String, position: Position, taille: Float, paint: Paint): Etiquette {
        val textWidth = paint.measureText(nom)
        val textX = position.abscisse - (textWidth / 2)
        val textY = position.ordonnee + taille + 40
        return Etiquette(nom, Position(textX, textY), paint.measureText(nom))
    }

    private fun drawCurvedLine(canvas: Canvas?, connexion: Connexion, paint: Paint) {
        val path = Path()
        path.moveTo(connexion.noeudDepart.position.abscisse, connexion.noeudDepart.position.ordonnee)
        path.quadTo(connexion.etiquette.position.abscisse, connexion.etiquette.position.ordonnee + connexion.courbure, connexion.noeudArrive.position.abscisse, connexion.noeudArrive.position.ordonnee)
        canvas?.drawPath(path, paint)
    }

    private fun calculateCurvature(connexion: Connexion): Float {
        // Calculez la distance entre les points de départ et d'arrivée
        val distance = calculateDistance(connexion.noeudDepart.position, connexion.noeudArrive.position)

        // Normalisez la distance en fonction de la plage de courbure souhaitée
        val maxCurvature = 100.0f

        // La distance normalisée peut être utilisée comme mesure de la courbure
        return distance.coerceIn(0f, maxCurvature)
    }
}
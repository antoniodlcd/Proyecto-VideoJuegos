package com.mygame;

import com.mygame.vista.ControlCamara;
import com.mygame.combate.ManejoArmas;
import com.mygame.entidades.IAVillanos;
import com.mygame.sistemas.ManejoFisicas;
import com.mygame.sistemas.GestorOleadas;
import com.mygame.sistemas.ManejoInputs;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.light.PointLight;
import com.jme3.renderer.Camera;

/* Clase principal que orquesta la inicialización de las físicas, el escenario, el héroe y el ciclo de vida del juego. */
public class Main extends SimpleApplication {

    private boolean cursorBloquedo = false;
    private Node NodoSoldado;
    private Spatial ModeloLaberinto;
    private BulletAppState EstadoFisicas;
    private ManejoInputs EntradasJugador;
    private float TiempoUltimoDisparo = 0f;
    private float TpfActual = 0f; // nueva declaracion de variable global
    private Spatial arania;
    private Spatial En_Tanque;
    private BitmapText textoContador;
    //Variables para la generacion de oleadas
    private BitmapText textoOleada;
    // Quitamos PoolDeSpawns de simpleInitApp y la volvemos global
    private Camera camMinimapa;
    private GestorOleadas gestorOleadas;
    // VARIABLES DE VIDA DEL JUGADOR
    private int vidaJugador = Constantes.JUGADOR_VIDA_INICIAL;
    private float tiempoUltimoGolpe = -Constantes.JUGADOR_TIEMPO_INVULNERABILIDAD; // Para darle invulnerabilidad temporal
    private BitmapText textoVida;
    // marcador de salida del laberinto
    private Spatial MarcadorSalida;
    private boolean juegoTerminado = false;
    
    private com.jme3.scene.Geometry muroSalida;
    private com.jme3.bullet.control.RigidBodyControl fisicoMuro;
    private boolean salidaAbierta = false;
    
    private boolean primerFotograma = true;
    
    public static void main(String[] args) {
        Main Aplicacion = new Main();
        
        //Ajustes del juego
        AppSettings Ajustes = new AppSettings(true);
        Ajustes.setResolution(1280, 720);
        Ajustes.setVSync(true);
        Ajustes.setFullscreen(false);
        
        Aplicacion.setSettings(Ajustes);
        Aplicacion.start();
        
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        flyCam.setMoveSpeed(20f);
        flyCam.unregisterInput();
        
//        inputManager.setCursorVisible(false); // ocultar el cursor
//        getContext().getMouseInput().setCursorVisible(false);
        // Inicializar el objeto de texto para la pantalla
        textoContador = new BitmapText(assetManager.loadFont("Interface/Fonts/Default.fnt"));
        textoContador.setSize(30);
        textoContador.setColor(ColorRGBA.Red);
        textoContador.setLocalTranslation(20, settings.getHeight() - 20, 0); 

        guiNode.attachChild(textoContador);
        
        float relacionAspecto = (float) settings.getWidth() / settings.getHeight();
        cam.setFrustumPerspective(45f, relacionAspecto, 0.1f, 1000f);
        cam.setFrustumNear(0.1f);

        EstadoFisicas = new BulletAppState();
        stateManager.attach(EstadoFisicas);

        // Inicializar el texto de vida en el HUD
        textoVida = new BitmapText(assetManager.loadFont("Interface/Fonts/Default.fnt"));
        textoVida.setSize(30);
        textoVida.setColor(ColorRGBA.Green); // Verde para la salud
        textoVida.setLocalTranslation(20, settings.getHeight() - 60, 0); // Un poco más abajo del contador
        guiNode.attachChild(textoVida);
        actualizarTextoVida();

//        ModeloLaberinto = assetManager.loadModel("Models/Laberinto.j3o");
        ModeloLaberinto = assetManager.loadModel("Models/maze3.j3o");
        ModeloLaberinto.setLocalScale(5f); // mapa escalado a la mitad
        ManejoFisicas.ConfigurarEscena(ModeloLaberinto, rootNode, EstadoFisicas, assetManager);

        Spatial visualSoldado = assetManager.loadModel("Models/soldier.j3o");
        NodoSoldado = ManejoFisicas.AplicarFisicasPersonaje(visualSoldado, rootNode, EstadoFisicas);
        
        // =====================================================================
        // PRIMERO: Buscamos el punto de inicio del mapa en Blender
        // =====================================================================
        Spatial MarcadorInicio = EncontrarNodo(ModeloLaberinto, "PuntoInicio"); 
        
        if (MarcadorInicio != null) {
            Vector3f CoordenadaInicio = MarcadorInicio.getWorldTranslation();
            NodoSoldado.getControl(BetterCharacterControl.class).warp(CoordenadaInicio.add(0, 1.5f, 0)); 
            System.out.println("Personaje posicionado automaticamente en: " + CoordenadaInicio);
        } else {
            System.out.println("Error: No se encontro 'PuntoInicio', usando coordenadas por defecto");
            NodoSoldado.getControl(BetterCharacterControl.class).warp(new Vector3f(0, 20 ,0));
        }
        
        MarcadorSalida = EncontrarNodo(ModeloLaberinto, "PuntoFin");
        
        if (MarcadorSalida != null) {
            System.out.println("Punto de salida detectado en: " + MarcadorSalida.getWorldTranslation());
            
            com.jme3.scene.shape.Box formaCaja = new com.jme3.scene.shape.Box(6f, 6f, 6f); 
            muroSalida = new com.jme3.scene.Geometry("MuroSalida", formaCaja);
            muroSalida.setLocalTranslation(MarcadorSalida.getWorldTranslation());
            muroSalida.setCullHint(Spatial.CullHint.Always); 
            
            fisicoMuro = new com.jme3.bullet.control.RigidBodyControl(0.0f);
            muroSalida.addControl(fisicoMuro);
            
            rootNode.attachChild(muroSalida);
            EstadoFisicas.getPhysicsSpace().add(fisicoMuro);
        } else {
            System.out.println("Error: No se encontro 'PuntoFin' en el modelo del laberinto");
        }

        // =====================================================================
        // SPAWN ALEATORIO Y SEGURO DE VILLANOS
        // =====================================================================
        Spatial modeloAraniaBase = assetManager.loadModel("Models/arania.j3o");
        Spatial modeloTanqueBase = assetManager.loadModel("Models/En_Tanque.j3o");

        textoOleada = new BitmapText(assetManager.loadFont("Interface/Fonts/Default.fnt"));
        textoOleada.setSize(30);
        textoOleada.setColor(ColorRGBA.Cyan);
        textoOleada.setLocalTranslation(20, settings.getHeight() - 100, 0); 
        guiNode.attachChild(textoOleada);


        // inicializar el gestor de oleadas
        gestorOleadas = new GestorOleadas(this, ModeloLaberinto);
        gestorOleadas.iniciarNuevaOleada();
        
        
        EntradasJugador = new ManejoInputs();
        EntradasJugador.ConfigurarTeclado(inputManager);
        
        NodoSoldado.getControl(BetterCharacterControl.class).setViewDirection(new Vector3f(0, 0, -1));

        // --- Nueva Linterna Adaptada a la escala ---
        PointLight Linterna = new PointLight();
        Linterna.setColor(ColorRGBA.White.mult(1.5f));
        Linterna.setRadius(40f);
        //Adjuntamos la luz al personaje
        NodoSoldado.addLight(Linterna);
     
        ControlCamara.ActualizarCamaraFisica(cam, NodoSoldado, 0, 0f, 0f, EstadoFisicas.getPhysicsSpace());
        //Insertamos Materiales  
        com.jme3.material.Material MatLaberinto = new com.jme3.material.Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        
        //Cargamos texturas
        com.jme3.texture.Texture TexturaPiedra = assetManager.loadTexture("Textures/Piedra.jpg");
        
        //Hacemos que la textura se repita
        TexturaPiedra.setWrap(com.jme3.texture.Texture.WrapMode.Repeat);
        
        //Asignamos la textura como color principal
        MatLaberinto.setTexture("DiffuseMap", TexturaPiedra);
        
        ConfigurarMirilla();
        
        //Aplicamos el material
        setDisplayStatView(false);
        
        // ==========================================
        // CONFIGURACIÓN DEL MINIMAPA (RADAR)
        // ==========================================
        camMinimapa = cam.clone();
        
        // Recortamos la cámara para que solo dibuje en la esquina superior derecha
        camMinimapa.setViewPort(0.75f, 0.98f, 0.70f, 0.95f); 
        
        // Hacemos que sea una vista 2D plana desde arriba
        camMinimapa.setParallelProjection(true); 
        float aspecto = (float) cam.getWidth() / cam.getHeight();
        
        // Ajustamos el zoom del radar (50 unidades de visión)
        camMinimapa.setFrustum(-100f, 300f, -50f * aspecto, 50f * aspecto, 50f, -50f); 
        
        com.jme3.renderer.ViewPort vistaMinimapa = renderManager.createMainView("Minimapa", camMinimapa);
        vistaMinimapa.setClearFlags(true, true, true);
        vistaMinimapa.setBackgroundColor(new ColorRGBA(0.0f, 0.1f, 0.0f, 1f));
        vistaMinimapa.attachScene(rootNode);
        
        inputManager.setCursorVisible(false);
        cursorBloquedo = true;
    }


    

    // NUEVOS MÉTODOS PARA CONTROLAR EL CONTADOR DESDE PANTALLA O DESDE OTRAS CLASES
    public void actualizarTextoContador(int restantes) {
        if (textoContador != null) {
            textoContador.setText("Villanos restantes: " + restantes);
        }
    }
    
    public void actualizarTextoOleada(int ronda) {
        if (textoOleada != null) {
            textoOleada.setText("Oleada: " + ronda);
        }
    }

    @Override
    public void simpleUpdate(float Tpf) {
        
        if (primerFotograma) {
            inputManager.setCursorVisible(false);
            primerFotograma = false;
        }
        
        // 1. Crear el texto de Victoria
        BitmapText textoVictoria = new BitmapText(assetManager.loadFont("Interface/Fonts/Default.fnt"));
        textoVictoria.setText("¡VICTORIA! LABERINTO COMPLETADO");
        textoVictoria.setSize(40);
        textoVictoria.setColor(ColorRGBA.Green); // Color verde de éxito
        
        
        TpfActual = Tpf; 
        tiempoUltimoGolpe += Tpf;
        
        if (juegoTerminado) {
            if (EntradasJugador.getReiniciar()) {
                reiniciarJuego();
            }
            return; 
        }
        
        // --- LÓGICA DE MOVIMIENTO ---
        Vector3f Direccion = EntradasJugador.ObtenerDireccion(cam);
        NodoSoldado.getControl(BetterCharacterControl.class).setWalkDirection(Direccion.mult(Constantes.JUGADOR_VELOCIDAD));

        Vector3f DireccionVista = cam.getDirection().clone();
        DireccionVista.setY(0);
        DireccionVista.normalizeLocal();
        NodoSoldado.getControl(BetterCharacterControl.class).setViewDirection(DireccionVista);
        
        // Sumamos el tiempo que ha pasado desde el último frame (Tpf)
        TiempoUltimoDisparo += Tpf;
        
        // Si el jugador hace clic Y ha pasado el tiempo suficiente desde el último disparo...
        if (EntradasJugador.getDisparando() && TiempoUltimoDisparo >= Constantes.ARMA_CADENCIA_TIRO) {
            
            // Imprime tu posición exacta en la consola para usarla como Spawn
            System.out.println("Posición segura para enemigo: " + NodoSoldado.getWorldTranslation());

            // NUEVO LLAMADO AL DISPARO CON FÍSICAS
            ManejoArmas.DispararLaser(cam, EstadoFisicas.getPhysicsSpace(), NodoSoldado, assetManager, rootNode, this);
            TiempoUltimoDisparo = 0; 
        }
        
        IAVillanos.PerseguirHeroe(NodoSoldado, gestorOleadas.getListaVillanos(), Tpf, this);
        
        // --- ACTUALIZAR MINIMAPA ---
        Vector3f posJugador = NodoSoldado.getWorldTranslation();
        // La cámara del mapa se posiciona 150 metros arriba de tu cabeza
        camMinimapa.setLocation(new Vector3f(posJugador.x, 150f, posJugador.z)); 
        // Y mira directamente hacia abajo
        camMinimapa.lookAtDirection(new Vector3f(0, -1, 0), new Vector3f(0, 0, -1));
        
        // --- VERIFICAR SI SE LLEGA A LA META ---
        if (!juegoTerminado && MarcadorSalida != null) {
            float distanciaASalida = NodoSoldado.getWorldTranslation().distance(MarcadorSalida.getWorldTranslation());
            
            if (distanciaASalida < 10.0f) {
                mostrarPantallaVictoria();
            }
        }
        
        // --- SISTEMA DE CAÍDA ---
        // Si el jugador cae por debajo de Y = -3.0 (el vacío)
        if (NodoSoldado.getWorldTranslation().y < -3.0f) {
            
            // 1. Buscamos el punto de inicio para regresarlo
            Spatial MarcadorInicio = EncontrarNodo(ModeloLaberinto, "PuntoInicio"); 
            if (MarcadorInicio != null) {
                Vector3f CoordenadaInicio = MarcadorInicio.getWorldTranslation().add(0, 1.5f, 0);
                
                // 2. Lo teletransportamos de vuelta a un lugar seguro
                NodoSoldado.getControl(BetterCharacterControl.class).warp(CoordenadaInicio);
                
                // 3. Le cobramos 20 de vida como penalización por caerse
                recibirDanio(20); 
                System.out.println("¡Te caíste del mapa! Regresando al inicio...");
            }
        }
        
    }

    @Override
    public void simpleRender(RenderManager rm) {
        ControlCamara.ActualizarCamaraFisica(
            cam, 
            NodoSoldado, 
            TpfActual, 
            EntradasJugador.getGiroX(), 
            EntradasJugador.getGiroY(),
            EstadoFisicas.getPhysicsSpace()
        );
    }
    
    // Metodo para buscar un objeto por su nombre
    private Spatial EncontrarNodo(Spatial NodoRaiz, String NombreBuscado) {
        if (NodoRaiz.getName().equals(NombreBuscado)) { // el nodo raiz es el buscado
            return NodoRaiz;
        }
        
        if (NodoRaiz instanceof Node) { 
            Node Contenedor = (Node) NodoRaiz;
            for (Spatial Hijo : Contenedor.getChildren()) { // busca entre los hijos
                Spatial Resultado = EncontrarNodo(Hijo, NombreBuscado);
                if (Resultado != null) {
                    return Resultado;
                }
            }
        }
        return null;
    }
    
    // metodo para dibujar mira
    private void ConfigurarMirilla() {
        BitmapText Mirilla = new BitmapText(assetManager.loadFont("Interface/Fonts/Default.fnt")); // fuente por defecto
        
        // configurar simbolo y tamaño del simbolo
        Mirilla.setText("+");
        Mirilla.setSize(Mirilla.getFont().getCharSet().getRenderedSize() * 2);
        Mirilla.setColor(ColorRGBA.White);
        
        // colocar al centro de la pantalla
        float MitadAncho = (settings.getWidth() / 2f) - (Mirilla.getLineWidth() / 2f);
        float MitadAlto = (settings.getHeight()/ 2f) + (Mirilla.getLineHeight() / 2f);
        Mirilla.setLocalTranslation(MitadAncho, MitadAlto + 150, 0);
        
        
        guiNode.attachChild(Mirilla); // aisgnarla al guiNode
    }
    
    // MÉTODOS DE CONTROL DE VIDA Y CONDICIÓN DE DERROTA
    public void actualizarTextoVida() {
        if (textoVida != null) {
            textoVida.setText("Vida: " + vidaJugador + "%");
            if (vidaJugador <= Constantes.JUGADOR_ALERTA_VIDA_BAJA) {
                textoVida.setColor(ColorRGBA.Red);
            } else {
                textoVida.setColor(ColorRGBA.Green);
            }
        }
    }

    public void recibirDanio(int cantidad) {
        if (vidaJugador > 0) {
            vidaJugador -= cantidad;
            actualizarTextoVida();
            
            if (vidaJugador <= 0) {
                vidaJugador = 0;
                actualizarTextoVida();
                mostrarPantallaDerrota();
            }
        }
    }

    public void mostrarPantallaVictoria() {
        juegoTerminado = true;
        
        BitmapText textoVictoria = new BitmapText(assetManager.loadFont("Interface/Fonts/Default.fnt"));
        textoVictoria.setText("¡VICTORIA! LABERINTO COMPLETADO");
        textoVictoria.setSize(40);
        textoVictoria.setColor(ColorRGBA.Green);
        
        float x = (settings.getWidth() / 2f) - (textoVictoria.getLineWidth() / 2f);
        float y = (settings.getHeight() / 2f) + (textoVictoria.getLineHeight() / 2f);
        textoVictoria.setLocalTranslation(x, y, 0);
        guiNode.attachChild(textoVictoria);
        
        BitmapText textoReinicio = new BitmapText(assetManager.loadFont("Interface/Fonts/Default.fnt"));
        textoReinicio.setText("[ PRESIONA 'R' PARA JUGAR DE NUEVO ]");
        textoReinicio.setSize(25);
        textoReinicio.setColor(ColorRGBA.White);
        float rx = (settings.getWidth() / 2f) - (textoReinicio.getLineWidth() / 2f);
        textoReinicio.setLocalTranslation(rx, y - 60, 0);
        guiNode.attachChild(textoReinicio);
        
        inputManager.setCursorVisible(true);
        cursorBloquedo = false;
        EstadoFisicas.setEnabled(false); 
    }

    public void mostrarPantallaDerrota() {
        juegoTerminado = true; 
        
        BitmapText textoDerrota = new BitmapText(assetManager.loadFont("Interface/Fonts/Default.fnt"));
        textoDerrota.setText("GAME OVER - TE HAN ELIMINADO");
        textoDerrota.setSize(40);
        textoDerrota.setColor(ColorRGBA.Red);
        
        float x = (settings.getWidth() / 2f) - (textoDerrota.getLineWidth() / 2f);
        float y = (settings.getHeight() / 2f) + (textoDerrota.getLineHeight() / 2f);
        textoDerrota.setLocalTranslation(x, y, 0);
        
        guiNode.attachChild(textoDerrota);
        
        BitmapText textoReinicio = new BitmapText(assetManager.loadFont("Interface/Fonts/Default.fnt"));
        textoReinicio.setText("[ PRESIONA 'R' PARA INTENTARLO OTRA VEZ ]");
        textoReinicio.setSize(25);
        textoReinicio.setColor(ColorRGBA.White);
        float rx = (settings.getWidth() / 2f) - (textoReinicio.getLineWidth() / 2f);
        textoReinicio.setLocalTranslation(rx, y - 60, 0);
        guiNode.attachChild(textoReinicio);
        
        inputManager.setCursorVisible(true);
        cursorBloquedo = false;
        EstadoFisicas.setEnabled(false); // Congelar juego al morir
    }
    
    public void reiniciarJuego() {
        System.out.println("Reiniciando el juego...");
        
        EstadoFisicas.setEnabled(true);
        inputManager.setCursorVisible(false);
        cursorBloquedo = true;
        juegoTerminado = false;
        
        guiNode.detachAllChildren();
        guiNode.attachChild(textoContador);
        guiNode.attachChild(textoVida);
        guiNode.attachChild(textoOleada);
        ConfigurarMirilla();
        
        for (Spatial enemigo : gestorOleadas.getListaVillanos()) {
            enemigo.removeFromParent();
            BetterCharacterControl fisicasE = enemigo.getControl(BetterCharacterControl.class);
            if (fisicasE != null) {
                EstadoFisicas.getPhysicsSpace().remove(fisicasE);
            }
        }
        
        vidaJugador = Constantes.JUGADOR_VIDA_INICIAL;
        actualizarTextoVida();
        tiempoUltimoGolpe = -Constantes.JUGADOR_TIEMPO_INVULNERABILIDAD;
        
        Spatial MarcadorInicio = EncontrarNodo(ModeloLaberinto, "PuntoInicio"); 
        if (MarcadorInicio != null) {
            NodoSoldado.getControl(BetterCharacterControl.class).warp(MarcadorInicio.getWorldTranslation().add(0, 1.5f, 0));
        } else {
            NodoSoldado.getControl(BetterCharacterControl.class).warp(new Vector3f(0, 20 ,0));
        }
        
        gestorOleadas = new GestorOleadas(this, ModeloLaberinto);
        gestorOleadas.iniciarNuevaOleada();
    }
    
    public void abrirSalida() {
        if (!salidaAbierta) {
            salidaAbierta = true;
            
            if (muroSalida != null) {
                muroSalida.removeFromParent();
                EstadoFisicas.getPhysicsSpace().remove(fisicoMuro);
            }
            
            BitmapText textoAlerta = new BitmapText(assetManager.loadFont("Interface/Fonts/Default.fnt"));
            textoAlerta.setText("¡LA SALIDA SE HA DESBLOQUEADO! ¡ESCAPA!");
            textoAlerta.setSize(35);
            textoAlerta.setColor(ColorRGBA.Yellow);
            
            float centroX = (settings.getWidth() / 2f) - (textoAlerta.getLineWidth() / 2f);
            textoAlerta.setLocalTranslation(centroX, settings.getHeight() - 150, 0);
            guiNode.attachChild(textoAlerta);
            
            System.out.println("Muro invisible destruido. El jugador ya puede escapar.");
        }
    }

    public float getTiempoUltimoGolpe() { return tiempoUltimoGolpe; }
    public void setTiempoUltimoGolpe(float tiempo) { this.tiempoUltimoGolpe = tiempo; }
    public BulletAppState getEstadoFisicas() { return EstadoFisicas; }
    public GestorOleadas getGestorOleadas() { return gestorOleadas; }
}
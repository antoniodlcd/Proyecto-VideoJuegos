package com.mygame;

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

/* Clase principal que orquesta la inicialización de las físicas, el escenario, el héroe y el ciclo de vida del juego. */
public class Main extends SimpleApplication {

    private boolean cursorBloquedo = false;
    private Node NodoSoldado;
    private Spatial ModeloLaberinto;
    private BulletAppState EstadoFisicas;
    private ManejoInputs EntradasJugador;
    private float TiempoUltimoDisparo = 0f;
    private float TpfActual = 0f; // nueva declaracion de variable global
    private final float CadenciaTiro = 0.5f;
    private Spatial arania;
    private Spatial En_Tanque;
    private BitmapText textoContador;
    private java.util.ArrayList<Spatial> ListaVillanos = new java.util.ArrayList<>();

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

//        ModeloLaberinto = assetManager.loadModel("Models/Laberinto.j3o");
        ModeloLaberinto = assetManager.loadModel("Models/maze2.j3o");
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

        // =====================================================================
        // SPAWN ALEATORIO Y SEGURO DE VILLANOS
        // =====================================================================
        Spatial modeloAraniaBase = assetManager.loadModel("Models/arania.j3o");
        Spatial modeloTanqueBase = assetManager.loadModel("Models/En_Tanque.j3o");

        // 1. Creamos una lista con todos los puntos seguros que recolectaste (puedes agregar más si quieres)
        java.util.ArrayList<Vector3f> PoolDeSpawns = new java.util.ArrayList<>();
        PoolDeSpawns.add(new Vector3f(14.43f, 2.5f, 378.24f));
        PoolDeSpawns.add(new Vector3f(12.29f, 2.5f, 285.62f));
        PoolDeSpawns.add(new Vector3f(11.50f, 2.5f, 270.16f));
        PoolDeSpawns.add(new Vector3f(-40.95f, 2.5f, 191.44f));
        PoolDeSpawns.add(new Vector3f(-62.91f, 2.5f, 191.77f));
        PoolDeSpawns.add(new Vector3f(-258.61f, 2.5f, 192.83f));
        PoolDeSpawns.add(new Vector3f(-310.77f, 2.5f, 202.06f));
        PoolDeSpawns.add(new Vector3f(-327.76f, 2.5f, 260.86f));
        PoolDeSpawns.add(new Vector3f(-338.89f, 2.5f, 185.40f));
        PoolDeSpawns.add(new Vector3f(-428.48f, 2.5f, 88.78f));
        
        // 2. MAGIA: Revolvemos la lista al azar como si fuera una baraja de cartas
        java.util.Collections.shuffle(PoolDeSpawns);

        // 3. Tomamos los primeros 10 lugares de la lista ya revuelta
        for (int i = 0; i < 10; i++) {
            
            // Creamos un contenedor vacío. Este Nodo será el verdadero enemigo para el motor.
            Node NodoEnemigo = new Node(); 
            Spatial visualEnemigo;
            
            if (i % 2 == 0) {
                // Instanciamos la Araña
                visualEnemigo = modeloAraniaBase.clone();
                NodoEnemigo.setName("Arania");
                
                // La araña está bien posicionada, la dejamos en el origen del Nodo (0,0,0)
                visualEnemigo.setLocalTranslation(0, 0f, 0); 
                
            } else {
                // Instanciamos el Tanque
                visualEnemigo = modeloTanqueBase.clone();
                NodoEnemigo.setName("Tanque");
                
                // Movemos solo el dibujo del Tanque hacia arriba dentro del contenedor.
                // 1.2f es un valor aproximado, puedes subirlo 
                // hasta que sus ruedas toquen el suelo perfectamente.
                visualEnemigo.setLocalTranslation(0, 1.2f, 0); 
            }

            // Metemos el dibujo visual dentro del Nodo contenedor
            NodoEnemigo.attachChild(visualEnemigo);
            
            // Le pegamos los puntos de vida al Nodo contenedor
            NodoEnemigo.setUserData("Vida", 100);

            // Las físicas ahora controlan al Nodo, no al dibujo
            BetterCharacterControl fisicasE = new BetterCharacterControl(0.8f, 2.5f, 40f);
            NodoEnemigo.addControl(fisicasE);
            
            rootNode.attachChild(NodoEnemigo);
            EstadoFisicas.getPhysicsSpace().add(fisicasE);
            
            // Asignamos el punto aleatorio seguro
            fisicasE.warp(PoolDeSpawns.get(i));
            
            // Agregamos el Nodo a la lista de persecución
            ListaVillanos.add(NodoEnemigo); 
        }
        
        EntradasJugador = new ManejoInputs();
        EntradasJugador.ConfigurarTeclado(inputManager);
        

//        NodoSoldado.getControl(BetterCharacterControl.class).warp(new Vector3f(32, 2, 33));
////        NodoSoldado.getControl(BetterCharacterControl.class).warp(new Vector3f(0,400, 500));

        if (MarcadorInicio != null) {
            Vector3f CoordenadaInicio = MarcadorInicio.getWorldTranslation();
            NodoSoldado.getControl(BetterCharacterControl.class).warp(CoordenadaInicio.add(0, 1.5f, 0)); // spawnear el soldado en el inicio
            System.out.println("Personaje posicionado automaticamente en: " + CoordenadaInicio);
        } else {
            System.out.println("Error: No se encontro 'PuntoInicio', usando coordenadas por defecto");
            NodoSoldado.getControl(BetterCharacterControl.class).warp(new Vector3f(0, 20 ,0));
        }
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
    }

    @Override
    public void simpleUpdate(float Tpf) {
        TpfActual = Tpf; // guardar el tiempo del fotograma
        
        // ocultamos el cursor cuando la ventana ya existe
        if (!cursorBloquedo) {
            inputManager.setCursorVisible(false);
            cursorBloquedo = true;
        }
        
        // --- LÓGICA DE MOVIMIENTO ---
        Vector3f Direccion = EntradasJugador.ObtenerDireccion(cam);
        NodoSoldado.getControl(BetterCharacterControl.class).setWalkDirection(Direccion.mult(30f));

        Vector3f DireccionVista = cam.getDirection().clone();
        DireccionVista.setY(0);
        DireccionVista.normalizeLocal();
        NodoSoldado.getControl(BetterCharacterControl.class).setViewDirection(DireccionVista);
        
        // Sumamos el tiempo que ha pasado desde el último frame (Tpf)
        TiempoUltimoDisparo += Tpf;
        
        // Si el jugador hace clic Y ha pasado el tiempo suficiente desde el último disparo...
        if (EntradasJugador.getDisparando() && TiempoUltimoDisparo >= CadenciaTiro) {
            
            // Imprime tu posición exacta en la consola para usarla como Spawn
            System.out.println("Posición segura para enemigo: " + NodoSoldado.getWorldTranslation());

            // NUEVO LLAMADO AL DISPARO CON FÍSICAS
            ManejoArmas.DispararLaser(cam, EstadoFisicas.getPhysicsSpace(), NodoSoldado, assetManager, rootNode);
            TiempoUltimoDisparo = 0; 
        }
        
        IAVillanos.PerseguirHeroe(NodoSoldado, ListaVillanos, Tpf);
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
}

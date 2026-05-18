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

        EntradasJugador = new ManejoInputs();
        EntradasJugador.ConfigurarTeclado(inputManager);
        

//        NodoSoldado.getControl(BetterCharacterControl.class).warp(new Vector3f(32, 2, 33));
////        NodoSoldado.getControl(BetterCharacterControl.class).warp(new Vector3f(0,400, 500));

        Spatial MarcadorInicio = EncontrarNodo(ModeloLaberinto, "PuntoInicio"); // encontrar el punto de inicsssio del mapa
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
        Linterna.setRadius(80f);
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
        NodoSoldado.getControl(BetterCharacterControl.class).setWalkDirection(Direccion.mult(10f));

        Vector3f DireccionVista = cam.getDirection().clone();
        DireccionVista.setY(0);
        DireccionVista.normalizeLocal();
        NodoSoldado.getControl(BetterCharacterControl.class).setViewDirection(DireccionVista);
        
        // Sumamos el tiempo que ha pasado desde el último frame (Tpf)
        TiempoUltimoDisparo += Tpf;
        
        // Si el jugador hace clic Y ha pasado el tiempo suficiente desde el último disparo...
        if (EntradasJugador.getDisparando() && TiempoUltimoDisparo >= CadenciaTiro) {
            // NUEVO: Agregamos assetManager como cuarto parámetro
            ManejoArmas.DispararLaser(cam, rootNode, NodoSoldado, assetManager);
            TiempoUltimoDisparo = 0; 
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
}

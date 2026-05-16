package com.mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;

/* Clase principal que orquesta la inicialización de las físicas, el escenario, el héroe y el ciclo de vida del juego. */
public class Main extends SimpleApplication {

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
        Ajustes.setFullscreen(true);
        
        Aplicacion.setSettings(Ajustes);
        Aplicacion.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        flyCam.setMoveSpeed(20f);
        
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
//        NodoSoldado.getControl(BetterCharacterControl.class).setViewDirection(new Vector3f(0, 0, -1));

        ControlCamara.ActualizarCamaraFisica(cam, NodoSoldado, 0, false, false, EstadoFisicas.getPhysicsSpace());
        //Insertamos Materiales  
        com.jme3.material.Material MatLaberinto = new com.jme3.material.Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        
        //Cargamos texturas
        com.jme3.texture.Texture TexturaPiedra = assetManager.loadTexture("Textures/Piedra.jpg");
        
        //Hacemos que la textura se repita
        TexturaPiedra.setWrap(com.jme3.texture.Texture.WrapMode.Repeat);
        
        //Asignamos la textura como color principal
        MatLaberinto.setTexture("DiffuseMap", TexturaPiedra);
        
        //Aplicamos el material
        setDisplayStatView(false);
    }

    @Override
    public void simpleUpdate(float Tpf) {
        TpfActual = Tpf; // guardar el tiempo del fotograma
        
        // --- LÓGICA DE MOVIMIENTO ---
        Vector3f Direccion = EntradasJugador.ObtenerDireccion(cam);
        NodoSoldado.getControl(BetterCharacterControl.class).setWalkDirection(Direccion.mult(10f));

//        ControlCamara.ActualizarCamaraFisica(
//            cam, 
//            NodoSoldado, 
//            Tpf, 
//            EntradasJugador.getRotarIzquierda(), 
//            EntradasJugador.getRotarDerecha(),
//            EstadoFisicas.getPhysicsSpace()
//        );
        
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
            EntradasJugador.getRotarIzquierda(), 
            EntradasJugador.getRotarDerecha(),
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
}

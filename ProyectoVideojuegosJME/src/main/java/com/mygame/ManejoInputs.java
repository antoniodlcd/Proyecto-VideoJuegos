package com.mygame;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/* Maneja las entradas del teclado (WASD y flechas) y del ratón para mover, rotar al personaje y disparar. */
public class ManejoInputs implements ActionListener {

    private boolean Adelante = false, Atras = false, Izquierda = false, Derecha = false;
    private boolean RotarIzquierda = false, RotarDerecha = false;
    private boolean Disparando = false; // Bandera para saber si se hizo clic

    public void ConfigurarTeclado(InputManager Entradas) {
        Entradas.addMapping("CaminarFrente", new KeyTrigger(KeyInput.KEY_W));
        Entradas.addMapping("CaminarAtras", new KeyTrigger(KeyInput.KEY_S));
        Entradas.addMapping("GiroIzquierda", new KeyTrigger(KeyInput.KEY_A));
        Entradas.addMapping("GiroDerecha", new KeyTrigger(KeyInput.KEY_D));
        
        Entradas.addMapping("RotarCapaIzq", new KeyTrigger(KeyInput.KEY_LEFT));
        Entradas.addMapping("RotarCapaDer", new KeyTrigger(KeyInput.KEY_RIGHT));

        // Mapeo del disparo al clic izquierdo del ratón
        Entradas.addMapping("Disparar", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        // Se registran todas las acciones en el Listener
        Entradas.addListener(this, "CaminarFrente", "CaminarAtras", "GiroIzquierda", "GiroDerecha", "RotarCapaIzq", "RotarCapaDer", "Disparar");
    }

    @Override
    public void onAction(String Nombre, boolean EstaPresionado, float Tpf) {
        if (Nombre.equals("CaminarFrente")) Adelante = EstaPresionado;
        if (Nombre.equals("CaminarAtras")) Atras = EstaPresionado;
        if (Nombre.equals("GiroIzquierda")) Izquierda = EstaPresionado;
        if (Nombre.equals("GiroDerecha")) Derecha = EstaPresionado;
        if (Nombre.equals("RotarCapaIzq")) RotarIzquierda = EstaPresionado;
        if (Nombre.equals("RotarCapaDer")) RotarDerecha = EstaPresionado;
        
        // Capturar estado del disparo
        if (Nombre.equals("Disparar")) {
            Disparando = EstaPresionado;
        }
    }

    public Vector3f ObtenerDireccion(Camera Camara) {
        Vector3f DireccionCaminata = new Vector3f();
        Vector3f CamDir = Camara.getDirection().clone().setY(0).normalizeLocal();
        Vector3f CamLeft = Camara.getLeft().clone().setY(0).normalizeLocal();

        if (Adelante) DireccionCaminata.addLocal(CamDir);
        if (Atras) DireccionCaminata.addLocal(CamDir.negate());
        if (Izquierda) DireccionCaminata.addLocal(CamLeft);
        if (Derecha) DireccionCaminata.addLocal(CamLeft.negate());

        return DireccionCaminata.normalizeLocal();
    }

    // Getters para acceder a los estados desde otras clases
    public boolean getRotarIzquierda() { return RotarIzquierda; }
    public boolean getRotarDerecha() { return RotarDerecha; }
    
    // AQUÍ ESTÁ EL MÉTODO QUE ESTABA PIDIENDO EL MAIN
    public boolean getDisparando() { return Disparando; } 
}
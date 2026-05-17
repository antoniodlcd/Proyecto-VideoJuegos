package com.mygame;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/* Maneja las entradas del teclado (WASD y flechas) y del ratón para mover, rotar al personaje y disparar. */
public class ManejoInputs implements ActionListener, AnalogListener {

    private boolean Adelante = false, Atras = false, Izquierda = false, Derecha = false;
    private boolean Disparando = false; // Bandera para saber si se hizo clic
//    private boolean RotarIzquierda = false, RotarDerecha = false;
    private float DeltaRatonX = 0; // movimiento del raton en el eje x
    private float DeltaRatonY = 0; // movimiento del raton en el eje y

    public void ConfigurarTeclado(InputManager Entradas) {
        Entradas.addMapping("CaminarFrente", new KeyTrigger(KeyInput.KEY_W));
        Entradas.addMapping("CaminarAtras", new KeyTrigger(KeyInput.KEY_S));
        Entradas.addMapping("GiroIzquierda", new KeyTrigger(KeyInput.KEY_A));
        Entradas.addMapping("GiroDerecha", new KeyTrigger(KeyInput.KEY_D));
        
//        Entradas.addMapping("RotarCapaIzq", new KeyTrigger(KeyInput.KEY_LEFT));
//        Entradas.addMapping("RotarCapaDer", new KeyTrigger(KeyInput.KEY_RIGHT));

        Entradas.addMapping("RatonXIzq", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        Entradas.addMapping("RatonXDer", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        Entradas.addMapping("RatonYArriba", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        Entradas.addMapping("RatonYAbajo", new MouseAxisTrigger(MouseInput.AXIS_Y, false));

        // Mapeo del disparo al clic izquierdo del ratón
        Entradas.addMapping("Disparar", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        // Se registran todas las acciones en el Listener
        Entradas.addListener(this, "CaminarFrente", "CaminarAtras", "GiroIzquierda", "GiroDerecha", "Disparar");
        Entradas.addListener(this, "RatonXIzq", "RatonXDer", "RatonYArriba", "RatonYAbajo");
    }

    @Override
    public void onAction(String Nombre, boolean EstaPresionado, float Tpf) {
        if (Nombre.equals("CaminarFrente")) Adelante = EstaPresionado;
        if (Nombre.equals("CaminarAtras")) Atras = EstaPresionado;
        if (Nombre.equals("GiroIzquierda")) Izquierda = EstaPresionado;
        if (Nombre.equals("GiroDerecha")) Derecha = EstaPresionado;
        if (Nombre.equals("Disparar")) Disparando = EstaPresionado; // Capturar estado del disparo
    }
    
    @Override
    public void onAnalog(String Nombre, float Valor, float Tpf) {
        if (Nombre.equals("RatonXIzq")) DeltaRatonX -= Valor;
        if (Nombre.equals("RatonXDer")) DeltaRatonX += Valor;
        if (Nombre.equals("RatonYArriba")) DeltaRatonY -= Valor;
        if (Nombre.equals("RatonYAbajo")) DeltaRatonY += Valor;
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
    public float getGiroX() { float val = DeltaRatonX; DeltaRatonX = 0; return val; }
    public float getGiroY() { float val = DeltaRatonY; DeltaRatonY = 0; return val; }
    
    // AQUÍ ESTÁ EL MÉTODO QUE ESTABA PIDIENDO EL MAIN
    public boolean getDisparando() { return Disparando; } 


}
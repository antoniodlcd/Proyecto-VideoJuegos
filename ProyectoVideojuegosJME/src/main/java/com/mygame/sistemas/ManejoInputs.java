package com.mygame.sistemas;

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
    private boolean Disparando = false; 
    private float DeltaRatonX = 0; 
    private float DeltaRatonY = 0; 
    private boolean Reiniciar = false; 
    
    public void ConfigurarTeclado(InputManager Entradas) {
        Entradas.addMapping("CaminarFrente", new KeyTrigger(KeyInput.KEY_W));
        Entradas.addMapping("CaminarAtras", new KeyTrigger(KeyInput.KEY_S));
        Entradas.addMapping("GiroIzquierda", new KeyTrigger(KeyInput.KEY_A));
        Entradas.addMapping("GiroDerecha", new KeyTrigger(KeyInput.KEY_D));

        Entradas.addMapping("RatonXIzq", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        Entradas.addMapping("RatonXDer", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        Entradas.addMapping("RatonYArriba", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        Entradas.addMapping("RatonYAbajo", new MouseAxisTrigger(MouseInput.AXIS_Y, false));

        Entradas.addMapping("Disparar", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        Entradas.addMapping("Reiniciar", new KeyTrigger(KeyInput.KEY_R)); 
        
        // Unificamos todos los listeners en una sola llamada limpia
        Entradas.addListener(this, "CaminarFrente", "CaminarAtras", "GiroIzquierda", "GiroDerecha", "Disparar", "Reiniciar");
        Entradas.addListener(this, "RatonXIzq", "RatonXDer", "RatonYArriba", "RatonYAbajo");
    }

    @Override
    public void onAction(String Nombre, boolean EstaPresionado, float Tpf) {
        if (Nombre.equals("CaminarFrente")) Adelante = EstaPresionado;
        if (Nombre.equals("CaminarAtras")) Atras = EstaPresionado;
        if (Nombre.equals("GiroIzquierda")) Izquierda = EstaPresionado;
        if (Nombre.equals("GiroDerecha")) Derecha = EstaPresionado;
        if (Nombre.equals("Disparar")) Disparando = EstaPresionado; 
        if (Nombre.equals("Reiniciar")) Reiniciar = EstaPresionado; 
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

        if (DireccionCaminata.lengthSquared() > 0) {
            DireccionCaminata.normalizeLocal();
        }

        return DireccionCaminata;
    }

    public float getGiroX() { float val = DeltaRatonX; DeltaRatonX = 0; return val; }
    public float getGiroY() { float val = DeltaRatonY; DeltaRatonY = 0; return val; }
    public boolean getDisparando() { return Disparando; } 
    public boolean getReiniciar() { return Reiniciar; }

}
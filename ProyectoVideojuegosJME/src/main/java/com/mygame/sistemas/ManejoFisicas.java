package com.mygame.sistemas;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class ManejoFisicas {
    
    public static void ConfigurarEscena(Spatial Laberinto, Node NodoRaiz, BulletAppState Estado, AssetManager Assets) {
        Laberinto.setLocalTranslation(0, 0, 0);
        CollisionShape FormaReal = CollisionShapeFactory.createMeshShape(Laberinto);
        RigidBodyControl FisicoLaberinto = new RigidBodyControl(FormaReal, 0.0f);
        
        // --- SOLUCIÓN AL TARTAMUDEO ---
        // Le quitamos la fricción al suelo. Esto hace que la cápsula resbale 
        // sobre las uniones de los triángulos en lugar de tropezar.
        FisicoLaberinto.setFriction(0.0f); 
        
        Laberinto.addControl(FisicoLaberinto);
        NodoRaiz.attachChild(Laberinto);
        Estado.getPhysicsSpace().add(FisicoLaberinto);
        
        Estado.setDebugEnabled(false);
        
        DirectionalLight LuzPrincipal = new DirectionalLight();
        LuzPrincipal.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
        LuzPrincipal.setColor(ColorRGBA.White.mult(0.8f)); 
        NodoRaiz.addLight(LuzPrincipal);

        DirectionalLight LuzRelleno = new DirectionalLight();
        LuzRelleno.setDirection(new Vector3f(0.5f, 0.5f, 0.5f).normalizeLocal());
        LuzRelleno.setColor(ColorRGBA.White.mult(0.4f)); 
        NodoRaiz.addLight(LuzRelleno);

        AmbientLight LuzAmbiente = new AmbientLight();
        LuzAmbiente.setColor(ColorRGBA.White.mult(0.3f));
        NodoRaiz.addLight(LuzAmbiente);
    }

    public static Node AplicarFisicasPersonaje(Spatial Modelo, Node NodoRaiz, BulletAppState Estado) {
        Node NodoPersonaje = new Node("NodoHeroe");
        NodoRaiz.attachChild(NodoPersonaje);
        NodoPersonaje.attachChild(Modelo);
        
        // --- SOLUCIÓN AL FLOTE VISUAL ---
        // Bajamos el modelo en el eje Y (-0.6f) para que sus pies toquen el suelo.
        Modelo.setLocalTranslation(1.8f, -0.6f, -2.1f); 

        // Engordamos ligeramente el radio a 0.55f para darle más estabilidad al cilindro
        BetterCharacterControl ControlPersonaje = new BetterCharacterControl(0.55f, 1.8f, 80f);
        NodoPersonaje.addControl(ControlPersonaje);
        Estado.getPhysicsSpace().add(ControlPersonaje);
        
        return NodoPersonaje;
    }
}
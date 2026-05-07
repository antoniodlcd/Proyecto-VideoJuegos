package com.mygame;

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
    
     /* Configura las físicas de la escena (laberinto e iluminación) y del personaje (colisiones y gravedad) usando Bullet. */

    public static void ConfigurarEscena(Spatial Laberinto, Node NodoRaiz, BulletAppState Estado, AssetManager Assets) {
        Laberinto.setLocalTranslation(0, 0, 0);
        CollisionShape FormaReal = CollisionShapeFactory.createMeshShape(Laberinto);
        RigidBodyControl FisicoLaberinto = new RigidBodyControl(FormaReal, 0.0f);
        
        Laberinto.addControl(FisicoLaberinto);
        NodoRaiz.attachChild(Laberinto);
        Estado.getPhysicsSpace().add(FisicoLaberinto);
        
        Estado.setDebugEnabled(false);

        DirectionalLight Sol = new DirectionalLight();
        Sol.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
        NodoRaiz.addLight(Sol);

        AmbientLight LuzAmbiente = new AmbientLight();
        LuzAmbiente.setColor(ColorRGBA.White.mult(0.6f));
        NodoRaiz.addLight(LuzAmbiente);
    }

    public static Node AplicarFisicasPersonaje(Spatial Modelo, Node NodoRaiz, BulletAppState Estado) {
        Node NodoPersonaje = new Node("NodoHeroe");
        NodoRaiz.attachChild(NodoPersonaje);
        NodoPersonaje.attachChild(Modelo);
        Modelo.setLocalTranslation(1.8f, 0, -2.1f); 

        BetterCharacterControl ControlPersonaje = new BetterCharacterControl(0.5f, 1.8f, 80f);
        NodoPersonaje.addControl(ControlPersonaje);
        Estado.getPhysicsSpace().add(ControlPersonaje);
        
        return NodoPersonaje;
    }
}
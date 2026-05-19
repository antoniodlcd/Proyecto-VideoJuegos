package com.mygame;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.BetterCharacterControl; // <-- NUEVO: Para saber hacia dónde mira el personaje
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Cylinder; // nuevo para hacer mas visible el disparo

public class ManejoArmas {

    public static void DispararLaser(Camera Camara, Node NodoRaiz, Node NodoSoldado, AssetManager GestorRecursos) {
        
        // 1. ORIGEN: En lugar de la cámara, tomamos la posición del robot y le sumamos 1.5 en Y 
        // para que el disparo salga de la altura del pecho/arma, y no desde los tobillos.
        Vector3f Origen = NodoSoldado.getWorldTranslation().add(0, 1.5f, 0); 
        
        // 2. DIRECCIÓN: Le preguntamos a las físicas del robot hacia dónde está mirando su pecho.
        Vector3f Direccion = NodoSoldado.getControl(BetterCharacterControl.class).getViewDirection();
        
        
        // CÁLCULO MATEMÁTICO DEL RAYO
        Ray RayoLaser = new Ray(Origen, Direccion);
        CollisionResults Resultados = new CollisionResults();
        NodoRaiz.collideWith(RayoLaser, Resultados);

        // Determinamos dónde termina el rayo visualmente (100 unidades hacia el frente del robot)
        Vector3f PuntoDestino = Origen.add(Direccion.mult(100f)); 
        

        // --- NUEVA GENERACIÓN DEL LÁSER (USANDO UN CILINDRO 3D) --
        // 1. Calculamos la distancia exacta entre el arma y la pared para saber qué tan largo será 
        float DistanciaLaser = Origen.distance(PuntoDestino);
        
        // 2. Creamos un Cilindro 
        // Un radio de 0.05f nos dará un láser delgado pero tridimensional y sólido.
        Cylinder FormaCilindro = new Cylinder(8, 8, 0.07f, DistanciaLaser, true);
        Geometry GeoLaser = new Geometry("RayoVisual", FormaCilindro);
        
        // 3. Posicionamiento: A diferencia de la línea, el cilindro se crea desde su centro.
        // Así que encontramos el punto medio exacto entre el robot y la pared.
        Vector3f PuntoMedio = Origen.clone().interpolateLocal(PuntoDestino, 0.5f);
        GeoLaser.setLocalTranslation(PuntoMedio);
        
        // 4. Rotación: Hacemos que el cilindro apunte exactamente hacia donde disparamos
        GeoLaser.lookAt(PuntoDestino, Vector3f.UNIT_Y);

        Material MatLaser = new Material(GestorRecursos, "Common/MatDefs/Misc/Unshaded.j3md");
        MatLaser.setColor("Color", ColorRGBA.Red);
        GeoLaser.setMaterial(MatLaser);
        
        
        if (Resultados.size() > 0) {
            CollisionResult GolpeMasCercano = Resultados.getClosestCollision();
            
            // Filtro para ignorar colisiones con nuestro propio cuerpo
            if (!GolpeMasCercano.getGeometry().hasAncestor(NodoSoldado)) {
                String NombreObjetivo = GolpeMasCercano.getGeometry().getName();
                System.out.println("¡PUM! Impacto confirmado contra: " + NombreObjetivo);
                
                // Cortamos la línea roja exactamente en la pared o enemigo que golpeamos
                //PuntoDestino = GolpeMasCercano.getContactPoint();
            }
        }

        
        // AUTODESTRUCCIÓN DEL LÁSER
        GeoLaser.addControl(new AbstractControl() {
            float TiempoDeVida = 0.1f; 

            @Override
            protected void controlUpdate(float Tpf) {
                TiempoDeVida -= Tpf;
                if (TiempoDeVida <= 0) {
                    spatial.removeFromParent(); 
                }
            }

            @Override
            protected void controlRender(com.jme3.renderer.RenderManager rm, com.jme3.renderer.ViewPort vp) { }
        });

        NodoRaiz.attachChild(GeoLaser);
    }
}
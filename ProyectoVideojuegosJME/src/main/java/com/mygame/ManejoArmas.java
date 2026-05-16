package com.mygame;

import com.jme3.asset.AssetManager;
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
import com.jme3.scene.shape.Line;

/* Clase responsable de la lógica de combate, cálculo de impactos mediante Raycasting y generación de efectos visuales. */
public class ManejoArmas {

    // NUEVO: Ahora pedimos el AssetManager como parámetro para poder cargar el color del láser
    public static void DispararLaser(Camera Camara, Node NodoRaiz, Node NodoSoldado, AssetManager GestorRecursos) {
        
        Vector3f Origen = Camara.getLocation();
        Vector3f Direccion = Camara.getDirection();
        
        // 1. CÁLCULO MATEMÁTICO DEL RAYO
        Ray RayoLaser = new Ray(Origen, Direccion);
        CollisionResults Resultados = new CollisionResults();
        NodoRaiz.collideWith(RayoLaser, Resultados);

        // Determinamos dónde termina el rayo visualmente. 
        // Si no golpea nada, lo extendemos a 100 unidades hacia adelante.
        Vector3f PuntoDestino = Origen.add(Direccion.mult(100f)); 

        if (Resultados.size() > 0) {
            CollisionResult GolpeMasCercano = Resultados.getClosestCollision();
            
            if (!GolpeMasCercano.getGeometry().hasAncestor(NodoSoldado)) {
                String NombreObjetivo = GolpeMasCercano.getGeometry().getName();
                System.out.println("¡PUM! Impacto confirmado contra: " + NombreObjetivo);
                
                // Si golpeamos algo, el láser visual debe detenerse exactamente en el punto de contacto
                PuntoDestino = GolpeMasCercano.getContactPoint();
            }
        }

        // 2. GENERACIÓN DEL EFECTO VISUAL (LA LÍNEA LÁSER)
        // Creamos una forma geométrica de tipo línea que va desde la cámara hasta el punto de destino
        Line FormaLinea = new Line(Origen, PuntoDestino);
        Geometry GeoLaser = new Geometry("RayoVisual", FormaLinea);
        
        // Le aplicamos un material "Unshaded" (sin sombras) para que brille intensamente de color rojo
        Material MatLaser = new Material(GestorRecursos, "Common/MatDefs/Misc/Unshaded.j3md");
        MatLaser.setColor("Color", ColorRGBA.Red);
        GeoLaser.setMaterial(MatLaser);
        
        // 3. AUTODESTRUCCIÓN DEL LÁSER (Limpieza de memoria)
        // Le añadimos un controlador personalizado a la línea geométrica
        GeoLaser.addControl(new AbstractControl() {
            float TiempoDeVida = 0.1f; // El láser durará 0.1 segundos en pantalla

            @Override
            protected void controlUpdate(float Tpf) {
                TiempoDeVida -= Tpf;
                if (TiempoDeVida <= 0) {
                    // Cuando el tiempo se agota, el láser se borra a sí mismo del mapa
                    spatial.removeFromParent(); 
                }
            }

            @Override
            protected void controlRender(com.jme3.renderer.RenderManager rm, com.jme3.renderer.ViewPort vp) { }
        });

        // Finalmente, adjuntamos la línea visual al mundo para que la cámara la pueda renderizar
        NodoRaiz.attachChild(GeoLaser);
    }
}
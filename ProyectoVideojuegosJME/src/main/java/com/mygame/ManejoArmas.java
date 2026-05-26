package com.mygame;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Cylinder;
import java.util.List;

public class ManejoArmas {

    //  Ahora pedimos el PhysicsSpace en lugar del NodoRaiz para el cálculo
    public static void DispararLaser(Camera Camara, PhysicsSpace EspacioFisico, Node NodoSoldado, AssetManager GestorRecursos, Node NodoRaiz) {
        
        Vector3f Origen = NodoSoldado.getWorldTranslation().add(0, 1.5f, 0); 
        Vector3f Direccion = NodoSoldado.getControl(BetterCharacterControl.class).getViewDirection();
        Vector3f DestinoFinal = Origen.add(Direccion.mult(100f)); 
        
        // --- RAYCAST FÍSICO INSTANTÁNEO ---
        List<PhysicsRayTestResult> Resultados = EspacioFisico.rayTest(Origen, DestinoFinal);
        
        float FraccionMasCercana = 1.0f;
        Spatial ObjetoGolpeado = null;

        for (PhysicsRayTestResult impacto : Resultados) {
            // Extraemos el objeto 3D real que está conectado a esa caja de colisión
            Spatial objetoFisico = (Spatial) impacto.getCollisionObject().getUserObject();
            
            // Verificamos que no sea nuestro propio robot
            if (objetoFisico != null && objetoFisico != NodoSoldado) {
                if (impacto.getHitFraction() < FraccionMasCercana) {
                    FraccionMasCercana = impacto.getHitFraction();
                    ObjetoGolpeado = objetoFisico;
                }
            }
        }

        // Cortamos la línea roja usando la fracción de distancia donde ocurrió el impacto
        Vector3f PuntoDestinoLaser = Origen.add(Direccion.mult(100f * FraccionMasCercana));
        float DistanciaLaser = Origen.distance(PuntoDestinoLaser);
        
        // Generación visual del cilindro...
        Cylinder FormaCilindro = new Cylinder(8, 8, 0.07f, DistanciaLaser, true);
        Geometry GeoLaser = new Geometry("RayoVisual", FormaCilindro);
        Vector3f PuntoMedio = Origen.clone().interpolateLocal(PuntoDestinoLaser, 0.5f);
        GeoLaser.setLocalTranslation(PuntoMedio);
        GeoLaser.lookAt(PuntoDestinoLaser, Vector3f.UNIT_Y);

        Material MatLaser = new Material(GestorRecursos, "Common/MatDefs/Misc/Unshaded.j3md");
        MatLaser.setColor("Color", ColorRGBA.Red);
        GeoLaser.setMaterial(MatLaser);
        
        if (ObjetoGolpeado != null) {
            // ¡Más adelante aquí pondremos la lógica de restar vida!
            System.out.println("¡Impacto físico contra: " + ObjetoGolpeado.getName() + "!");
        }
        
        GeoLaser.addControl(new AbstractControl() {
            float TiempoDeVida = 0.1f; 
            @Override
            protected void controlUpdate(float Tpf) {
                TiempoDeVida -= Tpf;
                if (TiempoDeVida <= 0) spatial.removeFromParent(); 
            }
            @Override
            protected void controlRender(com.jme3.renderer.RenderManager rm, com.jme3.renderer.ViewPort vp) { }
        });

        NodoRaiz.attachChild(GeoLaser);
    }
}
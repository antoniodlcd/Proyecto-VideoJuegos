package com.mygame;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import java.util.List;

/* Clase encargada de gestionar el comportamiento de la cámara orbital, incluyendo la rotación y la detección de colisiones con el entorno. */
public class ControlCamara {

    private static float AnguloActual = 0; 
    private static final float RadioMaximo = 8.0f; 
    private static final float AlturaIdeal = 5.5f; 
    private static final float VelocidadRotacion = 2.5f;
    private static final float MargenSeguridad = 0.8f; 

    public static void ActualizarCamaraFisica(Camera Cam, Spatial Personaje, float Tpf, boolean Izq, boolean Der, PhysicsSpace EspacioFisico) {
        if (Personaje == null) return;

        if (Izq) AnguloActual += VelocidadRotacion * Tpf;
        if (Der) AnguloActual -= VelocidadRotacion * Tpf;

        Vector3f PosCabeza = Personaje.getWorldTranslation().add(0, 1.8f, 0); 

        float X_Ideal = PosCabeza.x + RadioMaximo * FastMath.sin(AnguloActual);
        float Z_Ideal = PosCabeza.z + RadioMaximo * FastMath.cos(AnguloActual);
        
        Vector3f PosicionIdeal = new Vector3f(X_Ideal, PosCabeza.y + (AlturaIdeal - 1.8f), Z_Ideal);

        List<PhysicsRayTestResult> Resultados = EspacioFisico.rayTest(PosCabeza, PosicionIdeal);
        
        float FraccionMasCercana = 1.0f;
        for (PhysicsRayTestResult hit : Resultados) {
            if (hit.getCollisionObject().getUserObject() != Personaje) {
                if (hit.getHitFraction() < FraccionMasCercana) {
                    FraccionMasCercana = hit.getHitFraction();
                }
            }
        }

        Vector3f PosicionFinal;
        if (FraccionMasCercana < 1.0f) {
            Vector3f DireccionRayo = PosicionIdeal.subtract(PosCabeza);
            float DistanciaTotal = DireccionRayo.length();
            DireccionRayo.normalizeLocal();

            float DistanciaSegura = (DistanciaTotal * FraccionMasCercana) - MargenSeguridad;
            if (DistanciaSegura < 1.5f) DistanciaSegura = 1.5f; 

            PosicionFinal = PosCabeza.add(DireccionRayo.mult(DistanciaSegura));
        } else {
            PosicionFinal = PosicionIdeal;
        }

        Cam.setLocation(PosicionFinal);
        Cam.lookAt(PosCabeza, Vector3f.UNIT_Y);
    }
}
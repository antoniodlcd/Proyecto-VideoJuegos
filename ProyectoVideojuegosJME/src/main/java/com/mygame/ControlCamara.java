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

    private static float AnguloHorizontal = 0;
    private static float AnguloVertical = 0.2f; // inicia ligeramente inclinada hacia abajo
    private static final float RADIO_MAXIMO = 15.0f; 
    private static final float ALTURA_IDEAL = 5.5f; 
    private static final float VELOCIDAD_ROTACION = 3.5f;
    private static final float MARGEN_SEGURIDAD = 0.8f; 

    public static void ActualizarCamaraFisica(Camera Cam, Spatial Personaje, float Tpf, float deltaX, float deltaY, PhysicsSpace EspacioFisico) {
        if (Personaje == null) return;

        // sumar el movimiento del raton
        AnguloHorizontal -= deltaX * VELOCIDAD_ROTACION;
        AnguloVertical -= deltaY * VELOCIDAD_ROTACION;
        
        // evita que la camara de volteretas
        AnguloVertical = FastMath.clamp(AnguloVertical, -0.2f, FastMath.HALF_PI - 0.1f);

        Vector3f PosCabeza = Personaje.getWorldTranslation().add(0, 1.8f, 0); 
        
        float DistanciaProyectadaXZ = RADIO_MAXIMO * FastMath.cos(AnguloVertical);

        float X_Ideal = PosCabeza.x + DistanciaProyectadaXZ * FastMath.sin(AnguloHorizontal);
        float Z_Ideal = PosCabeza.z + DistanciaProyectadaXZ * FastMath.cos(AnguloHorizontal);
        float Y_Ideal = PosCabeza.y + RADIO_MAXIMO * FastMath.sin(AnguloVertical);
        
        Vector3f PosicionIdeal = new Vector3f(X_Ideal, Y_Ideal, Z_Ideal);

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

            float DistanciaSegura = (DistanciaTotal * FraccionMasCercana) - MARGEN_SEGURIDAD;
            if (DistanciaSegura < 1.5f) DistanciaSegura = 1.5f; 

            PosicionFinal = PosCabeza.add(DireccionRayo.mult(DistanciaSegura));
        } else {
            PosicionFinal = PosicionIdeal;
        }

        Cam.setLocation(PosicionFinal);
        Cam.lookAt(PosCabeza, Vector3f.UNIT_Y);
    }
}
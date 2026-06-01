package com.mygame.vista;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import java.util.List;

public class ControlCamara {

    private static float AnguloHorizontal = 0;
    // Iniciamos la cámara un poco más arriba
    private static float AnguloVertical = 0.2f; 
    private static final float RADIO_MAXIMO = 15.0f; 
    private static final float ALTURA_IDEAL = 5.5f; 
    private static final float VELOCIDAD_ROTACION = 3.5f;
    private static final float MARGEN_SEGURIDAD = 0.8f; 

    public static void ActualizarCamaraFisica(Camera Cam, Spatial Personaje, float Tpf, float deltaX, float deltaY, PhysicsSpace EspacioFisico) {
        if (Personaje == null) return;

        AnguloHorizontal -= deltaX * VELOCIDAD_ROTACION;
        AnguloVertical -= deltaY * VELOCIDAD_ROTACION;
        
        // --- PROTECCIÓN DEL SUELO INVISIBLE ---
        // Cambiamos el límite inferior a 0.05f. Esto le prohíbe a la cámara 
        // bajar más allá de la cintura, evitando que choque con el piso.
        AnguloVertical = FastMath.clamp(AnguloVertical, 0.05f, FastMath.HALF_PI - 0.1f);

        Vector3f PosCabeza = Personaje.getWorldTranslation().add(0, 1.8f, 0); 
        
        float DistanciaProyectadaXZ = RADIO_MAXIMO * FastMath.cos(AnguloVertical);

        float X_Ideal = PosCabeza.x + DistanciaProyectadaXZ * FastMath.sin(AnguloHorizontal);
        float Z_Ideal = PosCabeza.z + DistanciaProyectadaXZ * FastMath.cos(AnguloHorizontal);
        float Y_Ideal = PosCabeza.y + RADIO_MAXIMO * FastMath.sin(AnguloVertical);
        
        Vector3f PosicionIdeal = new Vector3f(X_Ideal, Y_Ideal, Z_Ideal);

        List<PhysicsRayTestResult> Resultados = EspacioFisico.rayTest(PosCabeza, PosicionIdeal);
        
        float FraccionMasCercana = 1.0f;
        for (PhysicsRayTestResult hit : Resultados) {
            Spatial objetoChocado = (Spatial) hit.getCollisionObject().getUserObject();
            
            if (objetoChocado != null && objetoChocado != Personaje) {
                if (objetoChocado.getUserData("Vida") == null) {
                    
                    // --- MAGIA TRIGONOMÉTRICA (Ignorar el piso) ---
                    // Obtenemos hacia dónde mira el objeto golpeado. Si mira hacia arriba (Y > 0.8),
                    // sabemos que es el piso y la cámara lo atraviesa sin dar tirones.
                    Vector3f normal = hit.getHitNormalLocal();
                    if (Math.abs(normal.y) < 0.8f) {
                        if (hit.getHitFraction() < FraccionMasCercana) {
                            FraccionMasCercana = hit.getHitFraction();
                        }
                    }
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
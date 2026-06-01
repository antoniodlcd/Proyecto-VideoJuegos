package com.mygame.entidades;

import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.mygame.Constantes;
import com.mygame.Main;
import java.util.ArrayList;
import java.util.List;

public class IAVillanos {

    public static void PerseguirHeroe(Spatial Soldado, ArrayList<Spatial> ListaVillanos, float Tpf, Main app) {
        Vector3f PosHeroe = Soldado.getWorldTranslation();
        
        for (Spatial enemigo : ListaVillanos) {
            float velocidad = Constantes.ENEMIGO_VELOCIDAD_TANQUE;
            if (enemigo.getName().equals("Arania")) {
                velocidad = Constantes.ENEMIGO_VELOCIDAD_ARANIA;
            }    
            
            // =======================================================
            // 1. SISTEMA DE VISIÓN (RAYCAST)
            // =======================================================
            boolean veAlJugador = false;
            float distanciaAlJugador = enemigo.getWorldTranslation().distance(PosHeroe);
            
            if (distanciaAlJugador < 40f) { 
                Vector3f OjoEnemigo = enemigo.getWorldTranslation().add(0, 1.5f, 0);
                Vector3f OjoJugador = PosHeroe.add(0, 1.5f, 0);
                
                List<PhysicsRayTestResult> impactos = app.getEstadoFisicas().getPhysicsSpace().rayTest(OjoEnemigo, OjoJugador);
                float impactoMasCercano = 1.0f;
                Spatial objetoVisto = null;
                
                for (PhysicsRayTestResult hit : impactos) {
                    Spatial objeto = (Spatial) hit.getCollisionObject().getUserObject();
                    if (objeto != null && objeto != enemigo) {
                        if (hit.getHitFraction() < impactoMasCercano) {
                            impactoMasCercano = hit.getHitFraction();
                            objetoVisto = objeto;
                        }
                    }
                }
                
                if (objetoVisto == Soldado) {
                    veAlJugador = true;
                }
            }

            // =======================================================
            // 2. TOMA DE DECISIONES (Caza vs Centinela)
            // =======================================================
            BetterCharacterControl fisicas = enemigo.getControl(BetterCharacterControl.class);
            
            if (veAlJugador) {
                // MODO CAZA: Corre hacia el jugador
                if (fisicas != null) {
                    Vector3f Direccion = PosHeroe.subtract(enemigo.getWorldTranslation());
                    Direccion.setY(0); 
                    
                    if (distanciaAlJugador > Constantes.ENEMIGO_DISTANCIA_ATAQUE) {
                        Direccion.normalizeLocal();
                        fisicas.setViewDirection(Direccion);
                        fisicas.setWalkDirection(Direccion.mult(velocidad));
                    } else {
                        fisicas.setWalkDirection(Vector3f.ZERO);
                        Direccion.normalizeLocal();
                        fisicas.setViewDirection(Direccion);
                        
                        // Golpear al héroe
                        if (app.getTiempoUltimoGolpe() >= Constantes.JUGADOR_TIEMPO_INVULNERABILIDAD) {
                            app.recibirDanio(Constantes.ENEMIGO_DANIO_GOLPE);
                            app.setTiempoUltimoGolpe(0f);
                            System.out.println("¡Un enemigo te ha golpeado!");
                        }
                    }
                }
            } else {
                // MODO CENTINELA: Gira lentamente buscando al jugador
                Float anguloActual = enemigo.getUserData("AnguloVigilancia");
                if (anguloActual == null) anguloActual = 0f;
                
                anguloActual += Tpf * 1.5f; // Velocidad de rotación
                if (anguloActual > FastMath.TWO_PI) anguloActual -= FastMath.TWO_PI;
                
                enemigo.setUserData("AnguloVigilancia", anguloActual);
                
                float x = FastMath.sin(anguloActual);
                float z = FastMath.cos(anguloActual);
                Vector3f direccionVigilancia = new Vector3f(x, 0, z).normalizeLocal();
                
                if(fisicas != null) {
                    fisicas.setViewDirection(direccionVigilancia);
                    fisicas.setWalkDirection(Vector3f.ZERO); // Cero físicas = Cero Lag
                }
            }
        }
    }
}
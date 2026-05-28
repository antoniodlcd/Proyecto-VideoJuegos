package com.mygame;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.ArrayList;

public class IAVillanos {

    // Pasamos el Soldado (Héroe), los enemigos y el Tpf

    public static void PerseguirHeroe(Spatial Soldado, ArrayList<Spatial> ListaVillanos, float Tpf, Main app) {
        // Obtenemos la posición del héroe
        Vector3f PosHeroe = Soldado.getWorldTranslation();
        
        for (Spatial enemigo : ListaVillanos) {
            // Identificamos qué tipo de enemigo es por su nombre para asignarle su velocidad
            float velocidad = 2.5f;
            if (enemigo.getName().equals("Arania")) {
                velocidad = 4.5f;
            }    
            MoverEnemigo(enemigo, PosHeroe, velocidad);
            
            float distancia = enemigo.getWorldTranslation().distance(PosHeroe);

            if (distancia < 4.0f) {
                if (app.getTiempoUltimoGolpe() >= 1.5f) {
                    app.recibirDanio(20);
                    app.setTiempoUltimoGolpe(0f);
                    System.out.println("¡Un enemigo te ha golpeado!");
                }
            }
        }
    }

 private static void MoverEnemigo(Spatial Enemigo, Vector3f Objetivo, float Velocidad) {
        BetterCharacterControl fisiscasEnemigo = Enemigo.getControl(BetterCharacterControl.class);
        
        if (fisiscasEnemigo != null) {
            Vector3f Direccion = Objetivo.subtract(Enemigo.getWorldTranslation());
            Direccion.setY(0); // Mantener el movimiento en el suelo

            // Medimos a qué distancia exacta se encuentra este enemigo del jugador
            float DistanciaAlHeroe = Direccion.length();
            
            // Esto asegura que no se encimen y destruyan el rendimiento de las físicas.
            if (DistanciaAlHeroe > 4.0f) {
                
                // Si están lejos, normalizamos para obtener solo la dirección y caminamos
                Direccion.normalizeLocal();
                fisiscasEnemigo.setViewDirection(Direccion);
                fisiscasEnemigo.setWalkDirection(Direccion.mult(Velocidad));
                
            } else {
                // Si ya llegaron al perímetro seguro de 4 metros, se detienen
                fisiscasEnemigo.setWalkDirection(Vector3f.ZERO);
                
                // Opcional: Hacemos que, aunque estén detenidos, sigan girando para mirar 
                // al jugador de forma segura 
                if (DistanciaAlHeroe > 0.1f) {
                    Direccion.normalizeLocal();
                    fisiscasEnemigo.setViewDirection(Direccion);
                }
            }
        }
    }
}
package com.mygame;

import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.ArrayList;

public class IAVillanos {

    // Pasamos el Soldado (Héroe), los enemigos y el Tpf

    public static void PerseguirHeroe(Spatial Soldado, ArrayList<Spatial> ListaVillanos, float Tpf) {
        // Obtenemos la posición del héroe
        Vector3f PosHeroe = Soldado.getWorldTranslation();
        
        for (Spatial enemigo : ListaVillanos) {
            // Identificamos qué tipo de enemigo es por su nombre para asignarle su velocidad
            float velocidad = 2.5f;
            if (enemigo.getName().equals("Arania")) {
                velocidad = 4.5f;
            }    
            MoverEnemigo(enemigo, PosHeroe, velocidad);
        }
        
    }

    private static void MoverEnemigo(Spatial Enemigo, Vector3f Objetivo, float Velocidad) {
        // 1. Obtener el control de físicas que tiene asignado el enemigo
        BetterCharacterControl fisiscasEnemigo = Enemigo.getControl(BetterCharacterControl.class);
        
        if (fisiscasEnemigo != null) {
            // 2. Calcular la dirección hacia el héroe
            Vector3f Direccion = Objetivo.subtract(Enemigo.getWorldTranslation()).normalizeLocal();
            Direccion.setY(0); // Mantener el movimiento en el suelo

            // 3. Rotar el modelo para que mire al héroe
            Enemigo.lookAt(Objetivo, Vector3f.UNIT_Y);

            // 4. LA CLAVE: En lugar de usar Enemigo.move(), le damos la velocidad al motor de físicas
            // BetterCharacterControl ya multiplica internamente por el Tpf, así que no hace falta pasarlo aquí
            Vector3f VectorMovimiento = Direccion.mult(Velocidad);
            fisiscasEnemigo.setWalkDirection(VectorMovimiento);
        }
    }
}
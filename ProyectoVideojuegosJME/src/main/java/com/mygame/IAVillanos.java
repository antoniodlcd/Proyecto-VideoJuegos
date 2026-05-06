package com.mygame;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class IAVillanos {

    public static void PerseguirHeroe(Spatial Soldado, Spatial Arania, Spatial Tanque, float Tpf) {
        Vector3f PosHeroe = Soldado.getWorldTranslation();
        MoverEnemigo(Arania, PosHeroe, 4.5f, Tpf);
        MoverEnemigo(Tanque, PosHeroe, 2.5f, Tpf);
    }

    private static void MoverEnemigo(Spatial Enemigo, Vector3f Objetivo, float Velocidad, float Tpf) {
        Vector3f Direccion = Objetivo.subtract(Enemigo.getWorldTranslation()).normalizeLocal();
        Direccion.setY(0);
        Enemigo.lookAt(Objetivo, Vector3f.UNIT_Y);
        Enemigo.move(Direccion.mult(Velocidad * Tpf));
    }
}
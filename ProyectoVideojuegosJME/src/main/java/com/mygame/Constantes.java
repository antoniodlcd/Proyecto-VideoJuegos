
package com.mygame;


public class Constantes {
    
    // configuracion del jugador
    public static final int   JUGADOR_VIDA_INICIAL = 100;
    public static final int   JUGADOR_ALERTA_VIDA_BAJA = 30;
    public static final float JUGADOR_VELOCIDAD = 15.0f;
    public static final float JUGADOR_TIEMPO_INVULNERABILIDAD = 1.5f;
    
    // configuracion de enemigos e ia
    public static final float ENEMIGO_VELOCIDAD_ARANIA = 4.5f;
    public static final float ENEMIGO_VELOCIDAD_TANQUE = 3f;
    public static final float ENEMIGO_DISTANCIA_ATAQUE = 4.0f;
    public static final int   ENEMIGO_DANIO_GOLPE = 20;
    
    // --- NUEVAS REGLAS DEL TANQUE BOLSA ---
    public static final float ENEMIGO_TANQUE_DISTANCIA_DISPARO = 35.0f; // Te dispara desde lejos
    public static final float ENEMIGO_TANQUE_CADENCIA = 3.0f; // Tarda 3 segundos en recargar su arma
    public static final int   ENEMIGO_TANQUE_DANIO_DISPARO = 10; // Daño del proyectil
    
    // configuracion de oleadas
    public static final int OLEADAS_PARA_GANAR = 10;
    public static final int OLEADA_ENEMIGOS_BASE = 5;
    public static final int OLEADA_ENEMIGOS_MULTIPLICADOR = 2;
    public static final int OLEADA_VIDA_BASE = 50;
    public static final int OLEADA_VIDA_AUMENTO = 50;
    
    // configuracion de armas
    public static final float ARMA_DURACION_VISUAL_LASER = 0.1f;
    public static final float ARMA_CADENCIA_TIRO = 0.5f;
    public static final float ARMA_ALCANCE = 100.0f;
    public static final int   ARMA_DANIO = 25;
    
}

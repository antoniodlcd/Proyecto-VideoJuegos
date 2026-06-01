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
            boolean esTanque = enemigo.getName().equals("Tanque");
            float velocidad = esTanque ? Constantes.ENEMIGO_VELOCIDAD_TANQUE : Constantes.ENEMIGO_VELOCIDAD_ARANIA;
            
            // 1. LEER LA MEMORIA DEL ENEMIGO
            Boolean estaEnfurecido = enemigo.getUserData("Enfurecido");
            if (estaEnfurecido == null) estaEnfurecido = false; 
            
            float distanciaAlJugador = enemigo.getWorldTranslation().distance(PosHeroe);

            // 2. SISTEMA DE VISIÓN
            if (!estaEnfurecido && distanciaAlJugador < 40f) { 
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
                    estaEnfurecido = true;
                    enemigo.setUserData("Enfurecido", true);
                }
            }

            // 3. TOMA DE DECISIONES Y MOVIMIENTO FÍSICO
            BetterCharacterControl fisicas = enemigo.getControl(BetterCharacterControl.class);
            
            if (estaEnfurecido) {
                if (fisicas != null) {
                    Vector3f Direccion = PosHeroe.subtract(enemigo.getWorldTranslation());
                    Direccion.setY(0); 
                    
                    // --- LA CLAVE: ¿A qué distancia debe detenerse? ---
                    // El Tanque se detiene a 25 metros para disparar. La Araña avanza hasta los 4 metros.
                    float distanciaParaAtacar = esTanque ? Constantes.ENEMIGO_TANQUE_DISTANCIA_DISPARO : Constantes.ENEMIGO_DISTANCIA_ATAQUE;
                    
                    if (distanciaAlJugador > distanciaParaAtacar) {
                        
                        // Si aún no llega a su distancia ideal, sigue caminando y esquivando compañeros
                        Direccion.normalizeLocal();
                        for (Spatial otroEnemigo : ListaVillanos) {
                            if (otroEnemigo != enemigo) {
                                float distanciaEntreEllos = enemigo.getWorldTranslation().distance(otroEnemigo.getWorldTranslation());
                                if (distanciaEntreEllos < 2.5f) {
                                    Vector3f FuerzaSeparacion = enemigo.getWorldTranslation().subtract(otroEnemigo.getWorldTranslation());
                                    FuerzaSeparacion.setY(0);
                                    if (FuerzaSeparacion.lengthSquared() > 0) {
                                        FuerzaSeparacion.normalizeLocal();
                                        Direccion.addLocal(FuerzaSeparacion.mult(0.8f)); 
                                    }
                                }
                            }
                        }
                        Direccion.normalizeLocal();
                        fisicas.setViewDirection(Direccion);
                        fisicas.setWalkDirection(Direccion.mult(velocidad));
                        
                    } else {
                        // ¡ESTÁ EN POSICIÓN DE ATAQUE! Se detiene.
                        fisicas.setWalkDirection(Vector3f.ZERO);
                        Direccion.normalizeLocal();
                        fisicas.setViewDirection(Direccion); // Mirarte fijamente
                        
                        if (esTanque) {
                            // --- LÓGICA DE DISPARO DEL TANQUE BOLSA ---
                            // Leemos su cronómetro interno
                            Float tiempoUltimoTiro = enemigo.getUserData("TiempoUltimoTiro");
                            // Si es la primera vez que ataca, le damos el valor de cadencia para que dispare de inmediato
                            if (tiempoUltimoTiro == null) tiempoUltimoTiro = Constantes.ENEMIGO_TANQUE_CADENCIA; 
                            
                            tiempoUltimoTiro += Tpf; // El reloj avanza
                            
                            // Si ya pasaron los 2 segundos...
                            if (tiempoUltimoTiro >= Constantes.ENEMIGO_TANQUE_CADENCIA) {
                                DispararProyectilTanque(enemigo, Soldado, app);
                                tiempoUltimoTiro = 0f; // Reiniciamos el reloj tras disparar
                            }
                            // Guardamos el tiempo actualizado en su memoria
                            enemigo.setUserData("TiempoUltimoTiro", tiempoUltimoTiro);
                            
                        } else {
                            // --- LÓGICA DE MORDIDA DE LA ARAÑA ---
                            if (app.getTiempoUltimoGolpe() >= Constantes.JUGADOR_TIEMPO_INVULNERABILIDAD) {
                                app.recibirDanio(Constantes.ENEMIGO_DANIO_GOLPE);
                                app.setTiempoUltimoGolpe(0f);
                                System.out.println("¡La Araña te ha mordido!");
                            }
                        }
                    }
                }
            } else {
                // MODO CENTINELA (Vigilancia inactiva)
                Float anguloActual = enemigo.getUserData("AnguloVigilancia");
                if (anguloActual == null) anguloActual = 0f;
                anguloActual += Tpf * 1.5f; 
                if (anguloActual > FastMath.TWO_PI) anguloActual -= FastMath.TWO_PI;
                enemigo.setUserData("AnguloVigilancia", anguloActual);
                
                float x = FastMath.sin(anguloActual);
                float z = FastMath.cos(anguloActual);
                if(fisicas != null) {
                    fisicas.setViewDirection(new Vector3f(x, 0, z).normalizeLocal());
                    fisicas.setWalkDirection(Vector3f.ZERO); 
                }
            }
        }
    }

    // =========================================================================
    // MÉTODO INDEPENDIENTE PARA CREAR EL DISPARO Y CALCULAR EL DAÑO
    // =========================================================================
    private static void DispararProyectilTanque(Spatial Tanque, Spatial Heroe, Main app) {
        // 1. Matemáticas del cañón
        Vector3f Origen = Tanque.getWorldTranslation().add(0, 1.2f, 0); // Altura media de la bolsa
        Vector3f DestinoHeroe = Heroe.getWorldTranslation().add(0, 1.5f, 0); // Apunta a tu pecho
        Vector3f Direccion = DestinoHeroe.subtract(Origen).normalizeLocal();
        Vector3f RangoMaximo = Origen.add(Direccion.mult(Constantes.ENEMIGO_TANQUE_DISTANCIA_DISPARO));
        
        // 2. Disparamos un raycast físico instantáneo
        List<PhysicsRayTestResult> Resultados = app.getEstadoFisicas().getPhysicsSpace().rayTest(Origen, RangoMaximo);
        
        float FraccionMasCercana = 1.0f;
        Spatial ObjetoGolpeado = null;

        for (PhysicsRayTestResult impacto : Resultados) {
            Spatial objetoFisico = (Spatial) impacto.getCollisionObject().getUserObject();
            if (objetoFisico != null && objetoFisico != Tanque) {
                if (impacto.getHitFraction() < FraccionMasCercana) {
                    FraccionMasCercana = impacto.getHitFraction();
                    ObjetoGolpeado = objetoFisico;
                }
            }
        }

        // 3. Efecto Visual: Un láser verde tóxico rápido
        Vector3f PuntoImpacto = Origen.add(Direccion.mult(Constantes.ENEMIGO_TANQUE_DISTANCIA_DISPARO * FraccionMasCercana));
        float DistanciaLaser = Origen.distance(PuntoImpacto);
        
        com.jme3.scene.shape.Cylinder FormaCilindro = new com.jme3.scene.shape.Cylinder(8, 8, 0.08f, DistanciaLaser, true);
        com.jme3.scene.Geometry GeoLaser = new com.jme3.scene.Geometry("LaserToxico", FormaCilindro);
        GeoLaser.setLocalTranslation(Origen.clone().interpolateLocal(PuntoImpacto, 0.5f));
        GeoLaser.lookAt(PuntoImpacto, Vector3f.UNIT_Y);

        com.jme3.material.Material MatLaser = new com.jme3.material.Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        MatLaser.setColor("Color", com.jme3.math.ColorRGBA.Green); // Color distintivo para el enemigo
        GeoLaser.setMaterial(MatLaser);
        
        GeoLaser.addControl(new com.jme3.scene.control.AbstractControl() {
            float TiempoDeVida = 0.15f; 
            @Override
            protected void controlUpdate(float tpf) {
                TiempoDeVida -= tpf;
                if (TiempoDeVida <= 0) spatial.removeFromParent(); 
            }
            @Override
            protected void controlRender(com.jme3.renderer.RenderManager rm, com.jme3.renderer.ViewPort vp) { }
        });
        app.getRootNode().attachChild(GeoLaser);

        // 4. Calcular el Daño (Revisar si el rayo chocó contigo o con la pared)
        if (ObjetoGolpeado != null) {
            // Subimos por la estructura de Nodos para ver si lo que golpeó pertenece al Héroe
            boolean esHeroe = false;
            Spatial nodoActual = ObjetoGolpeado;
            while (nodoActual != null) {
                if (nodoActual == Heroe) {
                    esHeroe = true;
                    break;
                }
                nodoActual = nodoActual.getParent();
            }
            
            if (esHeroe) {
                // Restamos exactamente los 5 de daño
                app.recibirDanio(Constantes.ENEMIGO_TANQUE_DANIO_DISPARO);
                System.out.println("¡El Tanque Bolsa te ha disparado!");
            } else {
                System.out.println("El disparo del Tanque se estrelló contra el muro.");
            }
        }
    }
}
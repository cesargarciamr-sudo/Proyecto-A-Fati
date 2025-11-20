package com.mycompany.asteroids.entity;

import com.mycompany.asteroids.Game;
import com.mycompany.asteroids.WorldPanel;
import com.mycompany.asteroids.util.Vector2;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * Representa una nave alienígena en el mundo del juego.
 * Este enemigo aguanta 3 impactos y cambia su velocidad y posición
 * aleatoriamente cada 5 segundos. También dispara al jugador.
 * 
 * @author Fátima Valeria Bocanegra Costilla
 */
public class AlienShip extends Entity {
    
    /**
     * Número de impactos que puede aguantar antes de ser destruida.
     */
    private static final int MAX_HITS = 3;
    
    /**
     * Tiempo en frames entre cambios de velocidad/posición (5 segundos a 60 FPS).
     */
    private static final int CHANGE_INTERVAL = 300; // 5 segundos * 60 FPS
    
    /**
     * Velocidad mínima de movimiento.
     */
    private static final double MIN_VELOCITY = 1.0;
    
    /**
     * Velocidad máxima de movimiento.
     */
    private static final double MAX_VELOCITY = 2.5;
    
    /**
     * Variación de velocidad.
     */
    private static final double VELOCITY_VARIANCE = MAX_VELOCITY - MIN_VELOCITY;
    
    /**
     * Radio de colisión de la nave alienígena.
     */
    private static final double COLLISION_RADIUS = 20.0;
    
    /**
     * Puntos otorgados al destruir la nave.
     */
    private static final int KILL_POINTS = 150;
    
    /**
     * Velocidad de rotación.
     */
    private static final double ROTATION_SPEED = 0.03;
    
    /**
     * Tiempo entre disparos (en frames).
     */
    private static final int FIRE_RATE = 90; // Dispara cada 1.5 segundos
    
    /**
     * Contador de impactos recibidos.
     */
    private int hitsReceived;
    
    /**
     * Contador de frames desde el último cambio.
     */
    private int frameCounter;
    
    /**
     * Contador de frames desde el último disparo.
     */
    private int fireCounter;
    
    /**
     * Instancia de Random para cambios aleatorios.
     */
    private Random random;
    
    /**
     * Frame de animación para efecto de parpadeo al recibir daño.
     */
    private int damageFlashCounter;
    
    /**
     * Crea una nueva AlienShip en una posición aleatoria.
     * 
     * @param random Instancia de Random para generar valores aleatorios.
     */
    public AlienShip(Random random) {
        super(calculateRandomPosition(random), 
              calculateRandomVelocity(random), 
              COLLISION_RADIUS, 
              KILL_POINTS);
        this.random = random;
        this.hitsReceived = 0;
        this.frameCounter = 0;
        this.fireCounter = FIRE_RATE; // Dispara al poco tiempo de aparecer
        this.damageFlashCounter = 0;
    }
    
    /**
     * Calcula una posición aleatoria válida para spawneo.
     * 
     * @param random Instancia de Random.
     * @return Vector con la posición calculada.
     */
    private static Vector2 calculateRandomPosition(Random random) {
        // Spawneo en los bordes de la pantalla
        int side = random.nextInt(4); // 0=arriba, 1=derecha, 2=abajo, 3=izquierda
        double x, y;
        
        switch(side) {
            case 0: // Arriba
                x = random.nextDouble() * WorldPanel.WORLD_SIZE;
                y = 0;
                break;
            case 1: // Derecha
                x = WorldPanel.WORLD_SIZE;
                y = random.nextDouble() * WorldPanel.WORLD_SIZE;
                break;
            case 2: // Abajo
                x = random.nextDouble() * WorldPanel.WORLD_SIZE;
                y = WorldPanel.WORLD_SIZE;
                break;
            default: // Izquierda
                x = 0;
                y = random.nextDouble() * WorldPanel.WORLD_SIZE;
                break;
        }
        
        return new Vector2(x, y);
    }
    
    /**
     * Calcula una velocidad aleatoria.
     * 
     * @param random Instancia de Random.
     * @return Vector de velocidad calculado.
     */
    private static Vector2 calculateRandomVelocity(Random random) {
        double angle = random.nextDouble() * Math.PI * 2;
        double speed = MIN_VELOCITY + (random.nextDouble() * VELOCITY_VARIANCE);
        return new Vector2(angle).scale(speed);
    }
    
    /**
     * Cambia la velocidad y posición de manera aleatoria.
     */
    private void changeVelocityAndPosition() {
        // Cambiar velocidad con nuevo ángulo y magnitud
        velocity = calculateRandomVelocity(random);
        
        // Teletransportarse a una nueva posición aleatoria
        double newX = random.nextDouble() * WorldPanel.WORLD_SIZE;
        double newY = random.nextDouble() * WorldPanel.WORLD_SIZE;
        position.set(newX, newY);
        
        // Efecto visual de teletransportación (flash)
        damageFlashCounter = 15;
    }
    
    /**
     * Dispara una bala hacia el jugador.
     * 
     * @param game Instancia del juego.
     */
    private void fireAtPlayer(Game game) {
        // Obtener la posición del jugador
        Vector2 playerPos = game.getPlayer().getPosition();
        
        // Calcular el ángulo hacia el jugador
        double dx = playerPos.x - position.x;
        double dy = playerPos.y - position.y;
        double angleToPlayer = Math.atan2(dy, dx);
        
        // Crear y registrar la bala alienígena (AlienBullet)
        AlienBullet bullet = new AlienBullet(this, angleToPlayer);
        game.registerEntity(bullet);
    }
    
    @Override
    public void update(Game game) {
        super.update(game);
        
        // Rotar la nave
        rotate(ROTATION_SPEED);
        
        // Incrementar contador de frames
        frameCounter++;
        
        // Cambiar velocidad y posición cada 5 segundos
        if(frameCounter >= CHANGE_INTERVAL) {
            changeVelocityAndPosition();
            frameCounter = 0;
        }
        
        // Decrementar contador de flash de daño
        if(damageFlashCounter > 0) {
            damageFlashCounter--;
        }
        
        // Sistema de disparo
        fireCounter++;
        if(fireCounter >= FIRE_RATE) {
            fireAtPlayer(game);
            fireCounter = 0;
        }
    }
    
    @Override
    public void handleCollision(Game game, Entity other) {
        // Recibir daño SOLO de balas del jugador (Bullet), NO de balas alienígenas (AlienBullet)
        if(other.getClass() == Bullet.class) {
            hitsReceived++;
            damageFlashCounter = 10; // Flash al recibir daño
            
            // Si ha recibido 3 impactos, destruir la nave
            if(hitsReceived >= MAX_HITS) {
                flagForRemoval();
                game.addScore(getKillScore());
            }
        }
        
        // Matar al jugador si choca con la nave alienígena
        if(other.getClass() == Player.class) {
            game.killPlayer();
        }
    }
    
    @Override
    public void draw(Graphics2D g, Game game) {
        // Cambiar color basado en los impactos recibidos y el flash
        if(damageFlashCounter > 0 && damageFlashCounter % 4 < 2) {
            g.setColor(Color.WHITE); // Flash blanco al recibir daño
        } else {
            switch(hitsReceived) {
                case 0:
                    g.setColor(Color.GREEN); // Sin daño - verde
                    break;
                case 1:
                    g.setColor(Color.YELLOW); // 1 impacto - amarillo
                    break;
                case 2:
                    g.setColor(Color.RED); // 2 impactos - rojo
                    break;
                default:
                    g.setColor(Color.WHITE);
                    break;
            }
        }
        
        // Dibujar la nave alienígena (forma de platillo volador)
        // Cuerpo principal
        g.drawOval(-15, -5, 30, 10);
        
        // Cúpula superior
        g.drawArc(-10, -10, 20, 10, 0, 180);
        
        // Base inferior
        g.drawArc(-8, 0, 16, 8, 180, 180);
        
        // Líneas de detalle
        g.drawLine(-12, 0, -15, 3);
        g.drawLine(12, 0, 15, 3);
        
        // Indicador de vida (barras)
        g.setColor(Color.WHITE);
        for(int i = 0; i < MAX_HITS - hitsReceived; i++) {
            g.fillRect(-10 + (i * 8), -18, 6, 3);
        }
    }
}

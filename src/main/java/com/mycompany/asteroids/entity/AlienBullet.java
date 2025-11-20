package com.mycompany.asteroids.entity;

import com.mycompany.asteroids.Game;
import com.mycompany.asteroids.util.Vector2;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Representa una bala disparada por una nave alienígena.
 * @author Fátima Valeria Bocanegra Costilla
 *
 */
public class AlienBullet extends Entity {
	
	/**
	 * The magnitude of the velocity of a Bullet.
	 */
	private static final double VELOCITY_MAGNITUDE = 5.0; // Más lenta que las del jugador
	
	private static final double ANGULO_ROTACION = 2 * Math.PI / 60;
	
	/**
	 * The maximum number of cycles that a Bullet can exist.
	 */
	private static final int MAX_LIFESPAN = 80; // Duran un poco más
	
	/**
	 * The number of cycles this Bullet has existed.
	 */
	private int lifespan;

	/**
	 * Creates a new AlienBullet instance.
	 * @param owner The object that fired the bullet.
	 * @param angle The direction of the Bullet.
	 */
	public AlienBullet(Entity owner, double angle) {
		super(new Vector2(owner.position), new Vector2(angle).scale(VELOCITY_MAGNITUDE), 3.0, 0);
		this.lifespan = MAX_LIFESPAN;
	}
	
	@Override
	public void update(Game game) {
		super.update(game);
		
		rotate(ANGULO_ROTACION);
		//Decrement the lifespan of the bullet, and remove it if needed.
		this.lifespan--;
		if(lifespan <= 0) {
			flagForRemoval();
		}
	}

	@Override
	public void handleCollision(Game game, Entity other) {
		// Solo colisiona con el jugador o asteroides, NO con otras naves alienígenas
		if(other.getClass() == Player.class) {
			flagForRemoval();
			// El Player ya maneja su propia muerte en su handleCollision
		} else if(other.getClass() == Asteroid.class) {
			flagForRemoval();
			// Los asteroides manejan su propia destrucción
		}
	}
	
	@Override
	public void draw(Graphics2D g, Game game) {
		// Balas del alien son rojas y un poco más grandes
		g.setColor(Color.RED);
		g.fillOval(-2, -2, 4, 4);
	}

}

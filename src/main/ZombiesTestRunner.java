package main;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.scene.shape.Line;
import persons.Zombie;
import utility_classes.Windows;
import game_actions.Game;
import game_actions.Runner;
import guns.Gun;

public class ZombiesTestRunner extends Game {
	
	public int playerX = 400;
	public int playerY = 220;
	public int playerWidth = 30;
	public Rectangle player = new Rectangle(400 - 15, 220 - 15, playerWidth, playerWidth);
	public boolean touchingZombie = false;
	
	public int[] screenX = { 0, 800, 800, 0 };
	public int[] screenY = { 0, 0, 480, 480 };
	public Polygon screen = new Polygon(screenX, screenY, 4);
	public int ticks = 75;
	public int maxTicks = 150;
	public int scoreCount = 0;
	public int spawnCount = 0;
	public int score = 0;
	public int roundCount = 1;
	public int points = 0;
	
	public int cursorX;
	public int cursorY;
	
	public boolean rPressed = false;
	public boolean wPressed = false;
	public long lastShot;
	public boolean shooting = false;
	public HashMap<String, Gun> guns = new HashMap<String, Gun>();
	public Gun currentGun = guns.get("Pistol");
	public int firstLoad = 0;
	public long reloadStart;
	public boolean reloading = false;
	public long lastReload;
	public boolean autoReady = true;
	
	public int random;
	public double randomSpeed;
	public ArrayList<Zombie> zombies = new ArrayList<Zombie>();
	public int zombieLives = 100;
	
	public int playerHealth = 200;
	public int maxPlayerHealth = 200;
	
	public boolean supplyDrop = false;
	public int supplyDropX;
	public int supplyDropY;
	public int insideDrop;
	public int dropMessageStartPoints = -2;
	public int dropMessageDurationPoints = 2;
	public String dropMessage;
	
	@Override
	public void moves() {
		// TODO Auto-generated method stub
		
		playerMove();
		zombieMove();
		
		
		reloading();
		
		supplyDrop();
		
		horrorOfShooting();
		
		spawnZombie();
		
		scoringStuff();
		cursorX = MouseInfo.getPointerInfo().getLocation().x - 8;
		cursorY = MouseInfo.getPointerInfo().getLocation().y - 31;
		
	}

	public void playerMove() {
		
		if (upPressed) {
			deltaY = -movementVar;
		} else if (downPressed) {
			deltaY = movementVar;
		} else {
			deltaY = 0;
		}
		
		player.y += deltaY;
		
		if (!screen.contains(player)) {
			player.y -= deltaY;
		}
		for (Zombie z : zombies) {
			if (z.touchingPlayer(player)) {
				player.y -= deltaY;
			}
		}
		
		if (rightPressed) {
			deltaX = movementVar;
		} else if (leftPressed) {
			deltaX = -movementVar;
		} else {
			deltaX = 0;
		}
		
		player.x += deltaX;
		
		if (!screen.contains(player)) {
			player.x -= deltaX;
		}
	}

	public void zombieMove() {
		
		for (Zombie z : zombies) {
			if (z.touchingPlayer(player)) {
				player.x -= deltaX;
			}
		}
		
		for (Zombie z : zombies) {
			if (z.move(player, zombies, playerHealth))
				playerHealth -= 2;
		}
	}

	public void reloading() {
		
		ticks++;
		
		if (firstLoad == 1) {
			currentGun.reload();
			firstLoad++;
			reloading = false;
		} else if (firstLoad > 1) {
			if (rPressed && currentGun.magCurrent < currentGun.magSize && currentGun.bullets > 0) {
				rPressed = false;
				reloadStart = System.currentTimeMillis();
				reloading = true;
			} else if (currentGun.magCurrent == 0 && currentGun.bullets > 0 && !reloading) {
				reloadStart = System.currentTimeMillis();
				reloading = true;
			}
			if (System.currentTimeMillis() - reloadStart > currentGun.reloadTime && reloading) {
				currentGun.reload();
				reloading = false;
			}
		}
	}

	public void supplyDrop() {
		
		if (supplyDrop && player.intersects(supplyDropX, supplyDropY, 30, 30)) {
				currentGun = guns[insideDrop - 1];
				currentGun.bullets = currentGun.maxBullets;
				dropMessage = currentGun.name;
				rPressed = (insideDrop != 0);
				
				supplyDrop = false;
				score = 0;
				dropMessageStartPoints = points;
		}
	}

	public void horrorOfShooting() {
		
		if (shooting) {
			
			if (!currentGun.isAuto || autoReady) {
				autoReady = false;
				for (Zombie z : zombies) {
					z.shot(z, currentGun.shot, "Normal");
					if (currentGun.name == "Shotgun") {
						z.shot(z, currentGun.spreadOneLine, "Spread One");
						z.shot(z, currentGun.spreadTwoLine, "Spread Two");
					}
				}
				double[] minDistance = { 2000, -1 }; // Minimum distance from
														// zombie to player and
														// index of zombie
				int i = 0;
				if (currentGun.name == "Sniper") {
					for (Zombie z : zombies) {
						if (z.isShot) {
							score++;
							if ((z.lives -= currentGun.damage) < 0) {
								zombies.remove(i);
								score++;
							} else
								zombies.get(i).setColor();
						}
						i++;
					}
				} else {
					i = 0;
					
					for (Zombie z : zombies) {
						if (z.isShot) {
							double temp = z.getDistanceToPlayer(player);
							if (temp < minDistance[0]) {
								minDistance[0] = temp;
								minDistance[1] = i;
							}
							z.isShot = false;
						}
						i++;
					}
					if (minDistance[0] != 2000) {
						// zombies.get((int)minDistance[1]).isShot = true;
						score++;
						if ((zombies.get((int) minDistance[1]).lives -= currentGun.damage) < 0) {
							zombies.remove((int) minDistance[1]);
							score++;
						} else
							zombies.get((int) minDistance[1]).setColor();
					}
					
					if (currentGun.name == "Shotgun") {
						double[] minDistance1 = { 2000, -1 }; // Minimum
																// distance from
																// zombie to
																// player and
																// index of
																// zombie
						int j = 0;
						for (Zombie z : zombies) {
							if (z.spreadOneHit) {
								double temp = z.getDistanceToPlayer(player);
								if (temp < minDistance1[0]) {
									minDistance1[0] = temp;
									minDistance1[1] = j;
								}
								z.spreadOneHit = false;
							}
							j++;
						}
						if (minDistance1[0] != 2000) {
							// zombies.get((int)minDistance[1]).isShot = true;
							score++;
							if ((zombies.get((int) minDistance1[1]).lives -= currentGun.damage) < 0) {
								zombies.remove((int) minDistance1[1]);
								score++;
							} else
								zombies.get((int) minDistance1[1]).setColor();
						}
						double[] minDistance2 = { 2000, -1 }; // Minimum
																// distance from
																// zombie to
																// player and
																// index of
																// zombie
						int k = 0;
						for (Zombie z : zombies) {
							if (z.spreadOneHit) {
								double temp = z.getDistanceToPlayer(player);
								if (temp < minDistance2[0]) {
									minDistance2[0] = temp;
									minDistance2[1] = k;
								}
								z.spreadTwoHit = false;
							}
							k++;
						}
						if (minDistance2[0] != 2000) {
							// zombies.get((int)minDistance[1]).isShot = true;
							score++;
							if ((zombies.get((int) minDistance2[1]).lives -= currentGun.damage) < 0) {
								zombies.remove((int) minDistance2[1]);
								score++;
							} else
								zombies.get((int) minDistance2[1]).setColor();
						}
					}
				}
				
			}
			if (currentGun.name == "AK-47") {
				if (currentGun.magCurrent > 0 && System.currentTimeMillis() - lastShot > currentGun.shotTime
						&& !reloading) {
					shooting = true;
					currentGun.shoot(player, cursorX, cursorY);
					lastShot = System.currentTimeMillis();
					autoReady = true;
				}
			} else {
				shooting = false;
			}
			
		}
		
		if (currentGun.isAuto == true) {
			
			if (currentGun.magCurrent > 0 && System.currentTimeMillis() - lastShot > currentGun.shotTime
					&& !reloading) {
				autoReady = true;
			}
			
		}
	}

	public void scoringStuff() {
		
		scoreCount++;
		if (scoreCount % 25 == 0) {
			if (playerHealth < maxPlayerHealth)
				playerHealth++;
		}
		if (scoreCount == 150) {
			scoreCount = 0;
			score++;
			points++;
		}
		if (score >= 100) {
			score = 0;
			roundCount += 1;
			if (!supplyDrop) {
				supplyDrop = true;
				supplyDropX = (int) (Math.random() * 650) + 20;
				supplyDropY = (int) (Math.random() * 350) + 70;
				while (supplyDropX > player.x - 40 && supplyDropX < player.x + 40) {
					supplyDropX = (int) (Math.random() * 760) + 20;
				}
				while (supplyDropY > player.y - 40 && supplyDropY < player.y + 40) {
					supplyDropY = (int) (Math.random() * 400) + 20;
				}
				insideDrop = (int) (Math.random() * 6);
			}
		}
	}

	public void spawnZombie() {
		
		if (ticks >= maxTicks) {
			firstLoad++;
			ticks = 0;
			
			random = (int) (Math.random() * 2);
			randomSpeed = (3 + (int) (Math.random() * 3)) * 0.25;
			if (random == 1)
				zombies.add(new Zombie((int) (Math.random() * 700) + 20, -20, 20, randomSpeed, zombieLives));
			else
				zombies.add(new Zombie((int) (Math.random() * 700) + 20, 500, 20, randomSpeed, zombieLives));
			spawnCount++;
			if (maxTicks > 50 && spawnCount % 2 == 0) {
				maxTicks--;
			}
			if (spawnCount == 100) {
				zombieLives += 100;
			} else if (spawnCount == 50) {
				zombieLives += 100;
			}
		}
	}
	
	@Override
	public boolean checkIfDead() {
		// TODO Auto-generated method stub
		
		return playerHealth <= 0;
	}
	
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void draw(Graphics2D g) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void drawPlaying(Graphics2D g) {
		
		// TODO Auto-generated method stub
		
		supplyDropDraw(g);
		
		drawPlayer(g);
		
		drawZombies(g);
		
		drawAmmo(g);
		
		drawHealth(g);
		
		
		drawAimDot(g);
		
		
	}

	public void drawZombies(Graphics2D g) {
		
		for (Zombie z : zombies) {
			g.setColor(z.color);
			g.fill(z);
		}
	}

	public void drawPlayer(Graphics2D g) {
		
		g.setColor(Color.RED);
		g.fillOval(player.x, player.y, playerWidth, playerWidth);
	}

	public void supplyDropDraw(Graphics2D g) {
		
		if (supplyDrop) {
			g.setColor(Color.YELLOW);
			g.fillRect(supplyDropX, supplyDropY, 30, 30);
			
		}
		
		if (points - dropMessageStartPoints < dropMessageDurationPoints) {
			g.setColor(Color.ORANGE);
			g.setFont(new Font("Century Gothic", Font.PLAIN, 36));
			g.drawString(dropMessage, supplyDropX, supplyDropY);
		}
	}

	public void drawAmmo(Graphics2D g) {
		
		g.setFont(new Font("Century Gothic", Font.PLAIN, 64));
		g.setColor(Color.orange);
		if (reloading)
			g.drawString("-", 720, 460);
		else
			g.drawString(String.valueOf(currentGun.magCurrent), 720, 460);
		
		g.setFont(new Font("Century Gothic", Font.PLAIN, 28));
		g.drawString(String.valueOf(currentGun.bullets), 760, 460);
		g.drawString("Score: " + String.valueOf(points), 20, 460);
		
		g.setFont(new Font("Century Gothic", Font.PLAIN, 12));
		g.drawString(currentGun.name, 720, 475);
	}

	public void drawHealth(Graphics2D g) {
		
		g.setColor(Color.RED);
		g.fillRect(400 - maxPlayerHealth / 2, 20, 200, 20);
		g.setColor(Color.GREEN);
		g.fillRect(400 - maxPlayerHealth / 2, 20, playerHealth, 20);
	}

	public void drawAimDot(Graphics2D g) {
		
		g.setColor(Color.CYAN);
		int centerX = player.x + 15;
		int centerY = player.y + 15;
		int sideX = cursorX - centerX;
		int sideY = cursorY - centerY;
		double distance = Math.sqrt(sideX * sideX + sideY * sideY);
		int a = centerX + (int) ((sideX * Math.sqrt(900)) / distance);
		int b = centerY + (int) ((sideY * Math.sqrt(900)) / distance);
		g.fillOval(a, b, 4, 4);
	}
	
	@Override
	public void setup() {
		// TODO Auto-generated method stub
		
		upKey = KeyEvent.VK_R;
		downKey = KeyEvent.VK_S;
		leftKey = KeyEvent.VK_A;
		rightKey = KeyEvent.VK_D;
		
		movementVar = 3;
		reloading = true;
		lastShot = System.currentTimeMillis();
		shooting = false;
		Windows.setSCORE_SIZE(30);
	}
	
	@Override
	public String getGameName() {
		
		// TODO Auto-generated method stub
		return "Zombies";
	}
	
	@Override
	public void pressed(MouseEvent m) {
		
		if (m.getButton() == MouseEvent.BUTTON1) {
			if (System.currentTimeMillis() - lastShot > currentGun.shotTime && !reloading
					&& currentGun.magCurrent > 0) {
				shooting = true;
				currentGun.shoot(player, cursorX, cursorY);
				lastShot = System.currentTimeMillis();
				autoReady = true;
			}
		}
	}
	
	@Override
	public void released(MouseEvent m) {
		
		if (m.getButton() == MouseEvent.BUTTON1) {
			shooting = false;
		}
	}
	
	@Override
	public void customPressed(KeyEvent e) {
		
		if (e.getKeyCode() == KeyEvent.VK_R) {
			rPressed = true;
		}
	}
	
	@Override
	public void customReleased(KeyEvent e) {
		
		if (e.getKeyCode() == KeyEvent.VK_R) {
			rPressed = false;
		}
	}
	
	public static void main(String[] args) {
		
		// TODO Auto-generated method stub
		new Runner(new ZombiesTestRunner());
	}
}
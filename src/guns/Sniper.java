package guns;

import java.util.ArrayList;

import persons.Player;
import persons.Zombie;

public class Sniper extends Gun {
	
	public Sniper(ArrayList<Zombie> zombies, Player player) {
		super("Sniper", 10, 1, 1500, 0, 400);
		setPlayer(player);
		setZombies(zombies);
	}
	
	@Override
	public ArrayList<Zombie> loopThroughZombies() {
		
		ArrayList<Zombie> shotZombies = new ArrayList<Zombie>();
		for (Zombie z : zombies) {
			if (applyShot(z)) {
				shotZombies.add(z);
			}
		}
		return shotZombies;

	}

	@Override
	public boolean applyShot(Zombie zombie) {
		
		if (!zombie.ifShot(shot)) return false;
		
		score++;
		zombie.health -= zombie.health;
		return true;
	}

	@Override
	public void modifyShot() {
		
		// TODO Auto-generated method stub
		
	}
}

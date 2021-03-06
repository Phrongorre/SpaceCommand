package ph.games.scg._depreciated_.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector3;

import ph.games.scg._depreciated_.component.CharacterComponent;
import ph.games.scg._depreciated_.component.EnemyComponent;
import ph.games.scg._depreciated_.component.ModelComponent;
import ph.games.scg._depreciated_.component.NetEntityComponent;
import ph.games.scg._depreciated_.game.GameCore;
import ph.games.scg._depreciated_.server.ServerCore;

public class EnemySystem extends EntitySystem implements EntityListener {
	
	private ImmutableArray<Entity> entities;
	
	private static boolean spawnFlag = true;
	private static int ENEMY_COUNTER = 0;

	@Override
	public void addedToEngine(Engine engine) {
		this.entities = engine.getEntitiesFor(Family.all(EnemyComponent.class, CharacterComponent.class).get());
	}

	@Override
	public void update(float dt) {
		if (this.entities.size() < 1) {
			//Make sure to only spawn one and wait for spawn to come in
			if (spawnFlag) {
				//Request new enemy spawn
				ServerCore.server.spawnEnemy("enemy_" + ENEMY_COUNTER++);
				spawnFlag = false;
			}
			return;
		}

		spawnFlag = true;
		EnemyComponent ecomp=null;
		for (Entity e : this.entities) {
			ecomp = e.getComponent(EnemyComponent.class);
			if (ecomp.target == null) continue;
			
			ModelComponent mcomp = e.getComponent(ModelComponent.class);
			ModelComponent tgtmcomp = ecomp.target.getComponent(ModelComponent.class);
			NetEntityComponent necomp = e.getComponent(NetEntityComponent.class);
			Vector3 targetPosition = new Vector3();
			Vector3 enemyPosition = new Vector3();
			Vector3 distance = new Vector3();
			tgtmcomp.instance.transform.getTranslation(targetPosition);
			mcomp.instance.transform.getTranslation(enemyPosition);
			distance.set(targetPosition).sub(enemyPosition);
			float dX = targetPosition.x - enemyPosition.x;
			float dZ = targetPosition.z - enemyPosition.z;
			float theta = (float)Math.toDegrees(Math.atan2(dX, dZ));
			
			if (theta < 0f) theta += 360f;
			else if (theta >= 360f) theta -= 360f;
			
			Vector3 movement = new Vector3(0f, 0f, 1f);
			movement.rotate(Vector3.Y, theta).scl(3f * dt);
			
			ServerCore.server.move(necomp.netEntity.getName(), movement, theta, dt);
			
			//TODO: Works, but happens too fast. Rapidly reduces NetEntity health to zero, and is currently not entirely connected to Player, so once zero health is reached, player control freezes
//			if (distance.len() <= ecomp.reach) GameCore.client.attack(necomp.netEntity.getName(), ecomp.target.getComponent(NetEntityComponent.class).netEntity.getName());
		}
	}

	@Override
	public void entityAdded(Entity entity) { }

	@Override
	public void entityRemoved(Entity entity) { }

}
package ph.games.scg._depreciated_.util;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShapeZ;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.badlogic.gdx.physics.bullet.dynamics.btKinematicCharacterController;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.JsonReader;

import ph.games.scg._depreciated_.bullet.MotionState;
import ph.games.scg._depreciated_.component.AnimationComponent;
import ph.games.scg._depreciated_.component.BulletComponent;
import ph.games.scg._depreciated_.component.CharacterComponent;
import ph.games.scg._depreciated_.component.EnemyComponent;
import ph.games.scg._depreciated_.component.GunComponent;
import ph.games.scg._depreciated_.component.ModelComponent;
import ph.games.scg._depreciated_.component.NetEntityComponent;
import ph.games.scg._depreciated_.component.PlayerComponent;
import ph.games.scg._depreciated_.component.StatusComponent;
import ph.games.scg._depreciated_.environment.Room.Quad;
import ph.games.scg._depreciated_.game.GameCore;
import ph.games.scg._depreciated_.server.Enemy;
import ph.games.scg._depreciated_.server.User;
import ph.games.scg._depreciated_.system.BulletSystem;

public class EntityFactory {

	private static Model playerModel;
	//	private static Model spaceshipModel;
	private static Model enemyModel;
	private static final float BLENDER_MODEL_SCALE = 0.01f;

	//Static "constructor"
	static {
		ModelBuilder modelBuilder = new ModelBuilder();
		Texture playerTexture = new Texture(Gdx.files.internal("badlogic.jpg"));
		playerModel = modelBuilder.createCapsule(
				2f, //Radius
				6f, //Height
				16, //Divisions
				new Material(
						TextureAttribute.createDiffuse(playerTexture),
						ColorAttribute.createSpecular(1f, 1f, 1f, 1f),
						FloatAttribute.createShininess(8f)
						),
				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates
				);
//		spaceshipModel = null;
		enemyModel = null;
	}

	public static Entity createQuad(Quad quad) {
		ModelBuilder modelBuilder = new ModelBuilder();
		Texture quadTexture = new Texture(Gdx.files.internal("./rooms/" + quad.getTexture()));
		
		Vector3 bl = quad.getBottomLeft();
		Vector3 br = quad.getBottomRight();
		Vector3 tr = quad.getTopRight();
		Vector3 tl = quad.getTopLeft();
		Vector3 nml = quad.getNormal();
		Vector3 ctr = quad.getCenter();
		
		Model quadModel = modelBuilder.createRect(
				
				bl.x, bl.y, bl.z, //texture bottom left
				br.x, br.y, br.z, //texture bottom right
				tr.x, tr.y, tr.z, //texture top right
				tl.x, tl.y, tl.z, //texture top left

				nml.x, nml.y, nml.z, //normal vector

				new Material(
						TextureAttribute.createDiffuse(quadTexture),
						ColorAttribute.createSpecular(1f, 1f, 1f, 1f),
						FloatAttribute.createShininess(8f)
						),

				VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates

				);
		
		final BoundingBox boundingBox = new BoundingBox();
		quadModel.calculateBoundingBox(boundingBox);
		Vector3 tmpV = new Vector3();
		btCollisionShape col = new btBoxShape(
				tmpV.set(
						boundingBox.getWidth()  * 0.5f,
						boundingBox.getHeight() * 0.5f,
						boundingBox.getDepth()  * 0.5f
						)
				);

		Entity entity = new Entity();

		ModelComponent modelComponent = new ModelComponent(quadModel, 0f, 0f, 0f);
		entity.add(modelComponent);

		BulletComponent bulletComponent = new BulletComponent();
		bulletComponent.bodyInfo = new btRigidBody.btRigidBodyConstructionInfo(
				0,           //float mass
				null,        //btMotionState motionState
				col,         //btCollisionShape collisionShape
				Vector3.Zero //Vector3 localInertia
				);
		bulletComponent.body = new btRigidBody(bulletComponent.bodyInfo);
		bulletComponent.body.userData = entity;
		Matrix4 transform = new Matrix4();
		transform.translate(ctr.x, ctr.y, ctr.z);
		bulletComponent.motionState = new MotionState(transform);		
		((btRigidBody)bulletComponent.body).setMotionState(bulletComponent.motionState);
		entity.add(bulletComponent);

		return entity;
	}

	public static Entity createStaticEntity(Model model, float x, float y, float z) {
		final BoundingBox boundingBox = new BoundingBox();
		model.calculateBoundingBox(boundingBox);
		Vector3 tmpV = new Vector3();
		btCollisionShape col = new btBoxShape(
				tmpV.set(
						boundingBox.getWidth()  * 0.5f,
						boundingBox.getHeight() * 0.5f,
						boundingBox.getDepth()  * 0.5f
						)
				);

		Entity entity = new Entity();

		ModelComponent modelComponent = new ModelComponent(model, x, y, z);
		entity.add(modelComponent);

		BulletComponent bulletComponent = new BulletComponent();
		bulletComponent.bodyInfo = new btRigidBody.btRigidBodyConstructionInfo(
				0,           //float mass
				null,        //btMotionState motionState
				col,         //btCollisionShape collisionShape
				Vector3.Zero //Vector3 localInertia
				);
		bulletComponent.body = new btRigidBody(bulletComponent.bodyInfo);
		bulletComponent.body.userData = entity;
		bulletComponent.motionState = new MotionState(modelComponent.instance.transform);
		((btRigidBody)bulletComponent.body).setMotionState(bulletComponent.motionState);
		entity.add(bulletComponent);

		return entity;
	}

	public static Entity loadScene(float x, float y, float z) {
		Entity entity = new Entity();
		ModelLoader<?> modelLoader = new G3dModelLoader(new JsonReader());
		ModelData modelData = modelLoader.loadModelData(Gdx.files.internal("blender/arena.g3dj"));
		Model model = new Model(modelData, new TextureProvider.FileTextureProvider());
		ModelComponent modelComponent = new ModelComponent(model, x, y, z);
		entity.add(modelComponent);
		BulletComponent bulletComponent = new BulletComponent();
		btCollisionShape shape = Bullet.obtainStaticNodeShape(model.nodes);
		bulletComponent.bodyInfo = new btRigidBody.btRigidBodyConstructionInfo(0, null, shape, Vector3.Zero);
		bulletComponent.body = new btRigidBody(bulletComponent.bodyInfo);
		bulletComponent.body.userData = entity;
		bulletComponent.motionState = new MotionState(modelComponent.instance.transform);
		((btRigidBody)bulletComponent.body).setMotionState(bulletComponent.motionState);
		entity.add(bulletComponent);
		return entity;
	}

	public static Entity createPlayer(BulletSystem bulletSystem, float x, float y, float z) {
		Entity entity = createCharacter(bulletSystem, x, y, z);
		entity.add(new PlayerComponent());
		
		NetEntityComponent necomp = new NetEntityComponent();
		necomp.netEntity = GameCore.client.getUser();
		
		entity.add(necomp);
		
		return entity;
	}

	//	public static Entity createSpaceship(String g3djFilename, BulletSystem bulletSystem, float x, float y, float z) {
	//	      Entity entity = new Entity();
	//	      
	//	      if (spaceshipModel == null) {
	//	    	  ModelLoader<?> modelLoader = new G3dModelLoader(new JsonReader());
	//		      ModelData modelData = modelLoader.loadModelData(Gdx.files.internal("blender/" + g3djFilename));
	//	         spaceshipModel = new Model(modelData, new TextureProvider.FileTextureProvider());
	//	         for (Node node : spaceshipModel.nodes) node.scale.scl(BLENDER_MODEL_SCALE);
	//	         spaceshipModel.calculateTransforms();
	//	      }
	//	      ModelComponent modelComponent = new ModelComponent(spaceshipModel, x, y, z);
	//	      entity.add(modelComponent);
	//	      
	//	      CharacterComponent characterComponent = createCharacterComponent(modelComponent, entity);
	//	      entity.add(characterComponent);
	//	      
	//	      bulletSystem.collisionWorld.addCollisionObject(
	//	         characterComponent.ghostObject,
	//	         (short)btBroadphaseProxy.CollisionFilterGroups.CharacterFilter,
	//	         (short)btBroadphaseProxy.CollisionFilterGroups.AllFilter
	//	      );
	//	      bulletSystem.collisionWorld.addAction(characterComponent.characterController);
	//	      
	//	      return entity;
	//	}

	public static Entity loadGun(float x, float y, float z) {
		ModelLoader<?> modelLoader = new G3dModelLoader(new JsonReader()); //Research this...
		ModelData modelData = modelLoader.loadModelData(Gdx.files.internal("blender/spacegladgun.g3dj"));

		/*/
	      System.out.println("ModelData.id:                   " + modelData.id);
	      ModelMaterial modmat = modelData.materials.first();
	      System.out.println("ModelData.material[0].id:       " + modmat.id);
	      System.out.println("ModelData.material[0].opacity:  " + modmat.opacity);
	      System.out.println("ModelData.material[0].emissive: " + modmat.emissive);
	      /**/

		Model model = new Model(modelData, new TextureProvider.FileTextureProvider()); //And this...
		ModelComponent modelComponent = new ModelComponent(model, x, y, z);
		modelComponent.instance.transform.rotate(0f, 1f, 0f, 180f); //LEARN ABOUT QUATERNIONS!!!!
		Entity gunEntity = new Entity();
		gunEntity.add(modelComponent);
		gunEntity.add(new GunComponent());
		gunEntity.add(new AnimationComponent(modelComponent.instance));
		return gunEntity;
	}

	public static Entity createEnemy(BulletSystem bulletSystem, String name, float x, float y, float z) {
		Entity entity = new Entity();

		if (enemyModel == null) {
			ModelLoader<?> modelLoader = new G3dModelLoader(new JsonReader());
			ModelData modelData = modelLoader.loadModelData(Gdx.files.internal("blender/blunky.g3dj"));
			enemyModel = new Model(modelData, new TextureProvider.FileTextureProvider());
			for (Node node : enemyModel.nodes) node.scale.scl(BLENDER_MODEL_SCALE);
			enemyModel.calculateTransforms();
		}
		ModelComponent modelComponent = new ModelComponent(enemyModel, x, y, z);
		entity.add(modelComponent);

		CharacterComponent characterComponent = createCharacterComponent(modelComponent, entity);
		entity.add(characterComponent);

		bulletSystem.collisionWorld.addCollisionObject(
				characterComponent.ghostObject,
				(short)btBroadphaseProxy.CollisionFilterGroups.CharacterFilter,
				(short)btBroadphaseProxy.CollisionFilterGroups.AllFilter
				);
		bulletSystem.collisionWorld.addAction(characterComponent.characterController);

		entity.add(new EnemyComponent(EnemyComponent.STATE.RUNNING));

		AnimationComponent animationComponent = new AnimationComponent(modelComponent.instance);
		animationComponent.animate(EnemyAnimations.BasicRunID, EnemyAnimations.offsetBasicRun, EnemyAnimations.durationBasicRun, -1, 1f);
		entity.add(animationComponent);

		entity.add(new StatusComponent(animationComponent));
		
		NetEntityComponent necomp = new NetEntityComponent();
		necomp.netEntity = new Enemy(name);
		entity.add(necomp);

		return entity;
	}

	private static Entity createCharacter(BulletSystem bulletSystem, float x, float y, float z) {
		Entity entity = new Entity();

		ModelComponent modelComponent = new ModelComponent(playerModel, x, y, z);
		entity.add(modelComponent);

		CharacterComponent characterComponent = createCharacterComponent(modelComponent, entity);
		entity.add(characterComponent);

		bulletSystem.collisionWorld.addCollisionObject(
				characterComponent.ghostObject,
				(short)(btBroadphaseProxy.CollisionFilterGroups.CharacterFilter),
				(short)(btBroadphaseProxy.CollisionFilterGroups.AllFilter)
				);
		bulletSystem.collisionWorld.addAction(characterComponent.characterController);

		return entity;
	}

	private static CharacterComponent createCharacterComponent(ModelComponent mcomp, Entity entity) {
		CharacterComponent characterComponent = new CharacterComponent();
		characterComponent.ghostObject = new btPairCachingGhostObject();
		characterComponent.ghostObject.setWorldTransform(mcomp.instance.transform);
		characterComponent.ghostShape = new btCapsuleShapeZ(2f, 2f); //Creating a capsule with upAxis set to Z, makes for an accurate upAxis in btKinematicCharacterController creation
		characterComponent.ghostObject.setCollisionShape(characterComponent.ghostShape);
		characterComponent.ghostObject.setCollisionFlags(btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT);
		characterComponent.characterController = new btKinematicCharacterController(
				characterComponent.ghostObject,
				characterComponent.ghostShape,
				0.35f,
				Vector3.Y //Changes btKinematicCharacterController up axis to Y-axis, default is X-axis. This affects the gravity pull on the character controller.
				);
		characterComponent.ghostObject.userData = entity;
		return characterComponent;
	}

//	public static Entity createUserEntity(BulletSystem bulletSystem, String name, float x, float y, float z) {
//		Entity entity = EntityFactory.createCharacter(bulletSystem, x, y, z);
//		entity.add(new UserComponent());
//		return entity;
//	}

	public static Entity createUserEntity(BulletSystem bulletSystem, String name, float x, float y, float z) {
		Entity entity = EntityFactory.createCharacter(bulletSystem, x, y, z);
		NetEntityComponent necomp = new NetEntityComponent();
		necomp.netEntity = new User(name);
		entity.add(necomp);
		return entity;
	}

}

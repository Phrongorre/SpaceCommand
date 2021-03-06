package ph.games.scg._depreciated_.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;

public class AnimationComponent implements Component {
   
   private AnimationController animationController;
   
   public AnimationComponent(ModelInstance instance) {
      this.animationController = new AnimationController(instance);
      this.animationController.allowSameAnimation = true;
   }
   
   public void animate(final String id, final int loops, final float speed) {
      this.animationController.animate(id, loops, speed, null, 0f);
   }
   
   public void animate(String id, float offset, float duration, int loops, float speed) {
      this.animationController.animate(id, offset, duration, loops, speed, null, 0f);
   }
   
   public void update(float dt) {
      this.animationController.update(dt);
   }
   
}
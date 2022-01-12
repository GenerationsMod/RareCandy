package cf.hydos.renderer.main;

import cf.hydos.renderer.animatedModel.AnimatedModel;
import cf.hydos.renderer.animation.Animation;
import cf.hydos.renderer.loaders.AnimatedModelLoader;
import cf.hydos.renderer.loaders.AnimationLoader;
import cf.hydos.renderer.scene.ICamera;
import cf.hydos.renderer.scene.Scene;
import cf.hydos.renderer.utils.MyFile;

public class SceneLoader {

	/**
	 * Sets up the com.thinmatrix.animationrenderer.scene. Loads the entity, load the com.thinmatrix.animationrenderer.animation, tells the entity
	 * to do the com.thinmatrix.animationrenderer.animation, sets the light direction, creates the camera, etc...
	 *
	 * @return The entire com.thinmatrix.animationrenderer.scene.
	 */
	public static Scene loadScene() {
		ICamera camera = new Camera();
		AnimatedModel entity = AnimatedModelLoader.loadEntity(new MyFile(GeneralSettings.MODEL_FILE),
				new MyFile(GeneralSettings.DIFFUSE_FILE));
		Animation animation = AnimationLoader.loadAnimation(new MyFile(GeneralSettings.ANIM_FILE));
		entity.doAnimation(animation);
		Scene scene = new Scene(entity, camera);
		scene.setLightDirection(GeneralSettings.LIGHT_DIR);
		return scene;
	}

}

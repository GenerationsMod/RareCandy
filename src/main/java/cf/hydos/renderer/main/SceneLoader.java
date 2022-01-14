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
     * Sets up the animationrenderer.scene. Loads the entity, load the animationrenderer.animation, tells the entity
     * to do the animationrenderer.animation, sets the light direction, creates the camera, etc...
     *
     * @return The entire animationrenderer.scene.
     */
    public static Scene loadScene() {
        ICamera camera = new Camera();
        Animation animation = AnimationLoader.loadAnimation(new MyFile(GeneralSettings.ANIM_FILE));

        AnimatedModel entity = AnimatedModelLoader.loadEntity(new MyFile(GeneralSettings.MODEL_FILE), new MyFile(GeneralSettings.DIFFUSE_FILE));
        AnimatedModel entity2 = AnimatedModelLoader.loadEntity(new MyFile(GeneralSettings.MODEL_FILE), new MyFile(GeneralSettings.DIFFUSE_FILE));
        AnimatedModel entity3 = AnimatedModelLoader.loadEntity(new MyFile(GeneralSettings.MODEL_FILE), new MyFile(GeneralSettings.DIFFUSE_FILE));
        AnimatedModel entity4 = AnimatedModelLoader.loadEntity(new MyFile(GeneralSettings.MODEL_FILE), new MyFile(GeneralSettings.DIFFUSE_FILE));
        AnimatedModel[] entities = new AnimatedModel[]{entity, entity2, entity3, entity4};

        for (AnimatedModel animatedModel : entities) {
            animatedModel.doAnimation(animation);
        }
        Scene scene = new Scene(entities, camera);
        scene.setLightDirection(GeneralSettings.LIGHT_DIR);
        return scene;
    }
}

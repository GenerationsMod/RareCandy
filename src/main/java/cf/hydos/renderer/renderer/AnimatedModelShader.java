package cf.hydos.renderer.renderer;

import cf.hydos.renderer.shaders.ShaderProgram;
import cf.hydos.renderer.shaders.UniformMat4Array;
import cf.hydos.renderer.shaders.UniformMatrix;
import cf.hydos.renderer.shaders.UniformSampler;
import cf.hydos.renderer.shaders.UniformVec3;
import cf.hydos.renderer.utils.MyFile;

public class AnimatedModelShader extends ShaderProgram {

	private static final int MAX_JOINTS = 64;// max number of joints in a skeleton
	private static final int DIFFUSE_TEX_UNIT = 0;

	private static final MyFile VERTEX_SHADER = new MyFile("shaders/animatedEntity", "animatedEntityVertex.glsl");
	private static final MyFile FRAGMENT_SHADER = new MyFile("shaders/animatedEntity", "animatedEntityFragment.glsl");

	protected UniformMatrix projectionViewMatrix = new UniformMatrix("projectionViewMatrix");
	protected UniformVec3 lightDirection = new UniformVec3("lightDirection");
	protected UniformMat4Array jointTransforms = new UniformMat4Array("jointTransforms", MAX_JOINTS);
	private UniformSampler diffuseMap = new UniformSampler("diffuseMap");

	/**
	 * Creates the shader program for the {@link AnimatedModelRenderer} by
	 * loading up the vertex and fragment shader code files. It also gets the
	 * location of all the specified uniform variables, and also indicates that
	 * the diffuse texture will be sampled from texture unit 0.
	 */
	public AnimatedModelShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER, "in_position", "in_textureCoords", "in_normal", "in_jointIndices", "in_weights");
		super.storeAllUniformLocations(projectionViewMatrix, diffuseMap, lightDirection, jointTransforms);
		connectTextureUnits();
	}

	/**
	 * Indicates which texture unit the diffuse texture should be sampled from.
	 */
	private void connectTextureUnits() {
		super.start();
		diffuseMap.loadTexUnit(DIFFUSE_TEX_UNIT);
		super.stop();
	}

}

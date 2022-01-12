package cf.hydos.renderer.scene;

import org.joml.Matrix4f;

public interface ICamera {
	
	public Matrix4f getViewMatrix();
	public Matrix4f getProjectionMatrix();
	public Matrix4f getProjectionViewMatrix();
	public void move();

}

#version 150

in vec3 in_position;
out float pass_height;

uniform mat4 projectionViewMatrix;

void main(void){
//	// Remove position from the view matrix and model matrix
//	mat4 viewMatrix = mat4(projectionViewMatrix);
//	viewMatrix[3][0] = 0;
//	viewMatrix[3][1] = 0;
//	viewMatrix[3][2] = 0;

	gl_Position = projectionViewMatrix * vec4(in_position, 1.0);
	pass_height = in_position.y;
	
}
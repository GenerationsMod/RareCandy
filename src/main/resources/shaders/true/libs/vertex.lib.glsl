mat4 getBoneTransform(Instance instance, uvec4 joints, vec4 weights) {
    return
    instance.boneTransforms[joints.x] * weights.x +
    instance.boneTransforms[joints.y] * weights.y +
    instance.boneTransforms[joints.z] * weights.z +
    instance.boneTransforms[joints.w] * weights.w;
}

TargetVertex getVertex() {
    DrawInfo drawInfo = drawInfos[gl_DrawID];
    Instance instance = instances[drawInfo.instance];

    SourceVertex inV = src[indices[gl_VertexID + meshOffsets[gl_DrawID]]];
    TargetVertex outV;

    mat4 skin = getBoneTransform(instance, inV.joints, inV.weights);
    mat3 skin3 = mat3(skin);

    vec4 skinnedPos = skin * vec4(inV.position, 1.0);
    vec3 skinnedNormal = normalize(skin3 * inV.normals);
    vec3 skinnedTangent = normalize(skin3 * inV.tangents.xyz);

    vec4 worldPos = instance.modelMatrix * skinnedPos;

    outV.position = worldPos.xyz;
    outV.texCoord = inV.texCoord;

    outV.normal = normalize(instance.normalMatrix * skinnedNormal);
    outV.tangent = normalize(instance.normalMatrix * skinnedTangent);
    outV.tangent = normalize(outV.tangent - dot(outV.tangent, outV.normal) * outV.normal);
    outV.bitangent = cross(outV.normal, outV.tangent) * inV.tangents.w;

    return outV;
}
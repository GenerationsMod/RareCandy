package cf.hydos.renderer.colladaLoader;

import cf.hydos.renderer.utils.MyFile;
import cf.hydos.renderer.dataStructures.AnimatedModelData;
import cf.hydos.renderer.dataStructures.AnimationData;
import cf.hydos.renderer.dataStructures.MeshData;
import cf.hydos.renderer.dataStructures.SkeletonData;
import cf.hydos.renderer.dataStructures.SkinningData;
import cf.hydos.renderer.xmlParser.XmlNode;
import cf.hydos.renderer.xmlParser.XmlParser;

public class ColladaLoader {

	public static AnimatedModelData loadColladaModel(MyFile colladaFile, int maxWeights) {
		XmlNode node = XmlParser.loadXmlFile(colladaFile);

		SkinLoader skinLoader = new SkinLoader(node.getChild("library_controllers"), maxWeights);
		SkinningData skinningData = skinLoader.extractSkinData();

		SkeletonLoader jointsLoader = new SkeletonLoader(node.getChild("library_visual_scenes"), skinningData.jointOrder);
		SkeletonData jointsData = jointsLoader.extractBoneData();

		GeometryLoader g = new GeometryLoader(node.getChild("library_geometries"), skinningData.verticesSkinData);
		MeshData meshData = g.extractModelData();

		return new AnimatedModelData(meshData, jointsData);
	}

	public static AnimationData loadColladaAnimation(MyFile colladaFile) {
		XmlNode node = XmlParser.loadXmlFile(colladaFile);
		XmlNode animNode = node.getChild("library_animations");
		XmlNode jointsNode = node.getChild("library_visual_scenes");
		AnimationLoader loader = new AnimationLoader(animNode, jointsNode);
		AnimationData animData = loader.extractAnimation();
		return animData;
	}

}

package gg.generations.rarecandy.renderer.loading;

public class PlaneGenerator {
//    public static MultiRenderObject generatePlane(Function<ModelLoader.Names, MultiRenderObject> function, float width, float length) {
//        var names = new ModelLoader.Names();
//        names.meshes().add("plane");
//        names.variants().add("plane");
//        names.materials().add("regular");
//        names.images().add("blank");
//
//
//        var obj = function.apply(names);
//        obj.variantRelationships[0][0] = 0;
//        obj.variants[0] = new Variant(0, true, Transform.DEFAULT);
//        obj.materials[0] = new Material(new int[] {0,0,0,0}, new MaterialValues().complete(), CullType.None, BlendType.Regular, 0, 0);
//        ModelLoader
//
//        var vertexBuffer = MemoryUtil.memAlloc(vertexlength * amount)
//                .putFloat(-width / 2).putFloat(0.0f).putFloat(-length / 2).putFloat(0.0f).putFloat(0.0f)
//                .putFloat(width / 2).putFloat(0.0f).putFloat(-length / 2).putFloat(1.0f).putFloat(0.0f)
//                .putFloat(-width / 2).putFloat(0.0f).putFloat(length / 2).putFloat(0.0f).putFloat(1.0f)
//                .putFloat(width / 2).putFloat( 0.0f).putFloat(length / 2).putFloat(1.0f).putFloat(1.0f)
//                .flip();
//
//
//        var indexBuffer = MemoryUtil.memAlloc(6 * 2)
//                .putShort((short) 0).putShort((short) 1).putShort((short) 2)
//                .putShort((short) 1).putShort((short) 3).putShort((short) 2)
//                .flip();
//
//        obj.meshes[0] = new GLModel(vertexBuffer, indexBuffer, 6, GL11.GL_UNSIGNED_SHORT, attributes);
//
//        return obj;
//    }


    //    public static Pair<List<Runnable>, MultiRenderObject> generateCube(float width, float height, float length, String image) {
//
//        var attributes = List.of(Attribute.POSITION, Attribute.TEXCOORD);
//
//        var vertexLength = calculateVertexSize(attributes);
//        var amount = 24; // 6 faces * 4 vertices per face
//
//        var vertexBuffer = MemoryUtil.memAlloc(vertexLength * amount)
//                .putFloat(-width / 2).putFloat(-height / 2).putFloat(-length / 2).putFloat(0.0f).putFloat(0.0f) // Bottom-left-back
//                .putFloat(width / 2).putFloat(-height / 2).putFloat(-length / 2).putFloat(1.0f).putFloat(0.0f) // Bottom-right-back
//                .putFloat(width / 2).putFloat(height / 2).putFloat(-length / 2).putFloat(1.0f).putFloat(1.0f) // Top-right-back
//                .putFloat(-width / 2).putFloat(height / 2).putFloat(-length / 2).putFloat(0.0f).putFloat(1.0f) // Top-left-back
//
//                .putFloat(-width / 2).putFloat(-height / 2).putFloat(length / 2).putFloat(0.0f).putFloat(0.0f) // Bottom-left-front
//                .putFloat(width / 2).putFloat(-height / 2).putFloat(length / 2).putFloat(1.0f).putFloat(0.0f) // Bottom-right-front
//                .putFloat(width / 2).putFloat(height / 2).putFloat(length / 2).putFloat(1.0f).putFloat(1.0f) // Top-right-front
//                .putFloat(-width / 2).putFloat(height / 2).putFloat(length / 2).putFloat(0.0f).putFloat(1.0f) // Top-left-front
//
//                .putFloat(-width / 2).putFloat(-height / 2).putFloat(-length / 2).putFloat(0.0f).putFloat(0.0f) // Reuse Bottom-left-back
//                .putFloat(width / 2).putFloat(-height / 2).putFloat(-length / 2).putFloat(1.0f).putFloat(0.0f) // Reuse Bottom-right-back
//                .putFloat(width / 2).putFloat(-height / 2).putFloat(length / 2).putFloat(1.0f).putFloat(1.0f) // Bottom-right-front
//                .putFloat(-width / 2).putFloat(-height / 2).putFloat(length / 2).putFloat(0.0f).putFloat(1.0f) // Bottom-left-front
//
//                .putFloat(-width / 2).putFloat(height / 2).putFloat(-length / 2).putFloat(0.0f).putFloat(0.0f) // Reuse Top-left-back
//                .putFloat(width / 2).putFloat(height / 2).putFloat(-length / 2).putFloat(1.0f).putFloat(0.0f) // Reuse Top-right-back
//                .putFloat(width / 2).putFloat(height / 2).putFloat(length / 2).putFloat(1.0f).putFloat(1.0f) // Reuse Top-right-front
//                .putFloat(-width / 2).putFloat(height / 2).putFloat(length / 2).putFloat(0.0f).putFloat(1.0f) // Reuse Top-left-front
//
//                .putFloat(-width / 2).putFloat(-height / 2).putFloat(-length / 2).putFloat(0.0f).putFloat(0.0f) // Reuse Bottom-left-back
//                .putFloat(-width / 2).putFloat(height / 2).putFloat(-length / 2).putFloat(1.0f).putFloat(0.0f) // Reuse Top-left-back
//                .putFloat(-width / 2).putFloat(height / 2).putFloat(length / 2).putFloat(1.0f).putFloat(1.0f) // Reuse Top-left-front
//                .putFloat(-width / 2).putFloat(-height / 2).putFloat(length / 2).putFloat(0.0f).putFloat(1.0f) // Reuse Bottom-left-front
//
//                .putFloat(width / 2).putFloat(-height / 2).putFloat(-length / 2).putFloat(0.0f).putFloat(0.0f) // Reuse Bottom-right-back
//                .putFloat(width / 2).putFloat(height / 2).putFloat(-length / 2).putFloat(1.0f).putFloat(0.0f) // Reuse Top-right-back
//                .putFloat(width / 2).putFloat(height / 2).putFloat(length / 2).putFloat(1.0f).putFloat(1.0f) // Reuse Top-right-front
//                .putFloat(width / 2).putFloat(-height / 2).putFloat(length / 2).putFloat(0.0f).putFloat(1.0f) // Reuse Bottom-right-front
//                .flip();
//
//        var indexBuffer = MemoryUtil.memAlloc(36 * 2)
//                .putShort((short) 0).putShort((short) 1).putShort((short) 2) // Back face
//                .putShort((short) 2).putShort((short) 3).putShort((short) 0)
//
//                .putShort((short) 4).putShort((short) 5).putShort((short) 6) // Front face
//                .putShort((short) 6).putShort((short) 7).putShort((short) 4)
//
//                .putShort((short) 8).putShort((short) 9).putShort((short) 10) // Bottom face
//                .putShort((short) 10).putShort((short) 11).putShort((short) 8)
//
//                .putShort((short) 12).putShort((short) 13).putShort((short) 14) // Top face
//                .putShort((short) 14).putShort((short) 15).putShort((short) 12)
//
//                .putShort((short) 16).putShort((short) 17).putShort((short) 18) // Left face
//                .putShort((short) 18).putShort((short) 19).putShort((short) 16)
//
//                .putShort((short) 20).putShort((short) 21).putShort((short) 22) // Right face
//                .putShort((short) 22).putShort((short) 23).putShort((short) 20)
//                .flip();
//
//        List<Runnable> glCalls = new ArrayList<>();
//        var model = new GLModel(vertexBuffer, indexBuffer, glCalls, 36, GL11.GL_UNSIGNED_SHORT, attributes);
//
//        var obj = new MultiRenderObject<MeshObject>();
//
//        var mesh = new MeshObject();
//        mesh.setup(Map.of("cube", new Variant(new Material("cube", new MaterialImages().setDiffuse(image), new MaterialValues().complete(), false, CullType.None, BlendType.None, "solid", 0, 0))), model, "cube");
//        obj.add(mesh);
//
//        return new Pair<>(glCalls, obj);
//    }
}

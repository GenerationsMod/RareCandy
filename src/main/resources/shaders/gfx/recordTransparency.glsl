// record stage of transparent rendering outlined in
// https://on-demand.gputechconf.com/gtc/2014/presentations/S4385-order-independent-transparency-opengl.pdf
layout (early_fragment_tests) in;

layout(rgba32ui) uniform coherent uimageBuffer translucencyBuffer;
layout(r32ui) uniform coherent uimage2D imgListHead;
layout(binding=0) uniform atomic_uint counter;

void emit(vec4 color) {
    uint idx = atomicCounterIncrement(counter) + 1u;// position where data is stored
    if(idx < imageSize(translucencyBuffer)) {
        ivec2 coord = ivec2(gl_FragCoord.xy);
        uint prev = imageAtomicExchange(imgListHead, coord, idx); // next in offsets
        imageStore(translucencyBuffer, int(idx), uvec4(packUnorm4x8(color), floatBitsToUint(gl_FragCoord.z), prev, idx));
    }
}
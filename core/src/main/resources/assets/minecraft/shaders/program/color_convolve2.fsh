#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform vec3 Gray;
uniform vec3 RedMatrix;
uniform vec3 GreenMatrix;
uniform vec3 BlueMatrix;
uniform vec3 Offset;
uniform vec3 ColorScale;
uniform float Saturation;

out vec4 fragColor;

void main() {
    vec4 inTexel = texture(DiffuseSampler, texCoord);
    
    // Color Matrix
    float redValue = dot(inTexel.rgb, RedMatrix);
    float greenValue = dot(inTexel.rgb, GreenMatrix);
    float blueValue = dot(inTexel.rgb, BlueMatrix);
    vec3 outColor = vec3(redValue, greenValue, blueValue);
    
    // Offset & Scale
    outColor = (outColor * ColorScale) + Offset;
    
    // Saturation
    float luma = dot(outColor, Gray);
    vec3 chroma = outColor - vec3(luma);
    outColor = (chroma * Saturation) + vec3(luma);
    
    fragColor = vec4(outColor, inTexel.a);
}

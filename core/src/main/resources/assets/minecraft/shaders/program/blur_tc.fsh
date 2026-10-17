#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 BlurDir;
uniform float Radius;

out vec4 fragColor;

void main() {
    vec2 offset = oneTexel * BlurDir * Radius;

    vec4 color = texture(DiffuseSampler, texCoord) * 0.2270270270;
    color += texture(DiffuseSampler, texCoord + offset * 1.3846153846) * 0.3162162162;
    color += texture(DiffuseSampler, texCoord - offset * 1.3846153846) * 0.3162162162;
    color += texture(DiffuseSampler, texCoord + offset * 3.2307692308) * 0.0702702703;
    color += texture(DiffuseSampler, texCoord - offset * 3.2307692308) * 0.0702702703;

    fragColor = vec4(color.rgb, 1.0);
}

#version 110

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D TranslucentSampler;
uniform sampler2D TranslucentDepthSampler;
uniform sampler2D ItemEntitySampler;
uniform sampler2D ItemEntityDepthSampler;
uniform sampler2D ParticlesSampler;
uniform sampler2D ParticlesDepthSampler;
uniform sampler2D WeatherSampler;
uniform sampler2D WeatherDepthSampler;
uniform sampler2D CloudsSampler;
uniform sampler2D CloudsDepthSampler;
uniform float BloomIntensity;

varying vec2 texCoord;
varying vec2 oneTexel;

vec3 blend(vec3 destination, vec4 source) {
    return (destination * (1.0 - source.a)) + source.rgb;
}

float luminance(vec3 rgb) {
    return dot(rgb, vec3(0.2125, 0.7154, 0.0721));
}

struct Layer {
    vec4 color;
    float depth;
};

#define NUM_LAYERS 6

Layer layers[NUM_LAYERS];
int layerIndices[NUM_LAYERS];

void init_arrays() {
    layers[0] = Layer(vec4(texture2D(DiffuseSampler, texCoord).rgb, 1.0), texture2D(DiffuseDepthSampler, texCoord).r);
    layers[1] = Layer(texture2D(TranslucentSampler, texCoord), texture2D(TranslucentDepthSampler, texCoord).r);
    layers[2] = Layer(texture2D(ItemEntitySampler, texCoord), texture2D(ItemEntityDepthSampler, texCoord).r);
    layers[3] = Layer(texture2D(ParticlesSampler, texCoord), texture2D(ParticlesDepthSampler, texCoord).r);
    layers[4] = Layer(texture2D(WeatherSampler, texCoord), texture2D(WeatherDepthSampler, texCoord).r);
    layers[5] = Layer(texture2D(CloudsSampler, texCoord), texture2D(CloudsDepthSampler, texCoord).r);

    for (int ii = 0; ii < NUM_LAYERS; ++ii) {
        layerIndices[ii] = ii;
    }

    for (int ii = 0; ii < NUM_LAYERS; ++ii) {
        for (int jj = 0; jj < NUM_LAYERS - ii - 1; ++jj) {
            if (layers[layerIndices[jj]].depth < layers[layerIndices[jj + 1]].depth) {
                int temp = layerIndices[jj];
                layerIndices[jj] = layerIndices[jj + 1];
                layerIndices[jj + 1] = temp;
            }
        }
    }
}

#define LUM_TAPS 9

float estimate_luminance() {
  float accum = 0.0;
  for (int ii = 0; ii < LUM_TAPS; ++ii) {
    float xx = float(ii + 1) / float(LUM_TAPS + 1);
    for (int jj = 0; jj < LUM_TAPS; ++jj) {
      float yy = float(jj + 1) / float(LUM_TAPS + 1);
      accum += luminance(texture2D(DiffuseSampler, vec2(xx, yy)).rgb);
    }
  }
  return accum / float(LUM_TAPS * LUM_TAPS);
}

#define TAPS 6

float angle = radians(360.0 / float(TAPS));
float angleSin = sin(angle);
float angleCos = cos(angle);
mat2 rotationMatrix = mat2(angleCos, angleSin, -angleSin, angleCos);

void main() {
    init_arrays();

    vec3 OutTexel = vec3(0.0);
    for (int ii = 0; ii < NUM_LAYERS; ++ii) {
        OutTexel = blend(OutTexel, layers[layerIndices[ii]].color);
    }

    vec2 tapOffset = vec2(0.0, 1.0 / 512.0); // Fixed step for varying resolutions

    vec3 bloomAccumulator = vec3(0.0);
    for (int ii = 0; ii < TAPS; ++ii) {
      bloomAccumulator += texture2D(DiffuseSampler, texCoord + tapOffset).rgb;
      tapOffset = rotationMatrix * tapOffset;
    }
    bloomAccumulator /= float(TAPS);
    bloomAccumulator *= (0.75 - estimate_luminance());

    bloomAccumulator *= BloomIntensity;

    gl_FragColor = vec4(OutTexel + bloomAccumulator, 1.0);
}
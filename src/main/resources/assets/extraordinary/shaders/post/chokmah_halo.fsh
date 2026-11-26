#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

const float HALO_STRENGTH = 0.55;

float luminance(vec3 color) {
	return dot(color, vec3(0.299, 0.587, 0.114));
}

float luminanceAt(ivec2 pixel, ivec2 lastPixel) {
	return luminance(texelFetch(InSampler, clamp(pixel, ivec2(0), lastPixel), 0).rgb);
}

vec3 antialias(vec3 center, ivec2 pixel, ivec2 size) {
	ivec2 lastPixel = size - 1;
	float northwest = luminanceAt(pixel + ivec2(-1, 1), lastPixel);
	float northeast = luminanceAt(pixel + ivec2(1, 1), lastPixel);
	float southwest = luminanceAt(pixel + ivec2(-1, -1), lastPixel);
	float southeast = luminanceAt(pixel + ivec2(1, -1), lastPixel);
	float middle = luminance(center);
	float lowest = min(middle, min(min(northwest, northeast), min(southwest, southeast)));
	float highest = max(middle, max(max(northwest, northeast), max(southwest, southeast)));
	if (highest - lowest < max(0.05, highest * 0.125)) {
		return center;
	}

	// FXAA estimates the edge direction and uses fractional color samples along it.
	float horizontalGradient = (northeast + southeast) - (northwest + southwest);
	float verticalGradient = (northwest + northeast) - (southwest + southeast);
	vec2 direction = vec2(-verticalGradient, horizontalGradient);
	float reduction = max((northwest + northeast + southwest + southeast) / 32.0, 1.0 / 128.0);
	vec2 texelSize = 1.0 / vec2(size);
	vec2 uv = (vec2(pixel) + 0.5) * texelSize;
	vec2 span = clamp(direction / (min(abs(direction.x), abs(direction.y)) + reduction),
			vec2(-8.0), vec2(8.0)) * texelSize;
	vec3 narrow = 0.5 * (textureLod(InSampler, uv - span / 6.0, 0.0).rgb
			+ textureLod(InSampler, uv + span / 6.0, 0.0).rgb);
	vec3 wide = 0.5 * narrow + 0.25 * (textureLod(InSampler, uv - span * 0.5, 0.0).rgb
			+ textureLod(InSampler, uv + span * 0.5, 0.0).rgb);
	float wideLuminance = luminance(wide);
	return wideLuminance < lowest || wideLuminance > highest ? narrow : wide;
}

bool isWhite(vec3 color) {
	return all(greaterThanEqual(color, vec3(0.999)));
}

float backgroundAt(ivec2 pixel, ivec2 lastPixel) {
	pixel = clamp(pixel, ivec2(0), lastPixel);
	// The world clears reversed depth to zero. Translucent terrain can still contribute color there.
	if (texelFetch(DepthSampler, pixel, 0).r > 0.0) {
		return 0.0;
	}
	return isWhite(texelFetch(InSampler, pixel, 0).rgb) ? 1.0 : 0.0;
}

float axialBackground(ivec2 pixel, ivec2 lastPixel, int radius) {
	return backgroundAt(pixel + ivec2(-radius, 0), lastPixel)
			+ backgroundAt(pixel + ivec2(radius, 0), lastPixel)
			+ backgroundAt(pixel + ivec2(0, -radius), lastPixel)
			+ backgroundAt(pixel + ivec2(0, radius), lastPixel);
}

void main() {
	ivec2 size = textureSize(InSampler, 0);
	ivec2 lastPixel = size - 1;
	ivec2 pixel = clamp(ivec2(texCoord * vec2(size)), ivec2(0), lastPixel);
	vec4 color = texelFetch(InSampler, pixel, 0);
	vec3 smoothed = antialias(color.rgb, pixel, size);
	if (isWhite(color.rgb)) {
		fragColor = vec4(smoothed, color.a);
		return;
	}

	float adjacent = axialBackground(pixel, lastPixel, 1);
	float diagonal = backgroundAt(pixel + ivec2(-1, -1), lastPixel)
			+ backgroundAt(pixel + ivec2(-1, 1), lastPixel)
			+ backgroundAt(pixel + ivec2(1, -1), lastPixel)
			+ backgroundAt(pixel + ivec2(1, 1), lastPixel);
	float outer = axialBackground(pixel, lastPixel, 2);
	float fringe = axialBackground(pixel, lastPixel, 4);

	float coverage = min((4.0 * adjacent + 2.0 * diagonal + 2.0 * outer + fringe) / 18.0, 1.0);
	fragColor = vec4(mix(smoothed, vec3(1.0), coverage * HALO_STRENGTH), color.a);
}

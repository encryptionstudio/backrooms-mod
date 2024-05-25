package com.kpabr.backrooms.init;

import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.minecraft.util.Identifier;
import com.kpabr.backrooms.BackroomsMod;

public class BackroomsShaders {

	private static final ManagedShaderEffect BRIGHTNESS_AND_BLOOM_SHADER = ShaderEffectManager.getInstance()
    		.manage(new Identifier(BackroomsMod.ModId, "shaders/post/bloom.json"));

    private static boolean renderDark = false;

    public static void init() {
        setBrightness(0.9f);
        setBloomIntensity(1f);
        ShaderEffectRenderCallback.EVENT.register(tickDelta -> {
    	    if (renderDark) {
                BRIGHTNESS_AND_BLOOM_SHADER.render(tickDelta);
            }
    	});
    }


    public static void setRenderDark(boolean dark) {
        renderDark = dark;
    }

    public static boolean getRenderDark() {
        return renderDark;
    }

    public static void setBrightness(float brightness) {
        BRIGHTNESS_AND_BLOOM_SHADER.setUniformValue("ColorModulate", brightness, brightness, brightness, 1f);
    }

    public static void setBloomIntensity(float intensity) {

        BRIGHTNESS_AND_BLOOM_SHADER.setUniformValue("BloomIntensity", intensity);
    }
}
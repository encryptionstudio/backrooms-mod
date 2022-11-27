package com.kpabr.backrooms.client;

import com.kpabr.backrooms.entity.renderer.living.HoundEntityRenderer;
import com.kpabr.backrooms.entity.renderer.living.WretchEntityRenderer;
import com.kpabr.backrooms.init.*;
import com.kpabr.backrooms.particle.FireSaltParticle;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

public class BackroomsClient implements ClientModInitializer {


	@Override
	public void onInitializeClient() {
		ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register(((atlasTexture, registry) ->
			registry.register(new Identifier("minecraft", "particle/flame"))
		));



		ParticleFactoryRegistry.getInstance().register(BackroomsParticles.FIRESALT_PARTICLE, new FireSaltParticle.FireSaltFactory());

		EntityRendererRegistry.register(BackroomsProjectiles.FIRE_SALT_PROJECTILE_ENTITY_TYPE, FlyingItemEntityRenderer::new);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BackroomsBlocks.FIRESALT_CRYSTAL);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), BackroomsBlocks.TILEMOLD);
		BlockRenderLayerMap.INSTANCE.putBlock(BackroomsBlocks.PYROIL, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BackroomsBlocks.OFFICE_DOOR, RenderLayer.getTranslucent());

		EntityRendererRegistry.register(BackroomsEntities.HOUND, HoundEntityRenderer::new);
		EntityRendererRegistry.register(BackroomsEntities.WRETCHED, WretchEntityRenderer::new);

		//almond water fluid rendering
		FluidRenderHandlerRegistry.INSTANCE.register(BackroomsFluids.ALMOND_WATER_STILL,
				new SimpleFluidRenderHandler(SimpleFluidRenderHandler.WATER_STILL,
						SimpleFluidRenderHandler.WATER_FLOWING,
						SimpleFluidRenderHandler.WATER_OVERLAY, 0xE0E0FF));
		FluidRenderHandlerRegistry.INSTANCE.register(BackroomsFluids.ALMOND_WATER_FLOWING,
				new SimpleFluidRenderHandler(SimpleFluidRenderHandler.WATER_STILL,
						SimpleFluidRenderHandler.WATER_FLOWING,
						SimpleFluidRenderHandler.WATER_OVERLAY, 0xE0E0FF));


	}

	public static MinecraftClient getClient() {
		return MinecraftClient.getInstance();
	}
}
package dev.jaegyu.malachite;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class MalachiteAddonsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This makes the block *actually* transparent
		BlockRenderLayerMap.INSTANCE.putBlock(MalachiteAddons.COPPER_RAIL, RenderLayer.getCutout());
	}
}
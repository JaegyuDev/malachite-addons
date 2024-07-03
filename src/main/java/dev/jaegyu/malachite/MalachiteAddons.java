package dev.jaegyu.malachite;

import dev.jaegyu.malachite.Block.CopperRail;
import net.fabricmc.api.ModInitializer;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MalachiteAddons implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("malachite-addons");
	public static final String MOD_ID = "malachite-addons";

	@Override
	public void onInitialize() {
		final Block COPPER_RAIL = new CopperRail(Block.Settings.copy(Blocks.POWERED_RAIL));
		final Item COPPER_RAIL_ITEM = new BlockItem(COPPER_RAIL, new Item.Settings());
		LOGGER.info("Hello Fabric world!");

		// Register our new rail
		Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "copper_rail"), COPPER_RAIL);
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "copper_rail"), COPPER_RAIL_ITEM);
	}
}
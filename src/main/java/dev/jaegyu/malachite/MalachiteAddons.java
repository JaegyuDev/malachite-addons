package dev.jaegyu.malachite;

import dev.jaegyu.malachite.block.CopperRail;
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
	public static final String MOD_ID = "malachite-addons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Block COPPER_RAIL = new CopperRail(
			Block.Settings.copy(Blocks.POWERED_RAIL));
	public static final Item COPPER_RAIL_ITEM = new BlockItem(COPPER_RAIL, new Item.Settings());
	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");

		// Register our new rail
		Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "copper_rail"), COPPER_RAIL);
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "copper_rail"), COPPER_RAIL_ITEM);
	}
}
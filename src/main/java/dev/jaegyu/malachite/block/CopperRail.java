package dev.jaegyu.malachite.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CopperRail extends PoweredRailBlock {
    private static final int MaximumPoweredDistance = 16;
    public CopperRail(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean isPoweredByOtherRails(World world, BlockPos pos, BlockState state, boolean bl, int distance) {
        if (distance >= MaximumPoweredDistance) {
            return false;
        } else {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            boolean bl2 = true;
            RailShape railShape = state.get(SHAPE);
            switch (railShape) {
                case NORTH_SOUTH:
                    if (bl) {
                        k++;
                    } else {
                        k--;
                    }
                    break;
                case EAST_WEST:
                    if (bl) {
                        i--;
                    } else {
                        i++;
                    }
                    break;
                case ASCENDING_EAST:
                    if (bl) {
                        i--;
                    } else {
                        i++;
                        j++;
                        bl2 = false;
                    }

                    railShape = RailShape.EAST_WEST;
                    break;
                case ASCENDING_WEST:
                    if (bl) {
                        i--;
                        j++;
                        bl2 = false;
                    } else {
                        i++;
                    }

                    railShape = RailShape.EAST_WEST;
                    break;
                case ASCENDING_NORTH:
                    if (bl) {
                        k++;
                    } else {
                        k--;
                        j++;
                        bl2 = false;
                    }

                    railShape = RailShape.NORTH_SOUTH;
                    break;
                case ASCENDING_SOUTH:
                    if (bl) {
                        k++;
                        j++;
                        bl2 = false;
                    } else {
                        k--;
                    }

                    railShape = RailShape.NORTH_SOUTH;
            }

            return this.isPoweredByOtherRails(world, new BlockPos(i, j, k), bl, distance, railShape)
                    || bl2 && this.isPoweredByOtherRails(world, new BlockPos(i, j - 1, k), bl, distance, railShape);
        }
    }
}

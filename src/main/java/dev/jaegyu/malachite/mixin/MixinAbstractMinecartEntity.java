package dev.jaegyu.malachite.mixin;

import com.mojang.datafixers.util.Pair;
import dev.jaegyu.malachite.MalachiteAddons;
import dev.jaegyu.malachite.block.CopperRail;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.util.math.MathHelper;

@Mixin(AbstractMinecartEntity.class)
public abstract class MixinAbstractMinecartEntity extends VehicleEntity {
	public MixinAbstractMinecartEntity(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	/**
	 * @author JaegyuDev
	 * @reason Max speed on the cart is tied to the minecart entity and I need to change this behaviour
	 * 			such that I can specify some values on a rail block and allow the cart to vary in max speed.
	 * 			If you have a better way of doing this let me know, otherwise my hands are tied.
	 */
	@Overwrite()
	public void moveOnRail(BlockPos pos, BlockState state) {
		this.onLanding();
		double cartX = this.getX();
		double cartY = this.getY();
		double cartZ = this.getZ();
		Vec3d snappedPosition = this.snapPositionToRail(cartX, cartY, cartZ);
		cartY = (double)pos.getY();
		boolean isRailPowered = false;
		boolean isDecelerating = false;

		if (state.isOf(Blocks.POWERED_RAIL)) {
			isRailPowered = (Boolean)state.get(PoweredRailBlock.POWERED);
			isDecelerating = !isRailPowered;
		}

		// WOOOO WE GOT COPPER RAILS WORKING LES GOOO
		if (state.isOf(MalachiteAddons.COPPER_RAIL)) {
			isRailPowered = state.get(CopperRail.POWERED);
			isDecelerating = !isRailPowered;
		}

		double velFactor = 0.0078125;
		if (this.isTouchingWater()) {
			velFactor *= 0.2;
		}
		Vec3d currentVel = this.getVelocity();
		RailShape railShape = state.get(((AbstractRailBlock)state.getBlock()).getShapeProperty());
		switch (railShape) {
			case ASCENDING_EAST:
				this.setVelocity(currentVel.add(-velFactor, 0.0, 0.0));
				cartY++;
				break;
			case ASCENDING_WEST:
				this.setVelocity(currentVel.add(velFactor, 0.0, 0.0));
				cartY++;
				break;
			case ASCENDING_NORTH:
				this.setVelocity(currentVel.add(0.0, 0.0, velFactor));
				cartY++;
				break;
			case ASCENDING_SOUTH:
				this.setVelocity(currentVel.add(0.0, 0.0, -velFactor));
				cartY++;
		}

		currentVel = this.getVelocity();
		Pair<Vec3i, Vec3i> pair = getAdjacentRailPositionsByShape(railShape);
		Vec3i firstAdjRail = pair.getFirst();
		Vec3i secondAdjRail = pair.getSecond();
		double railDeltaX = (double)(secondAdjRail.getX() - firstAdjRail.getX());
		double railDeltaZ = (double)(secondAdjRail.getZ() - firstAdjRail.getZ());
		double railDistance = Math.sqrt(railDeltaX * railDeltaX + railDeltaZ * railDeltaZ);
		double velProjection = currentVel.x * railDeltaX + currentVel.z * railDeltaZ;
		if (velProjection < 0.0) {
			railDeltaX = -railDeltaX;
			railDeltaZ = -railDeltaZ;
		}

		double l = Math.min(2.0, currentVel.horizontalLength());
		currentVel = new Vec3d(l * railDeltaX / railDistance, currentVel.y, l * railDeltaZ / railDistance);
		this.setVelocity(currentVel);

		Entity passenger = this.getFirstPassenger();
		if (passenger instanceof PlayerEntity) {
			Vec3d passVel = passenger.getVelocity();
			double passSpeedSqrd = passVel.horizontalLengthSquared();
			double cartSpeedSqrd = this.getVelocity().horizontalLengthSquared();
			if (passSpeedSqrd > 1.0E-4 && cartSpeedSqrd < 0.01) {
				this.setVelocity(this.getVelocity().add(passVel.x * 0.1, 0.0, passVel.z * 0.1));
				isDecelerating = false;
			}
		}

		if (isDecelerating) {
			double o = this.getVelocity().horizontalLength();
			if (o < 0.03) {
				this.setVelocity(Vec3d.ZERO);
			} else {
				this.setVelocity(this.getVelocity().multiply(0.5, 0.0, 0.5));
			}
		}

		// honestly I don't really know why they seemed to do the same math twice here.
		double startX1 = (double)pos.getX() + 0.5 + (double)firstAdjRail.getX() * 0.5;
		double startZ1 = (double)pos.getZ() + 0.5 + (double)firstAdjRail.getZ() * 0.5;
		double startX2 = (double)pos.getX() + 0.5 + (double)secondAdjRail.getX() * 0.5;
		double startZ2 = (double)pos.getZ() + 0.5 + (double)secondAdjRail.getZ() * 0.5;
		double deltaX = startX2 - startX1;
		double deltaZ = startZ2 - startZ1;
		double positionFactor;
		if (deltaX == 0.0) {
			positionFactor = cartZ - (double)pos.getZ();
		} else if (deltaZ == 0.0) {
			positionFactor = cartX - (double)pos.getX();
		} else {
			double offsetX = cartX - startX1;
			double offsetZ = cartZ - startZ1;
			positionFactor = (offsetX * deltaX + offsetZ * deltaZ) * 2.0;
		}

		cartX = startX1 + deltaX * positionFactor;
		cartZ = startZ1 + deltaZ * positionFactor;
		this.setPosition(cartX, cartY, cartZ);

		// speed clamping
		double hasPassengers = this.hasPassengers() ? 0.75 : 1.0;

		double maxSpeed = this.getMaxSpeed();
		if (state.isOf(Blocks.POWERED_RAIL)) {
			maxSpeed = maxSpeed * 1.5;
		}

		currentVel = this.getVelocity();
		this.move(MovementType.SELF, new Vec3d(MathHelper.clamp(hasPassengers * currentVel.x, -maxSpeed, maxSpeed), 0.0, MathHelper.clamp(hasPassengers * currentVel.z, -maxSpeed, maxSpeed)));
		if (firstAdjRail.getY() != 0 && MathHelper.floor(this.getX()) - pos.getX() == firstAdjRail.getX() && MathHelper.floor(this.getZ()) - pos.getZ() == firstAdjRail.getZ()) {
			this.setPosition(this.getX(), this.getY() + (double)firstAdjRail.getY(), this.getZ());
		} else if (secondAdjRail.getY() != 0 && MathHelper.floor(this.getX()) - pos.getX() == secondAdjRail.getX() && MathHelper.floor(this.getZ()) - pos.getZ() == secondAdjRail.getZ()) {
			this.setPosition(this.getX(), this.getY() + (double)secondAdjRail.getY(), this.getZ());
		}

		this.applySlowdown();
		Vec3d currentRailPos = this.snapPositionToRail(this.getX(), this.getY(), this.getZ());
		if (currentRailPos != null && snappedPosition != null) {
			double verticalDiff = (snappedPosition.y - currentRailPos.y) * 0.05;
			Vec3d currentVelocity = this.getVelocity();
			double w = currentVelocity.horizontalLength();
			if (w > 0.0) {
				this.setVelocity(currentVelocity.multiply((w + verticalDiff) / w, 1.0, (w + verticalDiff) / w));
			}

			this.setPosition(this.getX(), currentRailPos.y, this.getZ());
		}

		int x = MathHelper.floor(this.getX());
		int y = MathHelper.floor(this.getZ());
		if (x != pos.getX() || y != pos.getZ()) {
			Vec3d v2 = this.getVelocity();
			double w = v2.horizontalLength();
			this.setVelocity(w * (double)(x - pos.getX()), v2.y, w * (double)(y - pos.getZ()));
		}

		double accelFactor = 0.06;
		if (state.isOf(Blocks.POWERED_RAIL)) {
			accelFactor = 0.12;
		}

		if (isRailPowered) {
			Vec3d newNewCurrVel = this.getVelocity();
			double currentSpeed = newNewCurrVel.horizontalLength();
			if (currentSpeed > 0.01) {;
				this.setVelocity(newNewCurrVel.add(newNewCurrVel.x / currentSpeed * accelFactor, 0.0, newNewCurrVel.z / currentSpeed * 0.06));
			} else {
				Vec3d adjustedVelocity = this.getVelocity();
				double adjustedX = adjustedVelocity.x;
				double adjustedZ = adjustedVelocity.z;
				if (railShape == RailShape.EAST_WEST) {
					if (this.willHitBlockAt(pos.west())) {
						adjustedX = 0.02;
					} else if (this.willHitBlockAt(pos.east())) {
						adjustedX = -0.02;
					}
				} else {
					if (railShape != RailShape.NORTH_SOUTH) {
						return;
					}

					if (this.willHitBlockAt(pos.north())) {
						adjustedZ = 0.02;
					} else if (this.willHitBlockAt(pos.south())) {
						adjustedZ = -0.02;
					}
				}

				this.setVelocity(adjustedX, adjustedVelocity.y, adjustedZ);
			}
		}
	}

	@Shadow
	protected abstract boolean willHitBlockAt(BlockPos west);


	@Shadow
	protected abstract void applySlowdown();

	@Shadow
	protected abstract double getMaxSpeed();

	@Shadow
	private static Pair<Vec3i, Vec3i> getAdjacentRailPositionsByShape(RailShape shape) {
		return null;
	}

	@Shadow
	public abstract Vec3d snapPositionToRail(double x, double y, double z);

}
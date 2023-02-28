package com.kpabr.backrooms.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class MachineryBlock extends Block {
	public static final BooleanProperty POWERED = Properties.POWERED;

	public MachineryBlock(Settings settings) {
		super(settings);
		setDefaultState(this.stateManager.getDefaultState().with(POWERED, false));
	}
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		state = state.cycle(POWERED);
		world.setBlockState(pos, state, 1, 0);

		return ActionResult.success(world.isClient);
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
		stateManager.add(POWERED);
	}
}

package dizzyd.simplethings.blocks

import alexiil.mc.lib.attributes.AttributeList
import alexiil.mc.lib.attributes.AttributeProvider
import dizzyd.simplethings.SimpleThings
import dizzyd.simplethings.entities.FluidChuteBlockEntity
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class FluidChuteBlock: BlockWithEntity(FabricBlockSettings.of(Material.METAL).strength(3.0f)),
    BlockEntityProvider, AttributeProvider {

    override fun createBlockEntity(pos: BlockPos?, state: BlockState?): BlockEntity? {
        return FluidChuteBlockEntity(pos!!, state!!)
    }

    override fun getRenderType(state: BlockState?): BlockRenderType {
        return BlockRenderType.MODEL;
    }

    override fun <T : BlockEntity?> getTicker(
        world: World?,
        state: BlockState?,
        type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? {
        return checkType(type, SimpleThings.FLUID_CHUTE_BLOCK_ENTITY,
            {_, _, _, blockEntity ->
                FluidChuteBlockEntity.tick(blockEntity) })
    }

    override fun addAllAttributes(
        world: World?,
        pos: BlockPos?,
        state: BlockState?,
        to: AttributeList<*>?
    ) {
        (world?.getBlockEntity(pos) as FluidChuteBlockEntity).let {
            to!!.offer(it.fluidInv)
        }
    }

}
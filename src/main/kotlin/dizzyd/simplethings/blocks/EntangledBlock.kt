package dizzyd.simplethings.blocks

import dizzyd.simplethings.SimpleThings
import dizzyd.simplethings.entities.EntangledBlockEntity
import dizzyd.simplethings.items.EntangledBlockItem
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager

class EntangledBlock : Block(FabricBlockSettings.of(Material.METAL).strength(4.0f)),
    BlockEntityProvider {
    companion object {
        val LOGGER = LogManager.getLogger()
    }

    override fun createBlockEntity(pos: BlockPos?, state: BlockState?): BlockEntity? {
        return EntangledBlockEntity(pos!!, state!!)
    }

    override fun onPlaced(
        world: World?,
        pos: BlockPos?,
        state: BlockState?,
        placer: LivingEntity?,
        itemStack: ItemStack?
    ) {
        if (world?.isClient == true) {
            return
        }

        val blockEntity = world?.getBlockEntity(pos)
        if (blockEntity is EntangledBlockEntity) {
            SimpleThings.ENTANGLED_BLOCK_MGR.register(blockEntity.entangledId, pos!!)
        }
    }

    override fun onSteppedOn(world: World?, pos: BlockPos?, state: BlockState?, entity: Entity?) {
        if (world?.isClient == true) {
            return
        }

        val blockEntity = world?.getBlockEntity(pos)
        if (blockEntity is EntangledBlockEntity) {
            val dest = blockEntity.getDestination()
            if (dest != null) {
                entity?.teleport(dest.x.toDouble(), dest.y.toDouble(), dest.z.toDouble())
            }
        }
    }

    override fun getDroppedStacks(
        state: BlockState?,
        builder: LootContext.Builder?
    ): MutableList<ItemStack> {
        // Construct a stack from the underlying block entity; this needs to be done so
        // that the resulting drop has the appropriate associated UUID
        val blockEntity = builder?.get(LootContextParameters.BLOCK_ENTITY) as BlockEntity
        val stack = EntangledBlockItem.stackFromBlockEntity(blockEntity)
        if (stack != null) {
            return mutableListOf(stack)
        }

        // This should never happen, but adding a log message so if a world gets corrupted/out of sync
        // we have a shot at knowing where
        LOGGER.error("Missing EntangledBlockEntity at ${blockEntity.pos}; falling back to loot tables for drops")
        return super.getDroppedStacks(state, builder)
    }
}
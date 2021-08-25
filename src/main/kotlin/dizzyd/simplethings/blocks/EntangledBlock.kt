package dizzyd.simplethings.blocks

import dizzyd.simplethings.SimpleThings
import dizzyd.simplethings.entities.EntangledBlockEntity
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameter
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import org.apache.logging.log4j.LogManager
import java.util.concurrent.ThreadLocalRandom
import java.util.logging.Logger

class EntangledBlock: Block(FabricBlockSettings.of(Material.METAL).strength(4.0f)), BlockEntityProvider {
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

        LOGGER.info("Stepping on $pos")

        val blockEntity = world?.getBlockEntity(pos)
        if (blockEntity is EntangledBlockEntity) {
            val dest = blockEntity.getDestination()
            LOGGER.info("Destination: $dest")
            if (dest != null) {
                entity?.teleport(dest.x.toDouble(), dest.y.toDouble(), dest.z.toDouble())
            }
        }
    }

    override fun getDroppedStacks(
        state: BlockState?,
        builder: LootContext.Builder?
    ): MutableList<ItemStack> {
        val stack = ItemStack(SimpleThings.ENTANGLED_BLOCK_ITEM, 1)
        val blockEntity = builder?.get(LootContextParameters.BLOCK_ENTITY) as BlockEntity
        if (blockEntity is EntangledBlockEntity) {
            stack.getOrCreateSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY)
                .putLong("entangled_uuid", blockEntity.entangledId)
        }
        return MutableList(1) { stack }
    }
}
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
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap

class EntangledBlock : Block(FabricBlockSettings.of(Material.METAL).strength(4.0f)),
    BlockEntityProvider {
    companion object {
        val LOGGER = LogManager.getLogger()
        val LAST_TELEPORT = HashMap<UUID, Long>()
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

    override fun onBreak(world: World?, pos: BlockPos?, state: BlockState?, player: PlayerEntity?) {
        if (world?.isClient == true) {
            return
        }

        val blockEntity = world?.getBlockEntity(pos)
        if (blockEntity is EntangledBlockEntity) {
            SimpleThings.ENTANGLED_BLOCK_MGR.unregister(blockEntity.entangledId, pos!!)
        }

        super.onBreak(world, pos, state, player)
    }

    override fun onSteppedOn(world: World?, pos: BlockPos?, state: BlockState?, entity: Entity?) {
        if (world?.isClient == true) {
            return
        }

        if (entity?.isPlayer == false) {
            return
        }

        // Check last transport time for this entity; if it's less than 5 seconds ago, ignore
        // this step
        val now = Instant.now().epochSecond
        val lastTeleport = LAST_TELEPORT.getOrDefault(entity!!.uuid, 0)
        if (now - lastTeleport < 5) {
            return
        }

        val blockEntity = world?.getBlockEntity(pos)
        if (blockEntity is EntangledBlockEntity) {
            if (doTransport(world, blockEntity.entangledId, entity as PlayerEntity)) {
                // Transport was successful! Update last teleport timestamp to prevent
                // people getting stuck in loops
                EntangledBlock.LAST_TELEPORT.put(entity.uuid, now)
            }
        }
    }

    fun doTransport(world: World, destId: String, player: PlayerEntity): Boolean {
        // First look up the destination; bail if no destination is found
        // N.B. we must use the block the player is STANDING on
        val destPos = SimpleThings.ENTANGLED_BLOCK_MGR.getDestination(destId, player?.blockPos.down())
            ?: return false

        // Validate that the destination is in the current dimension
        val destEntity = world?.getBlockEntity(destPos)
        if (destEntity is EntangledBlockEntity) {
            player.teleport(
                destPos.x.toDouble() + 0.5,
                destPos.y.toDouble() + 1.0,
                destPos.z.toDouble() + 0.5)

            return true
        }

        return false
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
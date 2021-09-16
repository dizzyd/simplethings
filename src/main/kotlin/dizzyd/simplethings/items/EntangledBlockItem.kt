package dizzyd.simplethings.items

import dizzyd.simplethings.SimpleThings
import dizzyd.simplethings.blocks.EntangledBlock
import dizzyd.simplethings.entities.EntangledBlockEntity
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.*
import net.minecraft.text.LiteralText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.world.World
import org.apache.logging.log4j.core.util.UuidUtil
import java.lang.String.format
import java.time.Instant
import java.util.concurrent.ThreadLocalRandom

class EntangledBlockItem(b: EntangledBlock) : BlockItem(b, FabricItemSettings().group(ItemGroup.MISC)){
    companion object {
        fun stackFromBlockEntity(blockEntity: BlockEntity?): ItemStack? {
            if (blockEntity is EntangledBlockEntity) {
                val stack = ItemStack(SimpleThings.ENTANGLED_BLOCK_ITEM, 1)
                stack.getOrCreateSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY)
                    .putString("entangled_uuid", blockEntity.entangledId)
                return stack
            }

            return null
        }
    }


    override fun onCraft(stack: ItemStack?, world: World?, player: PlayerEntity?) {
        if (!world!!.isClient) {
            stack!!.getOrCreateSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY)
                .putString("entangled_uuid", UuidUtil.getTimeBasedUuid().toString())
        }
    }

    override fun canPlace(context: ItemPlacementContext?, state: BlockState?): Boolean {
        if (context!!.world.isClient) {
            return false
        }

        val entangledId = context.stack.getOrCreateSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY)
            .getString("entangled_uuid")

        // Get destination position; if there isn't one, we allow placement using normal rules
        val destPos = SimpleThings.ENTANGLED_BLOCK_MGR.getDestination(entangledId, context.blockPos)
            ?: return super.canPlace(context, state)

        // We have a destination block position; ensure that it's registered in the current world/dimension
        val blockEntity = context.world.getBlockEntity(destPos)
        if (blockEntity is EntangledBlockEntity) {
            return super.canPlace(context, state)
        }

        // Let the user know why they can't place the block
        context.player?.sendMessage(LiteralText("Entangled blocks must be placed in the same dimension"),
        false)

        return false
    }

    override fun use(
        world: World?,
        user: PlayerEntity?,
        hand: Hand?
    ): TypedActionResult<ItemStack> {
        if (world?.isClient == true) {
            return super.use(world, user, hand)
        }

        // Enable the user to use a block in hand; this solves the problem of putting down a block in a remote
        // place and being unable to remove it when you're done.
        val stack = user!!.getStackInHand(hand)
        val id = stack.nbt!!.getCompound(BlockItem.BLOCK_ENTITY_TAG_KEY).getString("entangled_uuid")
        if (!SimpleThings.ENTANGLED_BLOCK.doTransport(world!!, id, user)) {
            user?.sendMessage(LiteralText("No destination found in this dimension!"), false)
        }

        return super.use(world, user, hand)
    }
}
package dizzyd.simplethings.items

import dizzyd.simplethings.SimpleThings
import dizzyd.simplethings.blocks.EntangledBlock
import dizzyd.simplethings.entities.EntangledBlockEntity
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.*
import net.minecraft.text.LiteralText
import net.minecraft.util.ActionResult
import net.minecraft.world.World
import java.util.concurrent.ThreadLocalRandom

class EntangledBlockItem(b: EntangledBlock) : BlockItem(b, FabricItemSettings().group(ItemGroup.MISC)){
    override fun onCraft(stack: ItemStack?, world: World?, player: PlayerEntity?) {
        if (!world!!.isClient) {
            stack!!.getOrCreateSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY)
                .putLong("entangled_uuid", ThreadLocalRandom.current().nextLong(Long.MAX_VALUE))
        }
    }

    override fun canPlace(context: ItemPlacementContext?, state: BlockState?): Boolean {
        if (context!!.world.isClient) {
            return false
        }

        val entangledId = context!!.stack.getOrCreateSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY)
            .getLong("entangled_uuid")

        val destPos = SimpleThings.ENTANGLED_BLOCK_MGR.getDestination(entangledId, context.blockPos)

        // No destination registered; we don't need to check that it's in the same world
        if (destPos == null) {
            return super.canPlace(context, state)
        }

        // We have a destination block position; ensure that it's registered in the current
        // world
        val blockEntity = context.world.getBlockEntity(destPos)
        if (blockEntity is EntangledBlockEntity) {
            return super.canPlace(context, state)
        }

        // Let the user know why they can't place the block
        context.player?.sendMessage(LiteralText("Entangled blocks must be placed in the same dimension"),
        false)

        return false
    }
}
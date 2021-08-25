package dizzyd.simplethings.entities

import dizzyd.simplethings.SimpleThings
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

class EntangledBlockEntity(pos: BlockPos, state: BlockState):
    BlockEntity(SimpleThings.ENTANGLED_BLOCK_ENTITY, pos, state) {

    var entangledId = ""

    override fun writeNbt(nbt: NbtCompound?): NbtCompound {
        nbt?.putString("entangled_uuid", entangledId)
        return super.writeNbt(nbt)
    }

    override fun readNbt(nbt: NbtCompound?) {
        entangledId = nbt!!.getString("entangled_uuid")
    }

    override fun markRemoved() {
        if (world?.isClient == false) {
            SimpleThings.ENTANGLED_BLOCK_MGR.unregister(entangledId, pos)
        }

        super.markRemoved()
    }

    fun getDestination(): BlockPos? {
        // Validate that the destination has the other entangled block
        val dest = SimpleThings.ENTANGLED_BLOCK_MGR.getDestination(entangledId, pos)
        if (dest != null) {
            val destBlockEntity = world?.getBlockEntity(dest)
            if (destBlockEntity is EntangledBlockEntity && destBlockEntity.entangledId == entangledId) {
                return dest
            }
        }

        return null
    }
}
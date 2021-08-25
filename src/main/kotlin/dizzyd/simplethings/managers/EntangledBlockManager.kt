package dizzyd.simplethings.managers

import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState
import org.apache.logging.log4j.LogManager

class EntangledBlockManager: PersistentState() {

    val blocks = HashMap<String, MutableSet<BlockPos>>()

    fun register(id: String, pos: BlockPos) {
        LOGGER.info("Registered entangled block $id: at $pos")
        val positions = blocks.getOrDefault(id, mutableSetOf())
        positions.add(pos)
        blocks.put(id, positions)

        this.markDirty()
    }

    fun unregister(id: String, pos: BlockPos) {
        LOGGER.info("Unregistered entangled block $id: at $pos")
        val positions = blocks.getOrDefault(id, mutableSetOf())
        positions.remove(pos)
        blocks.put(id, positions)

        this.markDirty()
    }

    fun getDestination(id: String, fromPos: BlockPos): BlockPos? {
        val positions = blocks.getOrDefault(id, mutableSetOf())
        LOGGER.info("Positions: $positions")
        return positions.firstOrNull{ pos -> pos != fromPos }
    }

    override fun writeNbt(nbt: NbtCompound?): NbtCompound {
        val blockCompound = NbtCompound()
        for ((k, v) in blocks) {
            if (v.size > 0) {
                blockCompound.putLongArray(k.toString(), v.map { pos -> pos.asLong() })
            }
        }

        nbt!!.put("blocks", blockCompound)
        return nbt
    }

    companion object {
        val LOGGER = LogManager.getLogger()

        fun fromNbt(nbt: NbtCompound): EntangledBlockManager {
            val blockManager = EntangledBlockManager()
            val blockEntries = nbt.getCompound("blocks")
            for (k in blockEntries.keys) {
                val positionLongs = blockEntries.getLongArray(k)
                if (positionLongs.size > 0) {
                    val positions =
                        positionLongs.map { pos -> BlockPos.fromLong(pos) }.toMutableSet()
                    blockManager.blocks.put(k, positions)
                }
            }

            return blockManager
        }
    }
}
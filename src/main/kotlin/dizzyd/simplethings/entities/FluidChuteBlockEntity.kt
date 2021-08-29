package dizzyd.simplethings.entities

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import alexiil.mc.lib.attributes.fluid.FluidExtractable
import alexiil.mc.lib.attributes.fluid.FluidInsertable
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv
import dizzyd.simplethings.SimpleThings
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class FluidChuteBlockEntity(pos: BlockPos, state: BlockState): BlockEntity(SimpleThings.FLUID_CHUTE_BLOCK_ENTITY,pos, state) {
    val fluidInv = SimpleFixedFluidInv(1, FluidAmount.BUCKET)

    override fun readNbt(nbt: NbtCompound?) {
        super.readNbt(nbt)
        fluidInv.fromTag(nbt)
    }

    override fun writeNbt(nbt: NbtCompound?): NbtCompound {
        super.writeNbt(nbt)
        fluidInv.toTag(nbt)
        return nbt!!
    }

    companion object {
        val MOVE_AMOUNT = FluidAmount.of(100, 1000)

        private fun shiftFluid(source: FluidExtractable, dest: FluidInsertable) {
            var vol = source.attemptAnyExtraction(MOVE_AMOUNT, Simulation.SIMULATE)
            if (!vol.isEmpty) {
                val remainingVol = dest.insert(vol)
                source.extract(vol.amount().sub(remainingVol.amount()))
            }
        }

        fun tick(entity: FluidChuteBlockEntity) {
            val source = FluidAttributes.EXTRACTABLE.getFromNeighbour(entity, Direction.UP)
            val dest = FluidAttributes.INSERTABLE.getFromNeighbour(entity, Direction.DOWN)

            shiftFluid(source, entity.fluidInv)
            shiftFluid(entity.fluidInv, dest)
        }
    }

}
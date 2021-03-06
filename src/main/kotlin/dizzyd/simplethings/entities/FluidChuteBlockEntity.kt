package dizzyd.simplethings.entities

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import alexiil.mc.lib.attributes.fluid.FluidExtractable
import alexiil.mc.lib.attributes.fluid.FluidInsertable
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.impl.EmptyFluidExtractable
import alexiil.mc.lib.attributes.fluid.impl.SimpleFixedFluidInv
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import dizzyd.simplethings.SimpleThings
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.impl.transfer.fluid.CauldronStorage
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

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
        val MOVE_AMOUNT_LBA = FluidAmount.of(100, 1000)

        private fun shiftFluid(source: FluidExtractable, dest: FluidInsertable) {
            var vol = source.attemptAnyExtraction(MOVE_AMOUNT_LBA, Simulation.SIMULATE)
            if (!vol.isEmpty) {
                val remainingVol = dest.insert(vol)
                source.extract(vol.amount().sub(remainingVol.amount()))
            }
        }

        private fun drainCauldron(world: World, pos: BlockPos, vol: FluidVolume, dest: FluidInsertable) {
            val remainingVol = dest.attemptInsertion(vol, Simulation.SIMULATE)
            if (remainingVol.isEmpty) {
                dest.insert(vol)
                world.setBlockState(pos, Blocks.CAULDRON.defaultState)
            }
        }

        fun tick(world: World, entity: FluidChuteBlockEntity) {
            // First attempt to use Fabric's fluid transfer API
            val sourceFluid = FluidStorage.SIDED.find(world, entity.pos.offset(Direction.UP), Direction.DOWN)
            val destFluid = FluidStorage.SIDED.find(world, entity.pos.offset(Direction.DOWN), Direction.UP)
            if (sourceFluid != null && destFluid != null) {
                var moveAmount = FluidConstants.BUCKET / 20 // 1 bucket/second @ 20 tps
                if (sourceFluid is CauldronStorage) {
                    moveAmount = FluidConstants.BUCKET // Cauldrons can only move 1 bucket per txn
                }
                StorageUtil.move(sourceFluid, destFluid, { fluid -> true}, moveAmount, null)
            }
            else {
                // Fallback to libblockattribute approach
                val source = FluidAttributes.EXTRACTABLE.getFromNeighbour(entity, Direction.UP)
                val dest = FluidAttributes.INSERTABLE.getFromNeighbour(entity, Direction.DOWN)
                if (source != EmptyFluidExtractable.NULL) {
                    shiftFluid(source, entity.fluidInv)
                    shiftFluid(entity.fluidInv, dest)
                } else {
                    // Check for cauldron; Fabric's fluid transfer handles this transparently
                    val abovePos = entity.pos.up()
                    val aboveBlockState = world.getBlockState(abovePos)
                    if (aboveBlockState == Blocks.LAVA_CAULDRON.defaultState) {
                        drainCauldron(world, abovePos, FluidKeys.LAVA.withAmount(FluidAmount.BUCKET), dest)
                    } else if (aboveBlockState == Blocks.WATER_CAULDRON.defaultState) {
                        drainCauldron(world, abovePos, FluidKeys.WATER.withAmount(FluidAmount.BUCKET), dest)
                    }
                }

            }
        }
    }

}
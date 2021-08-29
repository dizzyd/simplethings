package dizzyd.simplethings
import dizzyd.simplethings.blocks.EntangledBlock
import dizzyd.simplethings.blocks.FluidChuteBlock
import dizzyd.simplethings.entities.EntangledBlockEntity
import dizzyd.simplethings.entities.FluidChuteBlockEntity
import dizzyd.simplethings.items.EntangledBlockItem
import dizzyd.simplethings.managers.EntangledBlockManager
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemGroup
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager

@Suppress("UNUSED")
object SimpleThings: ModInitializer, ServerLifecycleEvents.ServerStarted {
    private const val MOD_ID = "simplethings"

    val LOGGER = LogManager.getLogger()

    val ENTANGLED_BLOCK = EntangledBlock()
    lateinit var ENTANGLED_BLOCK_ENTITY: BlockEntityType<EntangledBlockEntity>
    lateinit var ENTANGLED_BLOCK_ITEM: EntangledBlockItem
    lateinit var ENTANGLED_BLOCK_MGR: EntangledBlockManager

    val FLUID_CHUTE_BLOCK = FluidChuteBlock()
    lateinit var FLUID_CHUTE_BLOCK_ITEM: BlockItem
    lateinit var FLUID_CHUTE_BLOCK_ENTITY: BlockEntityType<FluidChuteBlockEntity>

    override fun onInitialize() {
        Registry.register(Registry.BLOCK, Identifier(MOD_ID, "entangled_block"),
            ENTANGLED_BLOCK)
        ENTANGLED_BLOCK_ITEM = Registry.register(Registry.ITEM, Identifier(MOD_ID, "entangled_block"),
            EntangledBlockItem(ENTANGLED_BLOCK))
        ENTANGLED_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "$MOD_ID:entangled_block_entity",
            FabricBlockEntityTypeBuilder.create(::EntangledBlockEntity, ENTANGLED_BLOCK).build(null))

        Registry.register(Registry.BLOCK, Identifier(MOD_ID, "fluid_chute_block"),
            FLUID_CHUTE_BLOCK)
        FLUID_CHUTE_BLOCK_ITEM = Registry.register(Registry.ITEM, Identifier(MOD_ID, "fluid_chute_block"), BlockItem(
            FLUID_CHUTE_BLOCK, FabricItemSettings().group(ItemGroup.MISC)))
        FLUID_CHUTE_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "$MOD_ID:fluid_chute_block_entity",
            FabricBlockEntityTypeBuilder.create(::FluidChuteBlockEntity, FLUID_CHUTE_BLOCK).build(null))

        ServerLifecycleEvents.SERVER_STARTED.register(this)
    }

    override fun onServerStarted(server: MinecraftServer?){
        ENTANGLED_BLOCK_MGR = server!!.overworld.persistentStateManager.getOrCreate(
            {nbt -> EntangledBlockManager.fromNbt(nbt)},
            { -> EntangledBlockManager() },
            "entangled_block")!!
    }


}

package dizzyd.simplethings
import dizzyd.simplethings.blocks.EntangledBlock
import dizzyd.simplethings.entities.EntangledBlockEntity
import dizzyd.simplethings.items.EntangledBlockItem
import dizzyd.simplethings.managers.EntangledBlockManager
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.block.entity.BlockEntityType
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

    override fun onInitialize() {
        Registry.register(Registry.BLOCK, Identifier(MOD_ID, "entangled_block"),
            ENTANGLED_BLOCK)
        ENTANGLED_BLOCK_ITEM = Registry.register(Registry.ITEM, Identifier(MOD_ID, "entangled_block"),
            EntangledBlockItem(ENTANGLED_BLOCK))

        ENTANGLED_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "$MOD_ID:entangled_block_entity",
            FabricBlockEntityTypeBuilder.create(::EntangledBlockEntity, ENTANGLED_BLOCK).build(null))

        ServerLifecycleEvents.SERVER_STARTED.register(this)
    }

    override fun onServerStarted(server: MinecraftServer?){
        ENTANGLED_BLOCK_MGR = server!!.overworld.persistentStateManager.getOrCreate(
            {nbt -> EntangledBlockManager.fromNbt(nbt)},
            { -> EntangledBlockManager() },
            "entangled_block")!!
    }


}

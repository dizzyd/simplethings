package dizzyd.simplethings
import net.fabricmc.api.ModInitializer
@Suppress("UNUSED")
object ModName: ModInitializer {
    private const val MOD_ID = "simplethings"
    override fun onInitialize() {
        println("SimpleThings mod has been initialized.")
    }
}
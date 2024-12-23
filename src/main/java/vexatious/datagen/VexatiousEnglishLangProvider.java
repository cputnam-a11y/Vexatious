package vexatious.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import vexatious.Vexatious;

import java.util.concurrent.CompletableFuture;

public class VexatiousEnglishLangProvider extends FabricLanguageProvider {
    protected VexatiousEnglishLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, "en_us", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(message("evoker_prefix"), "Evoker: ");
        translationBuilder.add(message("no_evokers"), "No evokers are currently active.");
    }
    private static String message(String key) {
        return Vexatious.MOD_ID + ".message." + key;
    }
}

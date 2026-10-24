package com.example.trexmod.client;

import com.example.trexmod.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;

public class TRexModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelLayerRegistry.registerModelLayer(TRexModel.LAYER, TRexModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MosasaurusModel.LAYER, MosasaurusModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntities.TREX, TRexRenderer::new);
        EntityRendererRegistry.register(ModEntities.MOSASAURUS, MosasaurusRenderer::new);
    }
}

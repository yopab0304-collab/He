package com.example.trexmod.client;

import com.example.trexmod.TRexMod;
import com.example.trexmod.entity.MosasaurusEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class MosasaurusRenderer extends MobRenderer<MosasaurusEntity, MosasaurusRenderState, MosasaurusModel> {
    private static final Identifier TEXTURE = TRexMod.id("textures/entity/mosasaurus.png");

    public MosasaurusRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new MosasaurusModel(ctx.bakeLayer(MosasaurusModel.LAYER)), 1.5f);
    }

    @Override
    public MosasaurusRenderState createRenderState() {
        return new MosasaurusRenderState();
    }

    @Override
    public void extractRenderState(MosasaurusEntity entity, MosasaurusRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.roaring = entity.isAggressive();
        state.inWater = entity.isInWater();
        state.roarT = entity.getRoarProgress(partialTick);
    }

    @Override
    public Identifier getTextureLocation(MosasaurusRenderState state) {
        return TEXTURE;
    }
}

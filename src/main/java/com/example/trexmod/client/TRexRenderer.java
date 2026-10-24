package com.example.trexmod.client;

import com.example.trexmod.TRexMod;
import com.example.trexmod.entity.TRexEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class TRexRenderer extends MobRenderer<TRexEntity, TRexRenderState, TRexModel> {
    private static final Identifier TEXTURE = TRexMod.id("textures/entity/trex.png");

    public TRexRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TRexModel(ctx.bakeLayer(TRexModel.LAYER)), 1.6f);
    }

    @Override
    public TRexRenderState createRenderState() {
        return new TRexRenderState();
    }

    @Override
    public void extractRenderState(TRexEntity entity, TRexRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.roaring = entity.isAggressive();
        state.sitting = entity.isInSittingPose();
        state.roarT = entity.getRoarProgress(partialTick);
    }

    @Override
    public Identifier getTextureLocation(TRexRenderState state) {
        return TEXTURE;
    }
}

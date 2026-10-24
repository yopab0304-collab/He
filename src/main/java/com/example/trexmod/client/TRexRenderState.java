package com.example.trexmod.client;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class TRexRenderState extends LivingEntityRenderState {
    public boolean roaring;   // กำลังก้าวร้าว (ปากอ้านิดๆ)
    public boolean sitting;   // ถูกสั่งให้นั่ง
    public float roarT;       // 1 -> 0 ระหว่างเล่นท่าคำราม
}

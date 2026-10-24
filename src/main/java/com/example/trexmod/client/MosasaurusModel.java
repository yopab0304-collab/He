package com.example.trexmod.client;

import com.example.trexmod.TRexMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/** โมซาซอรัส: ตัวยาว หัวใหญ่ ขากรรไกรเต็มฟัน ครีบ 4 อัน หางสามท่อนมีครีบแนวตั้ง */
public class MosasaurusModel extends EntityModel<MosasaurusRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(TRexMod.id("mosasaurus"), "main");

    private final ModelPart body, neck, head, jaw, tail1, tail2, tail3;
    private final ModelPart[] ff = new ModelPart[2], fb = new ModelPart[2];

    public MosasaurusModel(ModelPart root) {
        super(root);
        body = root.getChild("body");
        neck = body.getChild("neck");
        head = neck.getChild("head");
        jaw = head.getChild("jaw");
        tail1 = body.getChild("tail1");
        tail2 = tail1.getChild("tail2");
        tail3 = tail2.getChild("tail3");
        ff[0] = body.getChild("fin_front_r"); ff[1] = body.getChild("fin_front_l");
        fb[0] = body.getChild("fin_back_r");  fb[1] = body.getChild("fin_back_l");
    }

    private static PartDefinition box(PartDefinition p, String n, int u, int v,
                                      float x, float y, float z, float w, float h, float d, PartPose pose) {
        return p.addOrReplaceChild(n, CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, w, h, d), pose);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = box(root, "body", 0, 0, -8, -7, -20, 16, 14, 40, PartPose.offset(0, 8, 0));
        PartDefinition neck = box(body, "neck", 0, 100, -6, -6, -10, 12, 12, 10, PartPose.offset(0, -1, -20));
        PartDefinition head = box(neck, "head", 0, 60, -6.5f, -5, -22, 13, 10, 22, PartPose.offset(0, -1, -10));
        PartDefinition jaw = box(head, "jaw", 76, 96, -5.5f, 0, -20, 11, 4, 20, PartPose.offset(0, 3, -2));

        CubeListBuilder up = CubeListBuilder.create().texOffs(244, 122);
        for (float x : new float[]{-6.2f, 5.2f}) for (float z = -4; z >= -20; z -= 4) up.addBox(x, 5, z, 1, 3, 1);
        head.addOrReplaceChild("teeth_up", up, PartPose.ZERO);
        CubeListBuilder low = CubeListBuilder.create().texOffs(244, 122);
        for (float x : new float[]{-5.0f, 4.0f}) for (float z = -3; z >= -18; z -= 3) low.addBox(x, -3, z, 1, 3, 1);
        jaw.addOrReplaceChild("teeth_low", low, PartPose.ZERO);

        PartDefinition t1 = box(body, "tail1", 120, 0, -6, -6, 0, 12, 12, 22, PartPose.offset(0, 0, 20));
        PartDefinition t2 = box(t1, "tail2", 120, 40, -4, -4.5f, 0, 8, 9, 22, PartPose.offset(0, 0, 22));
        PartDefinition t3 = box(t2, "tail3", 190, 24, -2.5f, -3, 0, 5, 6, 16, PartPose.offset(0, 0, 22));
        box(t3, "fluke", 200, 50, -1, -16, -2, 2, 30, 12, PartPose.offset(0, 0, 16));

        // ครีบหน้า / ครีบหลัง ซ้าย-ขวา
        box(body, "fin_front_r", 190, 0, 0, -1, -4, 14, 2, 8, PartPose.offset(8, 5, -8));
        box(body, "fin_front_l", 190, 0, -14, -1, -4, 14, 2, 8, PartPose.offset(-8, 5, -8));
        box(body, "fin_back_r", 190, 12, 0, -1, -3, 10, 2, 6, PartPose.offset(7, 6, 12));
        box(body, "fin_back_l", 190, 12, -10, -1, -3, 10, 2, 6, PartPose.offset(-7, 6, 12));
        return LayerDefinition.create(mesh, 256, 128);
    }

    @Override
    public void setupAnim(MosasaurusRenderState s) {
        super.setupAnim(s);

        float spd = Math.min(s.walkAnimationSpeed, 1.0f);
        float age = s.ageInTicks;
        float phase = s.walkAnimationPos * 0.45f + age * 0.05f;
        float amp = 0.18f + 0.45f * spd;
        float roar = s.roarT > 0 ? Mth.sin((1.0f - s.roarT) * Mth.PI) : 0f;
        float bite = Mth.sin(s.attackTime * Mth.PI);

        // ตัวเอียงตามทิศที่ว่าย (หัวทิ่ม/หัวเชิด)
        body.xRot += s.xRot * Mth.DEG_TO_RAD;

        if (!s.inWater) {            // ขึ้นบก: นอนตะแคงดิ้น
            body.zRot += 1.3f + Mth.sin(age * 0.9f) * 0.12f;
            amp = 0.55f;
            phase = age * 0.5f;
        }

        // คลื่นหาง: ส่งต่อทีละท่อน
        tail1.yRot += Mth.sin(phase) * amp * 0.6f;
        tail2.yRot += Mth.sin(phase - 0.8f) * amp * 0.9f;
        tail3.yRot += Mth.sin(phase - 1.6f) * amp * 1.2f;
        body.yRot += Mth.sin(phase + 0.5f) * amp * 0.15f;
        neck.yRot += Mth.sin(phase + 1.2f) * amp * 0.12f;

        // มองตามเป้าหมาย
        head.yRot += s.yRot * Mth.DEG_TO_RAD * 0.6f;
        head.xRot += s.xRot * Mth.DEG_TO_RAD * 0.2f;

        // ครีบพาย
        float rowF = 0.3f + Mth.sin(phase * 2.0f) * 0.3f * (0.5f + spd);
        float rowB = 0.25f + Mth.sin(phase * 2.0f + 1.5f) * 0.2f * (0.5f + spd);
        ff[0].zRot += rowF;  ff[1].zRot -= rowF;
        ff[0].xRot += Mth.sin(phase) * 0.25f * (0.5f + spd);  ff[1].xRot -= Mth.sin(phase) * 0.25f * (0.5f + spd);
        fb[0].zRot += rowB;  fb[1].zRot -= rowB;

        // ปาก: หายใจ / อ้ากว้างตอนคำราม / งับตอนกัด
        jaw.xRot += 0.04f + 0.03f * Mth.sin(age * 0.05f) + (s.roaring ? 0.2f : 0f);
        jaw.xRot += roar * (1.1f + 0.1f * Mth.sin(age * 2.0f));
        head.xRot -= roar * 0.35f;
        neck.xRot -= roar * 0.25f;
        body.xRot -= roar * 0.2f;
        body.zRot += Mth.sin(age * 2.5f) * 0.03f * roar;

        jaw.xRot += bite * 1.0f;
        head.xRot += bite * 0.2f;
        neck.xRot += bite * 0.25f;
    }
}

package com.example.trexmod.client;

import com.example.trexmod.TRexMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/** T-Rex ท่าตัวเอียง ขาหลังงอแบบไดโนเสาร์ แขนจิ๋ว หางเรียวยาว ฟันแหลม */
public class TRexModel extends EntityModel<TRexRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(TRexMod.id("trex"), "main");

    private final ModelPart hip, neck, head, jaw, tail1, tail2, tail3;
    private final ModelPart[] arm = new ModelPart[2];
    private final ModelPart[] thigh = new ModelPart[2], shin = new ModelPart[2], meta = new ModelPart[2], foot = new ModelPart[2];

    public TRexModel(ModelPart root) {
        super(root);
        hip = root.getChild("hip");
        neck = hip.getChild("neck");
        head = neck.getChild("head");
        jaw = head.getChild("jaw");
        tail1 = hip.getChild("tail1");
        tail2 = tail1.getChild("tail2");
        tail3 = tail2.getChild("tail3");
        String[] sides = {"r", "l"};
        for (int i = 0; i < 2; i++) {
            arm[i] = hip.getChild("arm_" + sides[i]);
            thigh[i] = root.getChild("thigh_" + sides[i]);
            shin[i] = thigh[i].getChild("shin");
            meta[i] = shin[i].getChild("meta");
            foot[i] = meta[i].getChild("foot");
        }
    }

    private static PartDefinition box(PartDefinition p, String n, int u, int v,
                                      float x, float y, float z, float w, float h, float d, PartPose pose) {
        return p.addOrReplaceChild(n, CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, w, h, d), pose);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition hip = root.addOrReplaceChild("hip", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-6.5f, -8, -15, 13, 16, 27), PartPose.offsetAndRotation(0, -14, 3, -0.15f, 0, 0));

        PartDefinition neck = box(hip, "neck", 52, 102, -4, -4.5f, -10, 8, 9, 10,
                PartPose.offsetAndRotation(0, -5, -13, -0.55f, 0, 0));
        PartDefinition head = box(neck, "head", 52, 56, -5.5f, -5.5f, -15, 11, 11, 15,
                PartPose.offsetAndRotation(0, 0, -10, 0.7f, 0, 0));
        box(head, "jaw", 52, 84, -4.5f, 0, -13, 9, 3, 13, PartPose.offset(0, 4.5f, -1));

        // ฟันบน (ห้อยลง) และฟันล่าง (ชี้ขึ้น)
        CubeListBuilder up = CubeListBuilder.create().texOffs(122, 120);
        for (float x : new float[]{-4.8f, 3.8f}) for (float z : new float[]{-14, -11, -8, -5}) up.addBox(x, 5.5f, z, 1, 2, 1);
        up.addBox(-1.5f, 5.5f, -15.2f, 1, 2, 1).addBox(0.5f, 5.5f, -15.2f, 1, 2, 1);
        head.addOrReplaceChild("teeth_up", up, PartPose.ZERO);
        PartDefinition jawDef = head.getChild("jaw");
        CubeListBuilder low = CubeListBuilder.create().texOffs(122, 120);
        for (float x : new float[]{-3.8f, 2.8f}) for (float z : new float[]{-12, -9, -6}) low.addBox(x, -2, z, 1, 2, 1);
        jawDef.addOrReplaceChild("teeth_low", low, PartPose.ZERO);

        // หางเรียวยาว 3 ท่อน
        PartDefinition t1 = box(hip, "tail1", 0, 46, -4.5f, -4.5f, 0, 9, 9, 16, PartPose.offsetAndRotation(0, -3, 11, 0.12f, 0, 0));
        PartDefinition t2 = box(t1, "tail2", 0, 72, -3, -3, 0, 6, 6, 16, PartPose.offset(0, 0, 16));
        box(t2, "tail3", 0, 95, -1.5f, -1.5f, 0, 3, 3, 14, PartPose.offset(0, 0, 16));

        String[] sides = {"r", "l"};
        for (int i = 0; i < 2; i++) {
            float sx = i == 0 ? -1 : 1;
            // แขนจิ๋ว
            box(hip, "arm_" + sides[i], 116, 0, -1, 0, -1, 2, 7, 2,
                    PartPose.offsetAndRotation(sx * 5.5f, 5, -12, 0.9f, 0, sx * 0.15f));
            // ขา: ต้นขา -> แข้ง -> ข้อเท้า -> ฝ่าเท้า (ท่าเดินแบบสัตว์เท้าสองขา)
            PartDefinition th = box(root, "thigh_" + sides[i], 84, 0, -3.5f, -1, -4, 7, 12, 8,
                    PartPose.offsetAndRotation(sx * 7, -9, 3, -0.4f, 0, 0));
            PartDefinition sh = box(th, "shin", 84, 22, -2, 0, -2.5f, 4, 12, 5, PartPose.offsetAndRotation(0, 11, 0, 0.8f, 0, 0));
            PartDefinition me = box(sh, "meta", 104, 22, -2, 0, -2, 4, 8, 4, PartPose.offsetAndRotation(0, 12, 0, -0.7f, 0, 0));
            box(me, "foot", 84, 40, -2.5f, 0, -8, 5, 4, 10, PartPose.offsetAndRotation(0, 8, 0, 0.3f, 0, 0));
        }
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(TRexRenderState s) {
        super.setupAnim(s);   // รีเซ็ตกลับท่าเริ่มต้น แล้วค่อยบวกแอนิเมชัน

        float spd = Math.min(s.walkAnimationSpeed, 1.0f);
        float a = s.walkAnimationPos * 0.6662f;
        float age = s.ageInTicks;
        float roar = s.roarT > 0 ? Mth.sin((1.0f - s.roarT) * Mth.PI) : 0f;      // 0 -> 1 -> 0
        float bite = Mth.sin(s.attackTime * Mth.PI);                              // ท่ากัด

        // ---- เดิน ----
        for (int i = 0; i < 2; i++) {
            float ph = i == 0 ? 0f : Mth.PI;
            float sw = Mth.cos(a + ph);
            thigh[i].xRot += sw * 0.7f * spd;
            shin[i].xRot += (0.5f + 0.5f * Mth.cos(a + ph + 1.3f)) * 0.9f * spd;
            meta[i].xRot -= (0.5f + 0.5f * Mth.cos(a + ph + 1.3f)) * 0.5f * spd;
            foot[i].xRot += -sw * 0.3f * spd;
        }
        hip.zRot += Mth.sin(a) * 0.04f * spd;
        hip.y -= Mth.abs(Mth.cos(a)) * 0.8f * spd;

        // ---- หายใจ / หางส่าย ----
        neck.xRot += Mth.sin(age * 0.1f) * 0.02f;
        tail1.yRot += Mth.sin(age * 0.07f) * 0.06f + Mth.cos(a) * 0.18f * spd;
        tail2.yRot += Mth.sin(age * 0.07f - 0.6f) * 0.08f + Mth.cos(a - 0.6f) * 0.2f * spd;
        tail3.yRot += Mth.sin(age * 0.07f - 1.2f) * 0.10f + Mth.cos(a - 1.2f) * 0.22f * spd;

        // ---- มองตามผู้เล่น ----
        float yaw = s.yRot * Mth.DEG_TO_RAD, pitch = s.xRot * Mth.DEG_TO_RAD;
        neck.yRot += yaw * 0.4f;
        head.yRot += yaw * 0.6f;
        head.xRot += pitch;

        // ---- ก้าวร้าว: ปากอ้านิดๆ ----
        jaw.xRot += s.roaring ? 0.25f : 0.03f;

        // ---- ท่าคำราม: เงยหน้าสุด อ้าปากกว้าง ตัวสั่น ----
        neck.xRot -= roar * 0.45f;
        head.xRot -= roar * 0.55f;
        hip.xRot -= roar * 0.12f;
        jaw.xRot += roar * (1.0f + 0.12f * Mth.sin(age * 2.0f));
        hip.zRot += Mth.sin(age * 3.0f) * 0.03f * roar;
        for (int i = 0; i < 2; i++) arm[i].xRot += roar * 0.6f;

        // ---- ท่ากัด: ยื่นคอพุ่งลง ขากรรไกรงับ ----
        neck.xRot += bite * 0.5f;
        head.xRot += bite * 0.3f;
        jaw.xRot += bite * 0.9f;
        hip.xRot += bite * 0.08f;

        // ---- ท่านั่ง (เมื่อเชื่องแล้วถูกสั่งให้นั่ง) ----
        if (s.sitting) {
            hip.y += 9;
            hip.xRot += 0.2f;
            tail1.xRot += 0.15f;
            for (int i = 0; i < 2; i++) {
                thigh[i].xRot -= 0.8f;
                shin[i].xRot += 1.5f;
                meta[i].xRot -= 0.8f;
                foot[i].xRot += 0.1f;
            }
        }
    }
}

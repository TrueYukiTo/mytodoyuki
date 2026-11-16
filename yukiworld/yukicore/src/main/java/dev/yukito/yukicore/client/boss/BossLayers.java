package dev.yukito.yukicore.client.boss;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.HashMap;
import java.util.Map;

/** СГЕНЕРИРОВАНО tools/yukigen/models.py — не редактировать вручную. */
public final class BossLayers {
    private BossLayers() {}

    public static LayerDefinition frostColossus() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p0 = mesh.getRoot();
        PartDefinition p1 = p0.addOrReplaceChild("hips", CubeListBuilder.create().texOffs(48, 55).addBox(-8F, -4F, -5F, 16F, 6F, 10F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 6F, 0F, 0F, 0F, 0F));
        PartDefinition p2 = p1.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-12F, -20F, -7F, 24F, 20F, 14F, new CubeDeformation(0F)).texOffs(42, 74).addBox(-9F, -17F, -8F, 18F, 12F, 1F, new CubeDeformation(0F)).texOffs(156, 74).addBox(-3F, -14F, -9F, 6F, 6F, 2F, new CubeDeformation(0F)).texOffs(100, 55).addBox(-10F, -4F, -6F, 20F, 4F, 12F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -3F, 0F, 0F, 0F, 0F));
        PartDefinition p3 = p2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(182, 34).addBox(-5F, -9F, -5F, 10F, 9F, 10F, new CubeDeformation(0F)).texOffs(104, 74).addBox(-6F, -11F, -4F, 12F, 3F, 8F, new CubeDeformation(0F)).texOffs(188, 74).addBox(-4F, -6F, -5.5F, 3F, 2F, 1F, new CubeDeformation(0F)).texOffs(196, 74).addBox(1F, -6F, -5.5F, 3F, 2F, 1F, new CubeDeformation(0F)).texOffs(172, 74).addBox(-3F, -2F, -6F, 6F, 3F, 2F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -20F, -2F, 0F, 0F, 0F));
        PartDefinition p4 = p3.addOrReplaceChild("crest", CubeListBuilder.create().texOffs(144, 74).addBox(-1.5F, -8F, -1.5F, 3F, 8F, 3F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -11F, 0F, -0.3F, 0F, 0F));
        PartDefinition p5 = p2.addOrReplaceChild("arm_r", CubeListBuilder.create().texOffs(46, 34).addBox(-7F, -4F, -6F, 8F, 8F, 12F, new CubeDeformation(0F)).texOffs(86, 34).addBox(-6F, 4F, -4F, 6F, 12F, 8F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-13F, -17F, 0F, 0F, 0F, 0F));
        PartDefinition p6 = p5.addOrReplaceChild("spike_r", CubeListBuilder.create().texOffs(80, 74).addBox(-1.5F, -9F, -1.5F, 3F, 9F, 3F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-4F, -4F, 0F, 0F, 0F, -0.6F));
        PartDefinition p7 = p5.addOrReplaceChild("forearm_r", CubeListBuilder.create().texOffs(96, 0).addBox(-4F, 0F, -5F, 8F, 11F, 10F, new CubeDeformation(0F)).texOffs(132, 0).addBox(-5.5F, 10F, -6F, 11F, 9F, 12F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-3F, 16F, 0F, 0F, 0F, 0F));
        PartDefinition p8 = p2.addOrReplaceChild("arm_l", CubeListBuilder.create().texOffs(114, 34).addBox(-1F, -4F, -6F, 8F, 8F, 12F, new CubeDeformation(0F)).texOffs(154, 34).addBox(0F, 4F, -4F, 6F, 12F, 8F, new CubeDeformation(0F)), PartPose.offsetAndRotation(13F, -17F, 0F, 0F, 0F, 0F));
        PartDefinition p9 = p8.addOrReplaceChild("spike_l", CubeListBuilder.create().texOffs(92, 74).addBox(-1.5F, -9F, -1.5F, 3F, 9F, 3F, new CubeDeformation(0F)), PartPose.offsetAndRotation(4F, -4F, 0F, 0F, 0F, 0.6F));
        PartDefinition p10 = p8.addOrReplaceChild("forearm_l", CubeListBuilder.create().texOffs(178, 0).addBox(-4F, 0F, -5F, 8F, 11F, 10F, new CubeDeformation(0F)).texOffs(0, 34).addBox(-5.5F, 10F, -6F, 11F, 9F, 12F, new CubeDeformation(0F)), PartPose.offsetAndRotation(3F, 16F, 0F, 0F, 0F, 0F));
        PartDefinition p11 = p2.addOrReplaceChild("crystals", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -14F, 7F, 0F, 0F, 0F));
        PartDefinition p12 = p11.addOrReplaceChild("crystal_a", CubeListBuilder.create().texOffs(32, 55).addBox(-2F, -14F, -2F, 4F, 14F, 4F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-6F, 0F, 0F, -0.5F, 0F, -0.35F));
        PartDefinition p13 = p11.addOrReplaceChild("crystal_b", CubeListBuilder.create().texOffs(164, 55).addBox(-2F, -12F, -2F, 4F, 12F, 4F, new CubeDeformation(0F)), PartPose.offsetAndRotation(6F, 0F, 0F, -0.5F, 0F, 0.35F));
        PartDefinition p14 = p11.addOrReplaceChild("crystal_c", CubeListBuilder.create().texOffs(76, 0).addBox(-2.5F, -18F, -2.5F, 5F, 18F, 5F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -2F, 0F, -0.7F, 0F, 0F));
        PartDefinition p15 = p0.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(222, 34).addBox(-4F, 0F, -4F, 8F, 11F, 8F, new CubeDeformation(0F)).texOffs(180, 55).addBox(-5F, 11F, -6F, 10F, 5F, 11F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-5F, 8F, 0F, 0F, 0F, 0F));
        PartDefinition p16 = p0.addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(0, 55).addBox(-4F, 0F, -4F, 8F, 11F, 8F, new CubeDeformation(0F)).texOffs(0, 74).addBox(-5F, 11F, -6F, 10F, 5F, 11F, new CubeDeformation(0F)), PartPose.offsetAndRotation(5F, 8F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 256, 128);
    }

    public static LayerDefinition blizzardQueen() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p0 = mesh.getRoot();
        PartDefinition p1 = p0.addOrReplaceChild("body", CubeListBuilder.create().texOffs(30, 0).addBox(-5F, -13F, -3F, 10F, 13F, 6F, new CubeDeformation(0F)).texOffs(0, 48).addBox(-5.5F, -6F, -3.5F, 11F, 2F, 7F, new CubeDeformation(0F)).texOffs(144, 48).addBox(-2F, -11F, -3.5F, 4F, 3F, 1F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 6F, 0F, 0F, 0F, 0F));
        PartDefinition p2 = p1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(62, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0F)).texOffs(148, 33).addBox(-4.5F, -8.5F, -4.5F, 9F, 3F, 9F, new CubeDeformation(0F)).texOffs(94, 0).addBox(-4.5F, -6F, 2.5F, 9F, 14F, 2F, new CubeDeformation(0F)).texOffs(154, 48).addBox(-3F, -5F, -4.5F, 2F, 1F, 1F, new CubeDeformation(0F)).texOffs(160, 48).addBox(1F, -5F, -4.5F, 2F, 1F, 1F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -13F, 0F, 0F, 0F, 0F));
        PartDefinition p3 = p2.addOrReplaceChild("crown", CubeListBuilder.create().texOffs(184, 33).addBox(-4.5F, -2F, -4.5F, 9F, 2F, 9F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -8F, 0F, 0F, 0F, 0F));
        PartDefinition p4 = p3.addOrReplaceChild("crown_1", CubeListBuilder.create().texOffs(36, 48).addBox(-1F, -7F, -1F, 2F, 7F, 2F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -2F, -4F, -0.2F, 0F, 0F));
        PartDefinition p5 = p3.addOrReplaceChild("crown_2", CubeListBuilder.create().texOffs(80, 48).addBox(-1F, -5F, -1F, 2F, 5F, 2F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-3F, -2F, -3.5F, -0.15F, 0F, -0.3F));
        PartDefinition p6 = p3.addOrReplaceChild("crown_3", CubeListBuilder.create().texOffs(88, 48).addBox(-1F, -5F, -1F, 2F, 5F, 2F, new CubeDeformation(0F)), PartPose.offsetAndRotation(3F, -2F, -3.5F, -0.15F, 0F, 0.3F));
        PartDefinition p7 = p3.addOrReplaceChild("crown_4", CubeListBuilder.create().texOffs(96, 48).addBox(-1F, -4F, -1F, 2F, 4F, 2F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-4F, -2F, 1F, 0F, 0F, -0.5F));
        PartDefinition p8 = p3.addOrReplaceChild("crown_5", CubeListBuilder.create().texOffs(104, 48).addBox(-1F, -4F, -1F, 2F, 4F, 2F, new CubeDeformation(0F)), PartPose.offsetAndRotation(4F, -2F, 1F, 0F, 0F, 0.5F));
        PartDefinition p9 = p1.addOrReplaceChild("arm_r", CubeListBuilder.create().texOffs(220, 0).addBox(-3F, -1F, -2F, 3F, 11F, 4F, new CubeDeformation(0F)).texOffs(44, 48).addBox(-3.5F, 7F, -2.5F, 4F, 4F, 5F, new CubeDeformation(0F)).texOffs(112, 48).addBox(-2.5F, 11F, -1.5F, 2F, 2F, 3F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-6F, -11F, 0F, 0F, 0F, 0F));
        PartDefinition p10 = p9.addOrReplaceChild("staff", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -16F, -0.5F, 1F, 32F, 1F, new CubeDeformation(0F)).texOffs(220, 33).addBox(-2F, -22F, -2F, 4F, 6F, 4F, new CubeDeformation(0F)).texOffs(122, 48).addBox(-1.5F, -17F, -1.5F, 3F, 2F, 3F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-1.5F, 12F, -0.5F, 0F, 0F, 0F));
        PartDefinition p11 = p1.addOrReplaceChild("arm_l", CubeListBuilder.create().texOffs(234, 0).addBox(0F, -1F, -2F, 3F, 11F, 4F, new CubeDeformation(0F)).texOffs(62, 48).addBox(-0.5F, 7F, -2.5F, 4F, 4F, 5F, new CubeDeformation(0F)).texOffs(134, 48).addBox(0.5F, 11F, -1.5F, 2F, 2F, 3F, new CubeDeformation(0F)), PartPose.offsetAndRotation(6F, -11F, 0F, 0F, 0F, 0F));
        PartDefinition p12 = p1.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(4, 0).addBox(-6F, 0F, 0F, 12F, 26F, 1F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -12F, 3F, 0F, 0F, 0F));
        PartDefinition p13 = p1.addOrReplaceChild("skirt", CubeListBuilder.create().texOffs(60, 33).addBox(-6F, 0F, -4F, 12F, 6F, 8F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p14 = p13.addOrReplaceChild("skirt2", CubeListBuilder.create().texOffs(116, 0).addBox(-7F, 0F, -5F, 14F, 6F, 10F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 6F, 0F, 0F, 0F, 0F));
        PartDefinition p15 = p14.addOrReplaceChild("skirt3", CubeListBuilder.create().texOffs(164, 0).addBox(-8F, 0F, -6F, 16F, 4F, 12F, new CubeDeformation(0F)).texOffs(0, 33).addBox(-8.5F, 4F, -6.5F, 17F, 2F, 13F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 6F, 0F, 0F, 0F, 0F));
        PartDefinition p16 = p0.addOrReplaceChild("shard_0", CubeListBuilder.create().texOffs(100, 33).addBox(-1.5F, -5F, -1.5F, 3F, 10F, 3F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p17 = p0.addOrReplaceChild("shard_1", CubeListBuilder.create().texOffs(112, 33).addBox(-1.5F, -5F, -1.5F, 3F, 10F, 3F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p18 = p0.addOrReplaceChild("shard_2", CubeListBuilder.create().texOffs(124, 33).addBox(-1.5F, -5F, -1.5F, 3F, 10F, 3F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p19 = p0.addOrReplaceChild("shard_3", CubeListBuilder.create().texOffs(136, 33).addBox(-1.5F, -5F, -1.5F, 3F, 10F, 3F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 256, 64);
    }

    public static LayerDefinition ashLord() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p0 = mesh.getRoot();
        PartDefinition p1 = p0.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-9F, -18F, -6F, 18F, 18F, 12F, new CubeDeformation(0F)).texOffs(116, 48).addBox(-6F, -14F, -6.5F, 12F, 9F, 1F, new CubeDeformation(0F)).texOffs(160, 0).addBox(-10F, -2F, -7F, 20F, 4F, 14F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 4F, 0F, 0F, 0F, 0F));
        PartDefinition p2 = p1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(120, 0).addBox(-5F, -9F, -6F, 10F, 9F, 10F, new CubeDeformation(0F)).texOffs(70, 63).addBox(-4F, -6F, -6.5F, 3F, 2F, 1F, new CubeDeformation(0F)).texOffs(78, 63).addBox(1F, -6F, -6.5F, 3F, 2F, 1F, new CubeDeformation(0F)).texOffs(48, 63).addBox(-4F, -2F, -7F, 8F, 3F, 3F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -18F, -2F, 0F, 0F, 0F));
        PartDefinition p3 = p2.addOrReplaceChild("horn_r", CubeListBuilder.create().texOffs(142, 48).addBox(-1.5F, -7F, -1.5F, 3F, 7F, 3F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-4F, -8F, -1F, 0.3F, 0F, -0.6F));
        PartDefinition p4 = p3.addOrReplaceChild("horn_r2", CubeListBuilder.create().texOffs(166, 48).addBox(-1F, -6F, -1F, 2F, 6F, 2F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -7F, 0F, 0.5F, 0F, 0.4F));
        PartDefinition p5 = p2.addOrReplaceChild("horn_l", CubeListBuilder.create().texOffs(154, 48).addBox(-1.5F, -7F, -1.5F, 3F, 7F, 3F, new CubeDeformation(0F)), PartPose.offsetAndRotation(4F, -8F, -1F, 0.3F, 0F, 0.6F));
        PartDefinition p6 = p5.addOrReplaceChild("horn_l2", CubeListBuilder.create().texOffs(174, 48).addBox(-1F, -6F, -1F, 2F, 6F, 2F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -7F, 0F, 0.5F, 0F, -0.4F));
        PartDefinition p7 = p2.addOrReplaceChild("flame_crown", CubeListBuilder.create().texOffs(88, 48).addBox(-4F, -5F, -3F, 8F, 5F, 6F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -9F, 0F, 0F, 0F, 0F));
        PartDefinition p8 = p1.addOrReplaceChild("arm_r", CubeListBuilder.create().texOffs(152, 30).addBox(-6F, -3F, -4F, 7F, 7F, 8F, new CubeDeformation(0F)).texOffs(228, 0).addBox(-5F, 3F, -3F, 6F, 12F, 6F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-10F, -15F, 0F, 0F, 0F, 0F));
        PartDefinition p9 = p8.addOrReplaceChild("forearm_r", CubeListBuilder.create().texOffs(24, 30).addBox(-3.5F, 0F, -3.5F, 7F, 10F, 7F, new CubeDeformation(0F)).texOffs(80, 30).addBox(-4.5F, 9F, -4.5F, 9F, 7F, 9F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-2F, 15F, 0F, 0F, 0F, 0F));
        PartDefinition p10 = p1.addOrReplaceChild("arm_l", CubeListBuilder.create().texOffs(182, 30).addBox(-1F, -3F, -4F, 7F, 7F, 8F, new CubeDeformation(0F)).texOffs(0, 30).addBox(-1F, 3F, -3F, 6F, 12F, 6F, new CubeDeformation(0F)), PartPose.offsetAndRotation(10F, -15F, 0F, 0F, 0F, 0F));
        PartDefinition p11 = p10.addOrReplaceChild("forearm_l", CubeListBuilder.create().texOffs(52, 30).addBox(-3.5F, 0F, -3.5F, 7F, 10F, 7F, new CubeDeformation(0F)).texOffs(116, 30).addBox(-4.5F, 9F, -4.5F, 9F, 7F, 9F, new CubeDeformation(0F)), PartPose.offsetAndRotation(2F, 15F, 0F, 0F, 0F, 0F));
        PartDefinition p12 = p1.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(34, 48).addBox(-2F, 0F, 0F, 4F, 4F, 10F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -2F, 6F, 0.9F, 0F, 0F));
        PartDefinition p13 = p12.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(62, 48).addBox(-1.5F, 0.5F, 0F, 3F, 3F, 10F, new CubeDeformation(0F)).texOffs(182, 48).addBox(-2.5F, 0F, 9F, 5F, 4F, 4F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 10F, 0.3F, 0F, 0F));
        PartDefinition p14 = p0.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(60, 0).addBox(-4F, 0F, -4F, 7F, 12F, 8F, new CubeDeformation(0F)).texOffs(212, 30).addBox(-4.5F, 12F, -5F, 8F, 6F, 9F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-5F, 6F, 0F, 0F, 0F, 0F));
        PartDefinition p15 = p0.addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(90, 0).addBox(-3F, 0F, -4F, 7F, 12F, 8F, new CubeDeformation(0F)).texOffs(0, 48).addBox(-3.5F, 12F, -5F, 8F, 6F, 9F, new CubeDeformation(0F)), PartPose.offsetAndRotation(5F, 6F, 0F, 0F, 0F, 0F));
        PartDefinition p16 = p0.addOrReplaceChild("rock_0", CubeListBuilder.create().texOffs(200, 48).addBox(-2F, -2F, -2F, 4F, 4F, 4F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p17 = p0.addOrReplaceChild("rock_1", CubeListBuilder.create().texOffs(216, 48).addBox(-2F, -2F, -2F, 4F, 4F, 4F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p18 = p0.addOrReplaceChild("rock_2", CubeListBuilder.create().texOffs(232, 48).addBox(-2F, -2F, -2F, 4F, 4F, 4F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p19 = p0.addOrReplaceChild("rock_3", CubeListBuilder.create().texOffs(0, 63).addBox(-2F, -2F, -2F, 4F, 4F, 4F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p20 = p0.addOrReplaceChild("rock_4", CubeListBuilder.create().texOffs(16, 63).addBox(-2F, -2F, -2F, 4F, 4F, 4F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p21 = p0.addOrReplaceChild("rock_5", CubeListBuilder.create().texOffs(32, 63).addBox(-2F, -2F, -2F, 4F, 4F, 4F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 256, 128);
    }

    public static LayerDefinition voidWyrm() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p0 = mesh.getRoot();
        PartDefinition p1 = p0.addOrReplaceChild("head", CubeListBuilder.create().texOffs(78, 0).addBox(-5F, -5F, -10F, 10F, 8F, 10F, new CubeDeformation(0F)).texOffs(194, 20).addBox(-4F, -3F, -18F, 8F, 4F, 8F, new CubeDeformation(0F)).texOffs(76, 47).addBox(-5.5F, -4F, -8F, 1F, 2F, 3F, new CubeDeformation(0F)).texOffs(84, 47).addBox(4.5F, -4F, -8F, 1F, 2F, 3F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 12F, -12F, 0F, 0F, 0F));
        PartDefinition p2 = p1.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(144, 35).addBox(-4F, 0F, -8F, 8F, 2F, 8F, new CubeDeformation(0F)).texOffs(48, 47).addBox(-3.5F, -1F, -7.5F, 7F, 1F, 7F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 2F, -10F, 0F, 0F, 0F));
        PartDefinition p3 = p1.addOrReplaceChild("horn_r", CubeListBuilder.create().texOffs(226, 20).addBox(-1F, -1F, 0F, 2F, 2F, 10F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-4F, -5F, -3F, 0.7F, 0F, -0.3F));
        PartDefinition p4 = p1.addOrReplaceChild("horn_l", CubeListBuilder.create().texOffs(0, 35).addBox(-1F, -1F, 0F, 2F, 2F, 10F, new CubeDeformation(0F)), PartPose.offsetAndRotation(4F, -5F, -3F, 0.7F, 0F, 0.3F));
        PartDefinition p5 = p0.addOrReplaceChild("seg1", CubeListBuilder.create().texOffs(0, 0).addBox(-5F, -5F, 0F, 10F, 10F, 10F, new CubeDeformation(0F)).texOffs(176, 35).addBox(-0.5F, -9F, 2F, 1F, 4F, 6F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 12F, -12F, 0F, 0F, 0F));
        PartDefinition p6 = p5.addOrReplaceChild("wing_r", CubeListBuilder.create().texOffs(58, 20).addBox(-22F, -0.5F, -3F, 22F, 1F, 12F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-5F, -3F, 3F, 0F, 0F, 0F));
        PartDefinition p7 = p6.addOrReplaceChild("wing_r2", CubeListBuilder.create().texOffs(24, 35).addBox(-20F, -0.5F, -2F, 20F, 1F, 10F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-22F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p8 = p5.addOrReplaceChild("wing_l", CubeListBuilder.create().texOffs(126, 20).addBox(0F, -0.5F, -3F, 22F, 1F, 12F, new CubeDeformation(0F)), PartPose.offsetAndRotation(5F, -3F, 3F, 0F, 0F, 0F));
        PartDefinition p9 = p8.addOrReplaceChild("wing_l2", CubeListBuilder.create().texOffs(84, 35).addBox(0F, -0.5F, -2F, 20F, 1F, 10F, new CubeDeformation(0F)), PartPose.offsetAndRotation(22F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p10 = p5.addOrReplaceChild("seg2", CubeListBuilder.create().texOffs(40, 0).addBox(-4.5F, -4.5F, 0F, 9F, 9F, 10F, new CubeDeformation(0F)).texOffs(190, 35).addBox(-0.5F, -8.5F, 2F, 1F, 4F, 6F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 10F, 0F, 0F, 0F));
        PartDefinition p11 = p10.addOrReplaceChild("seg3", CubeListBuilder.create().texOffs(118, 0).addBox(-4F, -4F, 0F, 8F, 8F, 10F, new CubeDeformation(0F)).texOffs(204, 35).addBox(-0.5F, -8F, 2F, 1F, 4F, 6F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 10F, 0F, 0F, 0F));
        PartDefinition p12 = p11.addOrReplaceChild("seg4", CubeListBuilder.create().texOffs(176, 0).addBox(-3.5F, -3.5F, 0F, 7F, 7F, 10F, new CubeDeformation(0F)).texOffs(218, 35).addBox(-0.5F, -7.5F, 2F, 1F, 4F, 6F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 10F, 0F, 0F, 0F));
        PartDefinition p13 = p12.addOrReplaceChild("seg5", CubeListBuilder.create().texOffs(210, 0).addBox(-3F, -3F, 0F, 6F, 6F, 10F, new CubeDeformation(0F)).texOffs(232, 35).addBox(-0.5F, -7F, 2F, 1F, 4F, 6F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 10F, 0F, 0F, 0F));
        PartDefinition p14 = p13.addOrReplaceChild("seg6", CubeListBuilder.create().texOffs(0, 20).addBox(-2.5F, -2.5F, 0F, 5F, 5F, 10F, new CubeDeformation(0F)).texOffs(0, 47).addBox(-0.5F, -6.5F, 2F, 1F, 4F, 6F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 10F, 0F, 0F, 0F));
        PartDefinition p15 = p14.addOrReplaceChild("seg7", CubeListBuilder.create().texOffs(30, 20).addBox(-2F, -2F, 0F, 4F, 4F, 10F, new CubeDeformation(0F)).texOffs(14, 47).addBox(-0.5F, -6F, 2F, 1F, 4F, 6F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 10F, 0F, 0F, 0F));
        PartDefinition p16 = p15.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(154, 0).addBox(-0.5F, -4F, 0F, 1F, 8F, 10F, new CubeDeformation(0F)).texOffs(28, 47).addBox(-2F, -2F, 0F, 4F, 4F, 6F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 10F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 256, 64);
    }

    public static LayerDefinition shadowOfYuki() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p0 = mesh.getRoot();
        PartDefinition p1 = p0.addOrReplaceChild("body", CubeListBuilder.create().texOffs(80, 0).addBox(-4F, -12F, -2F, 8F, 12F, 4F, new CubeDeformation(0F)).texOffs(80, 25).addBox(-4.5F, -12F, -2.5F, 9F, 3F, 5F, new CubeDeformation(0F)).texOffs(156, 25).addBox(-1F, -9F, -2.5F, 2F, 2F, 1F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 4F, 0F, 0F, 0F, 0F));
        PartDefinition p2 = p1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(104, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0F)).texOffs(0, 25).addBox(-4.5F, -8.5F, -4.5F, 9F, 4F, 9F, new CubeDeformation(0F)).texOffs(58, 0).addBox(-4.5F, -5F, 2.5F, 9F, 15F, 2F, new CubeDeformation(0F)).texOffs(218, 25).addBox(-3F, -4F, -4.5F, 2F, 1F, 1F, new CubeDeformation(0F)).texOffs(224, 25).addBox(1F, -4F, -4.5F, 2F, 1F, 1F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -12F, 0F, 0F, 0F, 0F));
        PartDefinition p3 = p2.addOrReplaceChild("halo", CubeListBuilder.create().texOffs(230, 25).addBox(-6F, -7F, 0F, 12F, 1F, 1F, new CubeDeformation(0F)).texOffs(0, 38).addBox(-6F, 6F, 0F, 12F, 1F, 1F, new CubeDeformation(0F)).texOffs(168, 0).addBox(-7F, -7F, 0F, 1F, 14F, 1F, new CubeDeformation(0F)).texOffs(172, 0).addBox(6F, -7F, 0F, 1F, 14F, 1F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -6F, 5F, 0F, 0F, 0F));
        PartDefinition p4 = p1.addOrReplaceChild("arm_r", CubeListBuilder.create().texOffs(176, 0).addBox(-3F, -1F, -1.5F, 3F, 12F, 3F, new CubeDeformation(0F)).texOffs(108, 25).addBox(-3.5F, 8F, -2F, 4F, 2F, 4F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-5F, -11F, 0F, 0F, 0F, 0F));
        PartDefinition p5 = p1.addOrReplaceChild("arm_l", CubeListBuilder.create().texOffs(188, 0).addBox(0F, -1F, -1.5F, 3F, 12F, 3F, new CubeDeformation(0F)).texOffs(124, 25).addBox(-0.5F, 8F, -2F, 4F, 2F, 4F, new CubeDeformation(0F)), PartPose.offsetAndRotation(5F, -11F, 0F, 0F, 0F, 0F));
        PartDefinition p6 = p1.addOrReplaceChild("cloak", CubeListBuilder.create().texOffs(0, 0).addBox(-6F, 0F, 0F, 12F, 24F, 1F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -12F, 2.5F, 0F, 0F, 0F));
        PartDefinition p7 = p1.addOrReplaceChild("robe", CubeListBuilder.create().texOffs(136, 0).addBox(-5F, 0F, -3F, 10F, 10F, 6F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p8 = p7.addOrReplaceChild("robe2", CubeListBuilder.create().texOffs(200, 0).addBox(-6F, 0F, -4F, 12F, 6F, 8F, new CubeDeformation(0F)).texOffs(36, 25).addBox(-6.5F, 6F, -4.5F, 13F, 2F, 9F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 10F, 0F, 0F, 0F, 0F));
        PartDefinition p9 = p0.addOrReplaceChild("sword_0", CubeListBuilder.create().texOffs(26, 0).addBox(-0.5F, -12F, -1.5F, 1F, 16F, 3F, new CubeDeformation(0F)).texOffs(162, 25).addBox(-2.5F, 4F, -1F, 5F, 1F, 2F, new CubeDeformation(0F)).texOffs(140, 25).addBox(-0.5F, 5F, -0.5F, 1F, 4F, 1F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p10 = p0.addOrReplaceChild("sword_1", CubeListBuilder.create().texOffs(34, 0).addBox(-0.5F, -12F, -1.5F, 1F, 16F, 3F, new CubeDeformation(0F)).texOffs(176, 25).addBox(-2.5F, 4F, -1F, 5F, 1F, 2F, new CubeDeformation(0F)).texOffs(144, 25).addBox(-0.5F, 5F, -0.5F, 1F, 4F, 1F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p11 = p0.addOrReplaceChild("sword_2", CubeListBuilder.create().texOffs(42, 0).addBox(-0.5F, -12F, -1.5F, 1F, 16F, 3F, new CubeDeformation(0F)).texOffs(190, 25).addBox(-2.5F, 4F, -1F, 5F, 1F, 2F, new CubeDeformation(0F)).texOffs(148, 25).addBox(-0.5F, 5F, -0.5F, 1F, 4F, 1F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p12 = p0.addOrReplaceChild("sword_3", CubeListBuilder.create().texOffs(50, 0).addBox(-0.5F, -12F, -1.5F, 1F, 16F, 3F, new CubeDeformation(0F)).texOffs(204, 25).addBox(-2.5F, 4F, -1F, 5F, 1F, 2F, new CubeDeformation(0F)).texOffs(152, 25).addBox(-0.5F, 5F, -0.5F, 1F, 4F, 1F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 256, 64);
    }

    public static LayerDefinition glacialArchon() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p0 = mesh.getRoot();
        PartDefinition p1 = p0.addOrReplaceChild("body", CubeListBuilder.create().texOffs(128, 0).addBox(-7F, -18F, -4F, 14F, 18F, 8F, new CubeDeformation(0F)).texOffs(64, 52).addBox(-5F, -15F, -4.5F, 10F, 8F, 1F, new CubeDeformation(0F)).texOffs(122, 52).addBox(-2F, -13F, -5F, 4F, 4F, 1F, new CubeDeformation(0F)).texOffs(0, 52).addBox(-7.5F, -3F, -4.5F, 15F, 3F, 9F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 6F, 0F, 0F, 0F, 0F));
        PartDefinition p2 = p1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(172, 0).addBox(-4.5F, -10F, -4.5F, 9F, 10F, 9F, new CubeDeformation(0F)).texOffs(132, 52).addBox(-3.5F, -6F, -5F, 7F, 1F, 1F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -18F, 0F, 0F, 0F, 0F));
        PartDefinition p3 = p2.addOrReplaceChild("plume", CubeListBuilder.create().texOffs(130, 34).addBox(-0.5F, -4F, -4F, 1F, 4F, 10F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -10F, 0F, 0F, 0F, 0F));
        PartDefinition p4 = p2.addOrReplaceChild("ahorn_r", CubeListBuilder.create().texOffs(48, 52).addBox(-1F, -8F, -1F, 2F, 8F, 2F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-4.5F, -8F, 0F, 0F, 0F, -0.9F));
        PartDefinition p5 = p2.addOrReplaceChild("ahorn_l", CubeListBuilder.create().texOffs(56, 52).addBox(-1F, -8F, -1F, 2F, 8F, 2F, new CubeDeformation(0F)), PartPose.offsetAndRotation(4.5F, -8F, 0F, 0F, 0F, 0.9F));
        PartDefinition p6 = p1.addOrReplaceChild("arm_r", CubeListBuilder.create().texOffs(48, 34).addBox(-5F, -4F, -4.5F, 8F, 7F, 9F, new CubeDeformation(0F)).texOffs(208, 0).addBox(-4F, 3F, -2.5F, 5F, 14F, 5F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-9F, -16F, 0F, 0F, 0F, 0F));
        PartDefinition p7 = p6.addOrReplaceChild("sword", CubeListBuilder.create().texOffs(86, 52).addBox(-1F, -1F, -3F, 2F, 2F, 6F, new CubeDeformation(0F)).texOffs(102, 52).addBox(-4F, -2F, -5F, 8F, 4F, 2F, new CubeDeformation(0F)).texOffs(36, 0).addBox(-1.5F, -1F, -33F, 3F, 2F, 28F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-1.5F, 16F, 0F, 0F, 0F, 0F));
        PartDefinition p8 = p1.addOrReplaceChild("arm_l", CubeListBuilder.create().texOffs(82, 34).addBox(-3F, -4F, -4.5F, 8F, 7F, 9F, new CubeDeformation(0F)).texOffs(228, 0).addBox(-1F, 3F, -2.5F, 5F, 14F, 5F, new CubeDeformation(0F)), PartPose.offsetAndRotation(9F, -16F, 0F, 0F, 0F, 0F));
        PartDefinition p9 = p8.addOrReplaceChild("shield", CubeListBuilder.create().texOffs(0, 0).addBox(0F, -8F, -8F, 2F, 18F, 16F, new CubeDeformation(0F)).texOffs(116, 34).addBox(1.5F, -5F, -3F, 1F, 10F, 6F, new CubeDeformation(0F)), PartPose.offsetAndRotation(5F, 10F, 0F, 0F, 0F, 0F));
        PartDefinition p10 = p1.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(98, 0).addBox(-7F, 0F, 0F, 14F, 26F, 1F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -17F, 4F, 0F, 0F, 0F));
        PartDefinition p11 = p0.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(0, 34).addBox(-3F, 0F, -3F, 6F, 12F, 6F, new CubeDeformation(0F)).texOffs(152, 34).addBox(-3.5F, 12F, -4F, 7F, 6F, 8F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-3.5F, 6F, 0F, 0F, 0F, 0F));
        PartDefinition p12 = p0.addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(24, 34).addBox(-3F, 0F, -3F, 6F, 12F, 6F, new CubeDeformation(0F)).texOffs(182, 34).addBox(-3.5F, 12F, -4F, 7F, 6F, 8F, new CubeDeformation(0F)), PartPose.offsetAndRotation(3.5F, 6F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 256, 64);
    }

    public static LayerDefinition infernalPhoenix() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p0 = mesh.getRoot();
        PartDefinition p1 = p0.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5F, -5F, -8F, 10F, 10F, 16F, new CubeDeformation(0F)).texOffs(222, 26).addBox(-4F, -3F, -9F, 8F, 8F, 6F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 10F, 0F, 0F, 0F, 0F));
        PartDefinition p2 = p1.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 43).addBox(-2.5F, -9F, -2.5F, 5F, 9F, 5F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -3F, -8F, 0.4F, 0F, 0F));
        PartDefinition p3 = p2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(20, 43).addBox(-3.5F, -6F, -5F, 7F, 6F, 8F, new CubeDeformation(0F)).texOffs(90, 43).addBox(-1.5F, -4F, -10F, 3F, 3F, 5F, new CubeDeformation(0F)).texOffs(122, 43).addBox(-4F, -4F, -3F, 1F, 1F, 2F, new CubeDeformation(0F)).texOffs(128, 43).addBox(3F, -4F, -3F, 1F, 1F, 2F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -9F, 0F, -0.4F, 0F, 0F));
        PartDefinition p4 = p3.addOrReplaceChild("crest", CubeListBuilder.create().texOffs(68, 26).addBox(-0.5F, -2F, 0F, 1F, 4F, 12F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -6F, 0F, -0.6F, 0F, 0F));
        PartDefinition p5 = p3.addOrReplaceChild("crest2", CubeListBuilder.create().texOffs(50, 43).addBox(-0.5F, -2F, 0F, 1F, 3F, 9F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -5F, 0F, -0.3F, 0.3F, 0F));
        PartDefinition p6 = p3.addOrReplaceChild("crest3", CubeListBuilder.create().texOffs(70, 43).addBox(-0.5F, -2F, 0F, 1F, 3F, 9F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -5F, 0F, -0.3F, -0.3F, 0F));
        PartDefinition p7 = p1.addOrReplaceChild("wing_r", CubeListBuilder.create().texOffs(94, 26).addBox(-18F, -1F, -3F, 18F, 2F, 14F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-5F, -3F, -3F, 0F, 0F, 0F));
        PartDefinition p8 = p7.addOrReplaceChild("wing_r2", CubeListBuilder.create().texOffs(182, 0).addBox(-18F, -0.5F, -2F, 18F, 1F, 16F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-18F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p9 = p1.addOrReplaceChild("wing_l", CubeListBuilder.create().texOffs(158, 26).addBox(0F, -1F, -3F, 18F, 2F, 14F, new CubeDeformation(0F)), PartPose.offsetAndRotation(5F, -3F, -3F, 0F, 0F, 0F));
        PartDefinition p10 = p9.addOrReplaceChild("wing_l2", CubeListBuilder.create().texOffs(0, 26).addBox(0F, -0.5F, -2F, 18F, 1F, 16F, new CubeDeformation(0F)), PartPose.offsetAndRotation(18F, 0F, 0F, 0F, 0F, 0F));
        PartDefinition p11 = p1.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(52, 0).addBox(-1.5F, 0F, 0F, 3F, 1F, 20F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -1F, 8F, -0.3F, 0F, 0F));
        PartDefinition p12 = p11.addOrReplaceChild("tail_r", CubeListBuilder.create().texOffs(98, 0).addBox(-1.5F, 0F, 0F, 3F, 1F, 18F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.35F, 0F));
        PartDefinition p13 = p11.addOrReplaceChild("tail_l", CubeListBuilder.create().texOffs(140, 0).addBox(-1.5F, 0F, 0F, 3F, 1F, 18F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.35F, 0F));
        PartDefinition p14 = p1.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(106, 43).addBox(-1F, 0F, -1F, 2F, 6F, 2F, new CubeDeformation(0F)), PartPose.offsetAndRotation(-2.5F, 5F, 0F, 0F, 0F, 0F));
        PartDefinition p15 = p1.addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(114, 43).addBox(-1F, 0F, -1F, 2F, 6F, 2F, new CubeDeformation(0F)), PartPose.offsetAndRotation(2.5F, 5F, 0F, 0F, 0F, 0F));
        return LayerDefinition.create(mesh, 256, 64);
    }

    /** Пути к частям моделей: имя части → цепочка имён от корня. */
    public static final Map<String, Map<String, String[]>> PATHS = new HashMap<>();

    static {
        Map<String, String[]> frost_colossus = new HashMap<>();
        frost_colossus.put("hips", new String[]{"hips"});
        frost_colossus.put("torso", new String[]{"hips", "torso"});
        frost_colossus.put("head", new String[]{"hips", "torso", "head"});
        frost_colossus.put("crest", new String[]{"hips", "torso", "head", "crest"});
        frost_colossus.put("arm_r", new String[]{"hips", "torso", "arm_r"});
        frost_colossus.put("spike_r", new String[]{"hips", "torso", "arm_r", "spike_r"});
        frost_colossus.put("forearm_r", new String[]{"hips", "torso", "arm_r", "forearm_r"});
        frost_colossus.put("arm_l", new String[]{"hips", "torso", "arm_l"});
        frost_colossus.put("spike_l", new String[]{"hips", "torso", "arm_l", "spike_l"});
        frost_colossus.put("forearm_l", new String[]{"hips", "torso", "arm_l", "forearm_l"});
        frost_colossus.put("crystals", new String[]{"hips", "torso", "crystals"});
        frost_colossus.put("crystal_a", new String[]{"hips", "torso", "crystals", "crystal_a"});
        frost_colossus.put("crystal_b", new String[]{"hips", "torso", "crystals", "crystal_b"});
        frost_colossus.put("crystal_c", new String[]{"hips", "torso", "crystals", "crystal_c"});
        frost_colossus.put("leg_r", new String[]{"leg_r"});
        frost_colossus.put("leg_l", new String[]{"leg_l"});
        PATHS.put("frost_colossus", frost_colossus);
        Map<String, String[]> blizzard_queen = new HashMap<>();
        blizzard_queen.put("body", new String[]{"body"});
        blizzard_queen.put("head", new String[]{"body", "head"});
        blizzard_queen.put("crown", new String[]{"body", "head", "crown"});
        blizzard_queen.put("crown_1", new String[]{"body", "head", "crown", "crown_1"});
        blizzard_queen.put("crown_2", new String[]{"body", "head", "crown", "crown_2"});
        blizzard_queen.put("crown_3", new String[]{"body", "head", "crown", "crown_3"});
        blizzard_queen.put("crown_4", new String[]{"body", "head", "crown", "crown_4"});
        blizzard_queen.put("crown_5", new String[]{"body", "head", "crown", "crown_5"});
        blizzard_queen.put("arm_r", new String[]{"body", "arm_r"});
        blizzard_queen.put("staff", new String[]{"body", "arm_r", "staff"});
        blizzard_queen.put("arm_l", new String[]{"body", "arm_l"});
        blizzard_queen.put("cape", new String[]{"body", "cape"});
        blizzard_queen.put("skirt", new String[]{"body", "skirt"});
        blizzard_queen.put("skirt2", new String[]{"body", "skirt", "skirt2"});
        blizzard_queen.put("skirt3", new String[]{"body", "skirt", "skirt2", "skirt3"});
        blizzard_queen.put("shard_0", new String[]{"shard_0"});
        blizzard_queen.put("shard_1", new String[]{"shard_1"});
        blizzard_queen.put("shard_2", new String[]{"shard_2"});
        blizzard_queen.put("shard_3", new String[]{"shard_3"});
        PATHS.put("blizzard_queen", blizzard_queen);
        Map<String, String[]> ash_lord = new HashMap<>();
        ash_lord.put("body", new String[]{"body"});
        ash_lord.put("head", new String[]{"body", "head"});
        ash_lord.put("horn_r", new String[]{"body", "head", "horn_r"});
        ash_lord.put("horn_r2", new String[]{"body", "head", "horn_r", "horn_r2"});
        ash_lord.put("horn_l", new String[]{"body", "head", "horn_l"});
        ash_lord.put("horn_l2", new String[]{"body", "head", "horn_l", "horn_l2"});
        ash_lord.put("flame_crown", new String[]{"body", "head", "flame_crown"});
        ash_lord.put("arm_r", new String[]{"body", "arm_r"});
        ash_lord.put("forearm_r", new String[]{"body", "arm_r", "forearm_r"});
        ash_lord.put("arm_l", new String[]{"body", "arm_l"});
        ash_lord.put("forearm_l", new String[]{"body", "arm_l", "forearm_l"});
        ash_lord.put("tail", new String[]{"body", "tail"});
        ash_lord.put("tail2", new String[]{"body", "tail", "tail2"});
        ash_lord.put("leg_r", new String[]{"leg_r"});
        ash_lord.put("leg_l", new String[]{"leg_l"});
        ash_lord.put("rock_0", new String[]{"rock_0"});
        ash_lord.put("rock_1", new String[]{"rock_1"});
        ash_lord.put("rock_2", new String[]{"rock_2"});
        ash_lord.put("rock_3", new String[]{"rock_3"});
        ash_lord.put("rock_4", new String[]{"rock_4"});
        ash_lord.put("rock_5", new String[]{"rock_5"});
        PATHS.put("ash_lord", ash_lord);
        Map<String, String[]> void_wyrm = new HashMap<>();
        void_wyrm.put("head", new String[]{"head"});
        void_wyrm.put("jaw", new String[]{"head", "jaw"});
        void_wyrm.put("horn_r", new String[]{"head", "horn_r"});
        void_wyrm.put("horn_l", new String[]{"head", "horn_l"});
        void_wyrm.put("seg1", new String[]{"seg1"});
        void_wyrm.put("wing_r", new String[]{"seg1", "wing_r"});
        void_wyrm.put("wing_r2", new String[]{"seg1", "wing_r", "wing_r2"});
        void_wyrm.put("wing_l", new String[]{"seg1", "wing_l"});
        void_wyrm.put("wing_l2", new String[]{"seg1", "wing_l", "wing_l2"});
        void_wyrm.put("seg2", new String[]{"seg1", "seg2"});
        void_wyrm.put("seg3", new String[]{"seg1", "seg2", "seg3"});
        void_wyrm.put("seg4", new String[]{"seg1", "seg2", "seg3", "seg4"});
        void_wyrm.put("seg5", new String[]{"seg1", "seg2", "seg3", "seg4", "seg5"});
        void_wyrm.put("seg6", new String[]{"seg1", "seg2", "seg3", "seg4", "seg5", "seg6"});
        void_wyrm.put("seg7", new String[]{"seg1", "seg2", "seg3", "seg4", "seg5", "seg6", "seg7"});
        void_wyrm.put("tail", new String[]{"seg1", "seg2", "seg3", "seg4", "seg5", "seg6", "seg7", "tail"});
        PATHS.put("void_wyrm", void_wyrm);
        Map<String, String[]> shadow_of_yuki = new HashMap<>();
        shadow_of_yuki.put("body", new String[]{"body"});
        shadow_of_yuki.put("head", new String[]{"body", "head"});
        shadow_of_yuki.put("halo", new String[]{"body", "head", "halo"});
        shadow_of_yuki.put("arm_r", new String[]{"body", "arm_r"});
        shadow_of_yuki.put("arm_l", new String[]{"body", "arm_l"});
        shadow_of_yuki.put("cloak", new String[]{"body", "cloak"});
        shadow_of_yuki.put("robe", new String[]{"body", "robe"});
        shadow_of_yuki.put("robe2", new String[]{"body", "robe", "robe2"});
        shadow_of_yuki.put("sword_0", new String[]{"sword_0"});
        shadow_of_yuki.put("sword_1", new String[]{"sword_1"});
        shadow_of_yuki.put("sword_2", new String[]{"sword_2"});
        shadow_of_yuki.put("sword_3", new String[]{"sword_3"});
        PATHS.put("shadow_of_yuki", shadow_of_yuki);
        Map<String, String[]> glacial_archon = new HashMap<>();
        glacial_archon.put("body", new String[]{"body"});
        glacial_archon.put("head", new String[]{"body", "head"});
        glacial_archon.put("plume", new String[]{"body", "head", "plume"});
        glacial_archon.put("ahorn_r", new String[]{"body", "head", "ahorn_r"});
        glacial_archon.put("ahorn_l", new String[]{"body", "head", "ahorn_l"});
        glacial_archon.put("arm_r", new String[]{"body", "arm_r"});
        glacial_archon.put("sword", new String[]{"body", "arm_r", "sword"});
        glacial_archon.put("arm_l", new String[]{"body", "arm_l"});
        glacial_archon.put("shield", new String[]{"body", "arm_l", "shield"});
        glacial_archon.put("cape", new String[]{"body", "cape"});
        glacial_archon.put("leg_r", new String[]{"leg_r"});
        glacial_archon.put("leg_l", new String[]{"leg_l"});
        PATHS.put("glacial_archon", glacial_archon);
        Map<String, String[]> infernal_phoenix = new HashMap<>();
        infernal_phoenix.put("body", new String[]{"body"});
        infernal_phoenix.put("neck", new String[]{"body", "neck"});
        infernal_phoenix.put("head", new String[]{"body", "neck", "head"});
        infernal_phoenix.put("crest", new String[]{"body", "neck", "head", "crest"});
        infernal_phoenix.put("crest2", new String[]{"body", "neck", "head", "crest2"});
        infernal_phoenix.put("crest3", new String[]{"body", "neck", "head", "crest3"});
        infernal_phoenix.put("wing_r", new String[]{"body", "wing_r"});
        infernal_phoenix.put("wing_r2", new String[]{"body", "wing_r", "wing_r2"});
        infernal_phoenix.put("wing_l", new String[]{"body", "wing_l"});
        infernal_phoenix.put("wing_l2", new String[]{"body", "wing_l", "wing_l2"});
        infernal_phoenix.put("tail", new String[]{"body", "tail"});
        infernal_phoenix.put("tail_r", new String[]{"body", "tail", "tail_r"});
        infernal_phoenix.put("tail_l", new String[]{"body", "tail", "tail_l"});
        infernal_phoenix.put("leg_r", new String[]{"body", "leg_r"});
        infernal_phoenix.put("leg_l", new String[]{"body", "leg_l"});
        PATHS.put("infernal_phoenix", infernal_phoenix);
    }
}

package com.darkcraft.client.armadura;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** GENERADO por tools/armaduras/armaduras.py: no tocar a mano. La geometria de cada pieza de cada conjunto de armadura. */
public final class ArmaduraGeometria {
    private ArmaduraGeometria() {}

    /** Los conjuntos que tienen geometria (en el orden de la herramienta). */
    public static final String[] CONJUNTOS = {"caballero", "confesor", "vagabundo", "guerrero", "barbaro", "bandido", "hechicero", "profeta", "prisionero"};

    /** La geometria de la pieza (0 yelmo, 1 peto, 2 guantes, 3 grebas) del conjunto, o null si no hay. */
    public static LayerDefinition crear(String conjunto, int pieza) {
        return switch (conjunto + ":" + pieza) {
            case "caballero:0" -> caballero_yelmo();
            case "caballero:1" -> caballero_peto();
            case "caballero:2" -> caballero_guantes();
            case "caballero:3" -> caballero_grebas();
            case "confesor:0" -> confesor_yelmo();
            case "confesor:1" -> confesor_peto();
            case "confesor:2" -> confesor_guantes();
            case "confesor:3" -> confesor_grebas();
            case "vagabundo:0" -> vagabundo_yelmo();
            case "vagabundo:1" -> vagabundo_peto();
            case "vagabundo:2" -> vagabundo_guantes();
            case "vagabundo:3" -> vagabundo_grebas();
            case "guerrero:0" -> guerrero_yelmo();
            case "guerrero:1" -> guerrero_peto();
            case "guerrero:2" -> guerrero_guantes();
            case "guerrero:3" -> guerrero_grebas();
            case "barbaro:0" -> barbaro_yelmo();
            case "barbaro:1" -> barbaro_peto();
            case "barbaro:2" -> barbaro_guantes();
            case "barbaro:3" -> barbaro_grebas();
            case "bandido:0" -> bandido_yelmo();
            case "bandido:1" -> bandido_peto();
            case "bandido:2" -> bandido_guantes();
            case "bandido:3" -> bandido_grebas();
            case "hechicero:0" -> hechicero_yelmo();
            case "hechicero:1" -> hechicero_peto();
            case "hechicero:2" -> hechicero_guantes();
            case "hechicero:3" -> hechicero_grebas();
            case "profeta:0" -> profeta_yelmo();
            case "profeta:1" -> profeta_peto();
            case "profeta:2" -> profeta_guantes();
            case "profeta:3" -> profeta_grebas();
            case "prisionero:0" -> prisionero_yelmo();
            case "prisionero:1" -> prisionero_peto();
            case "prisionero:2" -> prisionero_guantes();
            case "prisionero:3" -> prisionero_grebas();
            default -> null;
        };
    }

    private static LayerDefinition caballero_yelmo() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.7F, 0.7F, 0.7F)).texOffs(94, 16).addBox(-4F, -1F, -4F, 8F, 1F, 8F, new CubeDeformation(1.05F, 1.05F, 1.05F)).texOffs(88, 0).addBox(-1F, -11F, -3F, 2F, 3F, 7F, new CubeDeformation(0F, 0F, 0F)).texOffs(104, 26).addBox(-1F, -10F, 4F, 2F, 5F, 1F, new CubeDeformation(0F, 0F, 0F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition caballero_peto() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-4F, 0F, -2F, 8F, 12F, 4F, new CubeDeformation(0.6F, 0.6F, 0.6F)).texOffs(80, 16).addBox(-3F, 1F, -3F, 6F, 9F, 1F, new CubeDeformation(0.05F, 0.05F, 0F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 26).addBox(-3.6F, -3F, -2.5F, 5F, 3F, 5F, new CubeDeformation(0.25F, 0.25F, 0.25F)).texOffs(64, 26).addBox(-3.6F, 0F, -2.5F, 5F, 2F, 5F, new CubeDeformation(0.05F, 0.05F, 0.05F)).texOffs(56, 0).addBox(-3F, -2F, -2F, 4F, 7F, 4F, new CubeDeformation(0.45F, 0.45F, 0.45F)), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(20, 26).addBox(-1.4F, -3F, -2.5F, 5F, 3F, 5F, new CubeDeformation(0.25F, 0.25F, 0.25F)).texOffs(84, 26).addBox(-1.4F, 0F, -2.5F, 5F, 2F, 5F, new CubeDeformation(0.05F, 0.05F, 0.05F)).texOffs(72, 0).addBox(-1F, -2F, -2F, 4F, 7F, 4F, new CubeDeformation(0.45F, 0.45F, 0.45F)), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition caballero_guantes() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(106, 0).addBox(-3F, 4F, -2F, 4F, 6F, 4F, new CubeDeformation(0.62F, 0.62F, 0.62F)).texOffs(110, 26).addBox(-3F, 4F, -2F, 4F, 1F, 4F, new CubeDeformation(0.95F, 0.95F, 0.95F)).texOffs(28, 34).addBox(0F, 7F, -3F, 1F, 2F, 1F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-1F, 4F, -2F, 4F, 6F, 4F, new CubeDeformation(0.62F, 0.62F, 0.62F)).texOffs(0, 34).addBox(-1F, 4F, -2F, 4F, 1F, 4F, new CubeDeformation(0.95F, 0.95F, 0.95F)).texOffs(32, 34).addBox(-1F, 7F, -3F, 1F, 2F, 1F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition caballero_grebas() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(40, 26).addBox(-4F, 10F, -2F, 8F, 3F, 4F, new CubeDeformation(0.82F, 0.82F, 0.82F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(16, 16).addBox(-2F, 0F, -2F, 4F, 6F, 4F, new CubeDeformation(0.5F, 0.5F, 0.5F)).texOffs(16, 34).addBox(-1F, 5F, -3F, 2F, 2F, 1F, new CubeDeformation(0.25F, 0.25F, 0.25F)).texOffs(32, 16).addBox(-2F, 6F, -2F, 4F, 6F, 4F, new CubeDeformation(0.56F, 0.56F, 0.56F)).texOffs(36, 34).addBox(-2F, 11F, -3F, 4F, 1F, 1F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(48, 16).addBox(-2F, 0F, -2F, 4F, 6F, 4F, new CubeDeformation(0.5F, 0.5F, 0.5F)).texOffs(22, 34).addBox(-1F, 5F, -3F, 2F, 2F, 1F, new CubeDeformation(0.25F, 0.25F, 0.25F)).texOffs(64, 16).addBox(-2F, 6F, -2F, 4F, 6F, 4F, new CubeDeformation(0.56F, 0.56F, 0.56F)).texOffs(46, 34).addBox(-2F, 11F, -3F, 4F, 1F, 1F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition confesor_yelmo() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.7F, 0.7F, 0.7F)).texOffs(0, 26).addBox(-4F, -1F, -4F, 8F, 1F, 8F, new CubeDeformation(1.05F, 1.05F, 1.05F)).texOffs(32, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(1F, 1F, 1F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(72, 26).addBox(-4F, -1F, -2F, 8F, 3F, 4F, new CubeDeformation(1.1F, 1.1F, 1.1F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        PartDefinition p_capucha_pico = p_head.addOrReplaceChild("capucha_pico", CubeListBuilder.create().texOffs(40, 35).addBox(-2F, 0F, 0F, 4F, 5F, 1F, new CubeDeformation(0F, 0F, 0F)), PartPose.offsetAndRotation(0F, -7.5F, 4.6F, 0.4887F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition confesor_peto() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(64, 0).addBox(-4F, 0F, -2F, 8F, 12F, 4F, new CubeDeformation(0.6F, 0.6F, 0.6F)).texOffs(96, 16).addBox(-3F, 1F, -3F, 6F, 9F, 1F, new CubeDeformation(0.05F, 0.05F, 0F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 26).addBox(-3.6F, -3F, -2.5F, 5F, 3F, 5F, new CubeDeformation(0.25F, 0.25F, 0.25F)).texOffs(0, 35).addBox(-3.6F, 0F, -2.5F, 5F, 2F, 5F, new CubeDeformation(0.05F, 0.05F, 0.05F)).texOffs(88, 0).addBox(-3F, -2F, -2F, 4F, 7F, 4F, new CubeDeformation(0.45F, 0.45F, 0.45F)), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(52, 26).addBox(-1.4F, -3F, -2.5F, 5F, 3F, 5F, new CubeDeformation(0.25F, 0.25F, 0.25F)).texOffs(20, 35).addBox(-1.4F, 0F, -2.5F, 5F, 2F, 5F, new CubeDeformation(0.05F, 0.05F, 0.05F)).texOffs(104, 0).addBox(-1F, -2F, -2F, 4F, 7F, 4F, new CubeDeformation(0.45F, 0.45F, 0.45F)), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition confesor_guantes() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-3F, 4F, -2F, 4F, 6F, 4F, new CubeDeformation(0.62F, 0.62F, 0.62F)).texOffs(50, 35).addBox(-3F, 4F, -2F, 4F, 1F, 4F, new CubeDeformation(0.95F, 0.95F, 0.95F)).texOffs(94, 35).addBox(0F, 7F, -3F, 1F, 2F, 1F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 16).addBox(-1F, 4F, -2F, 4F, 6F, 4F, new CubeDeformation(0.62F, 0.62F, 0.62F)).texOffs(66, 35).addBox(-1F, 4F, -2F, 4F, 1F, 4F, new CubeDeformation(0.95F, 0.95F, 0.95F)).texOffs(98, 35).addBox(-1F, 7F, -3F, 1F, 2F, 1F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition confesor_grebas() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(96, 26).addBox(-4F, 10F, -2F, 8F, 3F, 4F, new CubeDeformation(0.82F, 0.82F, 0.82F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(32, 16).addBox(-2F, 0F, -2F, 4F, 6F, 4F, new CubeDeformation(0.5F, 0.5F, 0.5F)).texOffs(82, 35).addBox(-1F, 5F, -3F, 2F, 2F, 1F, new CubeDeformation(0.25F, 0.25F, 0.25F)).texOffs(48, 16).addBox(-2F, 6F, -2F, 4F, 6F, 4F, new CubeDeformation(0.56F, 0.56F, 0.56F)).texOffs(102, 35).addBox(-2F, 11F, -3F, 4F, 1F, 1F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(64, 16).addBox(-2F, 0F, -2F, 4F, 6F, 4F, new CubeDeformation(0.5F, 0.5F, 0.5F)).texOffs(88, 35).addBox(-1F, 5F, -3F, 2F, 2F, 1F, new CubeDeformation(0.25F, 0.25F, 0.25F)).texOffs(80, 16).addBox(-2F, 6F, -2F, 4F, 6F, 4F, new CubeDeformation(0.56F, 0.56F, 0.56F)).texOffs(112, 35).addBox(-2F, 11F, -3F, 4F, 1F, 1F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition vagabundo_yelmo() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.62F, 0.62F, 0.62F)).texOffs(56, 0).addBox(-4F, -8F, -4F, 8F, 3F, 8F, new CubeDeformation(0.82F, 0.82F, 0.82F)).texOffs(96, 16).addBox(-3F, -10F, -3F, 6F, 2F, 6F, new CubeDeformation(0.25F, 0.25F, 0.25F)).texOffs(20, 35).addBox(-1F, -11F, -1F, 2F, 1F, 2F, new CubeDeformation(0.1F, 0.1F, 0.1F)).texOffs(16, 35).addBox(-0.5F, -5.2F, -5.15F, 1F, 3F, 1F, new CubeDeformation(0F, 0F, 0F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(24, 27).addBox(-4F, -1F, -2F, 8F, 2F, 4F, new CubeDeformation(0.9F, 0.9F, 0.9F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition vagabundo_peto() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-4F, 0F, -2F, 8F, 12F, 4F, new CubeDeformation(0.55F, 0.55F, 0.55F)).texOffs(0, 27).addBox(-4F, 11F, -2F, 8F, 4F, 4F, new CubeDeformation(0.72F, 0.72F, 0.72F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(88, 0).addBox(-3F, -2F, -2F, 4F, 7F, 4F, new CubeDeformation(0.47F, 0.47F, 0.47F)), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(104, 0).addBox(-1F, -2F, -2F, 4F, 7F, 4F, new CubeDeformation(0.47F, 0.47F, 0.47F)), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition vagabundo_guantes() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 16).addBox(-3F, 5F, -2F, 4F, 5F, 4F, new CubeDeformation(0.56F, 0.56F, 0.56F)).texOffs(72, 27).addBox(-3F, 5F, -2F, 4F, 1F, 4F, new CubeDeformation(0.82F, 0.82F, 0.82F)), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(48, 16).addBox(-1F, 5F, -2F, 4F, 5F, 4F, new CubeDeformation(0.56F, 0.56F, 0.56F)).texOffs(88, 27).addBox(-1F, 5F, -2F, 4F, 1F, 4F, new CubeDeformation(0.82F, 0.82F, 0.82F)), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition vagabundo_grebas() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(48, 27).addBox(-4F, 10F, -2F, 8F, 1F, 4F, new CubeDeformation(0.86F, 0.86F, 0.86F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2F, 0F, -2F, 4F, 7F, 4F, new CubeDeformation(0.42F, 0.42F, 0.42F)).texOffs(64, 16).addBox(-2F, 7F, -2F, 4F, 5F, 4F, new CubeDeformation(0.58F, 0.58F, 0.58F)).texOffs(104, 27).addBox(-2F, 7F, -2F, 4F, 1F, 4F, new CubeDeformation(0.84F, 0.84F, 0.84F)), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 16).addBox(-2F, 0F, -2F, 4F, 7F, 4F, new CubeDeformation(0.42F, 0.42F, 0.42F)).texOffs(80, 16).addBox(-2F, 7F, -2F, 4F, 5F, 4F, new CubeDeformation(0.58F, 0.58F, 0.58F)).texOffs(0, 35).addBox(-2F, 7F, -2F, 4F, 1F, 4F, new CubeDeformation(0.84F, 0.84F, 0.84F)), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition guerrero_yelmo() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.62F, 0.62F, 0.62F)).texOffs(56, 0).addBox(-4F, -8F, -4F, 8F, 3F, 8F, new CubeDeformation(0.85F, 0.85F, 0.85F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 27).addBox(-4F, -1F, -2F, 8F, 2F, 4F, new CubeDeformation(0.9F, 0.9F, 0.9F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        PartDefinition p_banda_cola = p_head.addOrReplaceChild("banda_cola", CubeListBuilder.create().texOffs(24, 27).addBox(-1F, 0F, 0F, 2F, 5F, 1F, new CubeDeformation(0F, 0F, 0F)), PartPose.offsetAndRotation(1.5F, -5.6F, 4.85F, 0.3142F, 0F, -0.1396F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition guerrero_peto() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-4F, 0F, -2F, 8F, 12F, 4F, new CubeDeformation(0.55F, 0.55F, 0.55F)).texOffs(96, 16).addBox(-4F, 11F, -2F, 8F, 4F, 4F, new CubeDeformation(0.72F, 0.72F, 0.72F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(88, 0).addBox(-3F, -2F, -2F, 4F, 7F, 4F, new CubeDeformation(0.47F, 0.47F, 0.47F)), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(104, 0).addBox(-1F, -2F, -2F, 4F, 7F, 4F, new CubeDeformation(0.47F, 0.47F, 0.47F)), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition guerrero_guantes() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 16).addBox(-3F, 5F, -2F, 4F, 5F, 4F, new CubeDeformation(0.56F, 0.56F, 0.56F)).texOffs(54, 27).addBox(-3F, 5F, -2F, 4F, 1F, 4F, new CubeDeformation(0.82F, 0.82F, 0.82F)), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(48, 16).addBox(-1F, 5F, -2F, 4F, 5F, 4F, new CubeDeformation(0.56F, 0.56F, 0.56F)).texOffs(70, 27).addBox(-1F, 5F, -2F, 4F, 1F, 4F, new CubeDeformation(0.82F, 0.82F, 0.82F)), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition guerrero_grebas() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(30, 27).addBox(-4F, 10F, -2F, 8F, 1F, 4F, new CubeDeformation(0.86F, 0.86F, 0.86F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2F, 0F, -2F, 4F, 7F, 4F, new CubeDeformation(0.42F, 0.42F, 0.42F)).texOffs(64, 16).addBox(-2F, 7F, -2F, 4F, 5F, 4F, new CubeDeformation(0.58F, 0.58F, 0.58F)).texOffs(86, 27).addBox(-2F, 7F, -2F, 4F, 1F, 4F, new CubeDeformation(0.84F, 0.84F, 0.84F)), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 16).addBox(-2F, 0F, -2F, 4F, 7F, 4F, new CubeDeformation(0.42F, 0.42F, 0.42F)).texOffs(80, 16).addBox(-2F, 7F, -2F, 4F, 5F, 4F, new CubeDeformation(0.58F, 0.58F, 0.58F)).texOffs(102, 27).addBox(-2F, 7F, -2F, 4F, 1F, 4F, new CubeDeformation(0.84F, 0.84F, 0.84F)), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition barbaro_yelmo() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(56, 0).addBox(-4F, -8F, -4F, 8F, 2F, 8F, new CubeDeformation(0.78F, 0.78F, 0.78F)).texOffs(88, 0).addBox(-4F, -6F, -4F, 8F, 1F, 8F, new CubeDeformation(1F, 0.35F, 1F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        PartDefinition p_cuerno1_right = p_head.addOrReplaceChild("cuerno1_right", CubeListBuilder.create().texOffs(82, 25).addBox(-3F, -1F, -1F, 3F, 2F, 2F, new CubeDeformation(0F, 0F, 0F)), PartPose.offsetAndRotation(-4.7F, -7F, 0.5F, 0F, 0F, 0.5585F));
        PartDefinition p_cuerno2_right = p_cuerno1_right.addOrReplaceChild("cuerno2_right", CubeListBuilder.create().texOffs(102, 25).addBox(-3F, -0.5F, -0.5F, 3F, 1F, 1F, new CubeDeformation(0F, 0F, 0F)), PartPose.offsetAndRotation(-3F, 0F, 0F, 0F, 0F, 0.6632F));
        PartDefinition p_cuerno3_right = p_cuerno2_right.addOrReplaceChild("cuerno3_right", CubeListBuilder.create().texOffs(0, 31).addBox(-2F, -0.5F, -0.5F, 2F, 1F, 1F, new CubeDeformation(0F, 0F, 0F)), PartPose.offsetAndRotation(-3F, 0F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_cuerno1_left = p_head.addOrReplaceChild("cuerno1_left", CubeListBuilder.create().texOffs(92, 25).addBox(0F, -1F, -1F, 3F, 2F, 2F, new CubeDeformation(0F, 0F, 0F)), PartPose.offsetAndRotation(4.7F, -7F, 0.5F, 0F, 0F, -0.5585F));
        PartDefinition p_cuerno2_left = p_cuerno1_left.addOrReplaceChild("cuerno2_left", CubeListBuilder.create().texOffs(110, 25).addBox(0F, -0.5F, -0.5F, 3F, 1F, 1F, new CubeDeformation(0F, 0F, 0F)), PartPose.offsetAndRotation(3F, 0F, 0F, 0F, 0F, -0.6632F));
        PartDefinition p_cuerno3_left = p_cuerno2_left.addOrReplaceChild("cuerno3_left", CubeListBuilder.create().texOffs(6, 31).addBox(0F, -0.5F, -0.5F, 2F, 1F, 1F, new CubeDeformation(0F, 0F, 0F)), PartPose.offsetAndRotation(3F, 0F, 0F, 0F, 0F, -0.6981F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition barbaro_peto() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, 0F, -2F, 8F, 12F, 4F, new CubeDeformation(0.5F, 0.5F, 0.5F)).texOffs(0, 16).addBox(-5F, -1F, -3F, 10F, 3F, 6F, new CubeDeformation(0.15F, 0.15F, 0.15F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 16).addBox(-3.8F, -3.2F, -2.6F, 5F, 3F, 5F, new CubeDeformation(0.1F, 0.1F, 0.1F)), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(84, 16).addBox(-1.4F, -2.8F, -2.5F, 5F, 2F, 5F, new CubeDeformation(0.15F, 0.15F, 0.15F)), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        PartDefinition p_colmillo = p_right_arm.addOrReplaceChild("colmillo", CubeListBuilder.create().texOffs(118, 25).addBox(-3F, -0.5F, -0.5F, 3F, 1F, 1F, new CubeDeformation(0F, 0F, 0F)), PartPose.offsetAndRotation(-3.6F, -3F, -1F, 0F, 0F, 0.6109F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition barbaro_guantes() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(52, 16).addBox(-3F, 5F, -2F, 4F, 4F, 4F, new CubeDeformation(0.55F, 0.55F, 0.55F)).texOffs(50, 25).addBox(-3F, 5F, -2F, 4F, 1F, 4F, new CubeDeformation(0.88F, 0.88F, 0.88F)), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(68, 16).addBox(-1F, 5F, -2F, 4F, 4F, 4F, new CubeDeformation(0.55F, 0.55F, 0.55F)).texOffs(66, 25).addBox(-1F, 5F, -2F, 4F, 1F, 4F, new CubeDeformation(0.88F, 0.88F, 0.88F)), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition barbaro_grebas() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(26, 25).addBox(-4F, 10F, -2F, 8F, 1F, 4F, new CubeDeformation(0.75F, 0.75F, 0.75F)).texOffs(16, 25).addBox(-2F, 11F, -3.6F, 4F, 5F, 1F, new CubeDeformation(0F, 0F, 0F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 0).addBox(-2F, 0F, -2F, 4F, 12F, 4F, new CubeDeformation(0.45F, 0.45F, 0.45F)).texOffs(104, 16).addBox(-2F, 8F, -2F, 4F, 2F, 4F, new CubeDeformation(0.78F, 0.78F, 0.78F)), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(40, 0).addBox(-2F, 0F, -2F, 4F, 12F, 4F, new CubeDeformation(0.45F, 0.45F, 0.45F)).texOffs(0, 25).addBox(-2F, 8F, -2F, 4F, 2F, 4F, new CubeDeformation(0.78F, 0.78F, 0.78F)), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition bandido_yelmo() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.66F, 0.66F, 0.66F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        PartDefinition p_capucha_cola = p_head.addOrReplaceChild("capucha_cola", CubeListBuilder.create().texOffs(104, 16).addBox(-2F, 0F, 0F, 4F, 4F, 1F, new CubeDeformation(0F, 0F, 0F)), PartPose.offsetAndRotation(0F, -7.6F, 4.62F, 0.5585F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 32);
    }

    private static LayerDefinition bandido_peto() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-4F, 0F, -2F, 8F, 12F, 4F, new CubeDeformation(0.5F, 0.5F, 0.5F)).texOffs(88, 0).addBox(-4F, 0F, 2.6F, 8F, 10F, 1F, new CubeDeformation(0F, 0F, 0F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(106, 0).addBox(-3F, -2F, -2F, 4F, 6F, 4F, new CubeDeformation(0.42F, 0.42F, 0.42F)), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 16).addBox(-1F, -2F, -2F, 4F, 6F, 4F, new CubeDeformation(0.42F, 0.42F, 0.42F)), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 32);
    }

    private static LayerDefinition bandido_guantes() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-3F, 4F, -2F, 4F, 6F, 4F, new CubeDeformation(0.56F, 0.56F, 0.56F)), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 16).addBox(-1F, 4F, -2F, 4F, 6F, 4F, new CubeDeformation(0.56F, 0.56F, 0.56F)), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 32);
    }

    private static LayerDefinition bandido_grebas() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(80, 16).addBox(-4F, 10F, -2F, 8F, 1F, 4F, new CubeDeformation(0.8F, 0.8F, 0.8F)).texOffs(114, 16).addBox(1.5F, 10.5F, -3.5F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, 0F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(56, 0).addBox(-2F, 0F, -2F, 4F, 8F, 4F, new CubeDeformation(0.38F, 0.38F, 0.38F)).texOffs(48, 16).addBox(-2F, 7F, -2F, 4F, 5F, 4F, new CubeDeformation(0.56F, 0.56F, 0.56F)), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(72, 0).addBox(-2F, 0F, -2F, 4F, 8F, 4F, new CubeDeformation(0.38F, 0.38F, 0.38F)).texOffs(64, 16).addBox(-2F, 7F, -2F, 4F, 5F, 4F, new CubeDeformation(0.56F, 0.56F, 0.56F)), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 32);
    }

    private static LayerDefinition hechicero_yelmo() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(16, 16).addBox(-4F, -8F, -4F, 8F, 2F, 8F, new CubeDeformation(0.75F, 0.75F, 0.75F)).texOffs(56, 0).addBox(-6F, -6.4F, -6F, 12F, 1F, 12F, new CubeDeformation(0F, 0F, 0F)).texOffs(48, 16).addBox(-3F, -11F, -3F, 6F, 3F, 6F, new CubeDeformation(0.3F, 0.3F, 0.3F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        PartDefinition p_cono2 = p_head.addOrReplaceChild("cono2", CubeListBuilder.create().texOffs(16, 28).addBox(-2F, -3F, -2F, 4F, 3F, 4F, new CubeDeformation(0.2F, 0.2F, 0.2F)).texOffs(118, 28).addBox(-1F, -6F, -1F, 2F, 3F, 2F, new CubeDeformation(0.12F, 0.12F, 0.12F)), PartPose.offsetAndRotation(0F, -11F, 0F, 0.384F, 0F, 0.0698F));
        PartDefinition p_cono4 = p_cono2.addOrReplaceChild("cono4", CubeListBuilder.create().texOffs(0, 35).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(0F, 0F, 0F)), PartPose.offsetAndRotation(0F, -6F, 0F, 0.5236F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition hechicero_peto() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, 0F, -2F, 8F, 12F, 4F, new CubeDeformation(0.58F, 0.58F, 0.58F)).texOffs(38, 28).addBox(-4F, -1F, -2F, 8F, 1F, 4F, new CubeDeformation(0.9F, 0.9F, 0.9F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(104, 0).addBox(-3F, -2F, -2F, 4F, 8F, 4F, new CubeDeformation(0.55F, 0.55F, 0.55F)).texOffs(72, 16).addBox(-3F, 5F, -2F, 4F, 3F, 4F, new CubeDeformation(0.95F, 0.95F, 0.95F)), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-1F, -2F, -2F, 4F, 8F, 4F, new CubeDeformation(0.55F, 0.55F, 0.55F)).texOffs(104, 16).addBox(-1F, 5F, -2F, 4F, 3F, 4F, new CubeDeformation(0.95F, 0.95F, 0.95F)), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition hechicero_guantes() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(88, 16).addBox(-3F, 7F, -2F, 4F, 3F, 4F, new CubeDeformation(0.4F, 0.4F, 0.4F)), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 28).addBox(-1F, 7F, -2F, 4F, 3F, 4F, new CubeDeformation(0.4F, 0.4F, 0.4F)), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition hechicero_grebas() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(62, 28).addBox(-4F, 10F, -2F, 8F, 1F, 4F, new CubeDeformation(0.8F, 0.8F, 0.8F)).texOffs(32, 28).addBox(-1F, 10F, -3.6F, 2F, 6F, 1F, new CubeDeformation(0F, 0F, 0F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 0).addBox(-2F, 0F, -2F, 4F, 11F, 4F, new CubeDeformation(0.72F, 0.72F, 0.72F)).texOffs(86, 28).addBox(-2F, 11F, -2F, 4F, 1F, 4F, new CubeDeformation(0.55F, 0.55F, 0.55F)), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(40, 0).addBox(-2F, 0F, -2F, 4F, 11F, 4F, new CubeDeformation(0.72F, 0.72F, 0.72F)).texOffs(102, 28).addBox(-2F, 11F, -2F, 4F, 1F, 4F, new CubeDeformation(0.55F, 0.55F, 0.55F)), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static LayerDefinition profeta_yelmo() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(88, 0).addBox(-4F, -5F, -4F, 8F, 2F, 8F, new CubeDeformation(0.64F, 0.64F, 0.64F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        PartDefinition p_nudo = p_head.addOrReplaceChild("nudo", CubeListBuilder.create().texOffs(40, 26).addBox(-1F, -1F, 0F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, 0F)).texOffs(38, 26).addBox(-2F, 0.5F, 0.5F, 1F, 4F, 0F, new CubeDeformation(0F, 0F, 0F)).texOffs(36, 26).addBox(1F, 0.5F, 0.5F, 1F, 5F, 0F, new CubeDeformation(0F, 0F, 0F)), PartPose.offsetAndRotation(0F, -4.2F, 4.64F, 0.1745F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 32);
    }

    private static LayerDefinition profeta_peto() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, 0F, -2F, 8F, 12F, 4F, new CubeDeformation(0.58F, 0.58F, 0.58F)).texOffs(0, 16).addBox(-5F, -1F, -3F, 10F, 4F, 6F, new CubeDeformation(0.1F, 0.1F, 0.1F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(56, 0).addBox(-3F, -2F, -2F, 4F, 9F, 4F, new CubeDeformation(0.52F, 0.52F, 0.52F)), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(72, 0).addBox(-1F, -2F, -2F, 4F, 9F, 4F, new CubeDeformation(0.52F, 0.52F, 0.52F)), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 32);
    }

    private static LayerDefinition profeta_guantes() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 16).addBox(-3F, 6F, -2F, 4F, 4F, 4F, new CubeDeformation(0.5F, 0.5F, 0.5F)).texOffs(0, 26).addBox(-3F, 6F, -2F, 4F, 1F, 4F, new CubeDeformation(0.78F, 0.78F, 0.78F)), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(48, 16).addBox(-1F, 6F, -2F, 4F, 4F, 4F, new CubeDeformation(0.5F, 0.5F, 0.5F)).texOffs(16, 26).addBox(-1F, 6F, -2F, 4F, 1F, 4F, new CubeDeformation(0.78F, 0.78F, 0.78F)), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 32);
    }

    private static LayerDefinition profeta_grebas() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(96, 16).addBox(-4F, 9F, -2F, 8F, 1F, 4F, new CubeDeformation(0.75F, 0.75F, 0.75F)).texOffs(32, 26).addBox(1F, 10F, -3.4F, 1F, 4F, 1F, new CubeDeformation(0F, 0F, 0F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(24, 0).addBox(-2F, 0F, -2F, 4F, 10F, 4F, new CubeDeformation(0.7F, 0.7F, 0.7F)).texOffs(64, 16).addBox(-2F, 10F, -2F, 4F, 2F, 4F, new CubeDeformation(0.5F, 0.5F, 0.5F)), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(40, 0).addBox(-2F, 0F, -2F, 4F, 10F, 4F, new CubeDeformation(0.7F, 0.7F, 0.7F)).texOffs(80, 16).addBox(-2F, 10F, -2F, 4F, 2F, 4F, new CubeDeformation(0.5F, 0.5F, 0.5F)), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 32);
    }

    private static LayerDefinition prisionero_yelmo() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(1.05F, 1.05F, 1.05F)).texOffs(88, 0).addBox(-4F, -1F, -4F, 8F, 1F, 8F, new CubeDeformation(1.35F, 0.35F, 1.35F)).texOffs(96, 16).addBox(-1F, -1.6F, -6F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, 0F)).texOffs(102, 16).addBox(-1F, -10F, -0.5F, 2F, 1F, 1F, new CubeDeformation(0F, 0F, 0F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 32);
    }

    private static LayerDefinition prisionero_peto() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(32, 0).addBox(-4F, 0F, -2F, 8F, 12F, 4F, new CubeDeformation(0.45F, 0.45F, 0.45F)), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 16).addBox(-3F, -2F, -2F, 4F, 5F, 4F, new CubeDeformation(0.4F, 0.4F, 0.4F)), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(16, 16).addBox(-1F, -2F, -2F, 4F, 5F, 4F, new CubeDeformation(0.4F, 0.4F, 0.4F)), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 32);
    }

    private static LayerDefinition prisionero_guantes() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(64, 16).addBox(-3F, 7F, -2F, 4F, 2F, 4F, new CubeDeformation(0.75F, 0.75F, 0.75F)), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(80, 16).addBox(-1F, 7F, -2F, 4F, 2F, 4F, new CubeDeformation(0.75F, 0.75F, 0.75F)), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 32);
    }

    private static LayerDefinition prisionero_grebas() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(56, 0).addBox(-2F, 0F, -2F, 4F, 9F, 4F, new CubeDeformation(0.36F, 0.36F, 0.36F)).texOffs(32, 16).addBox(-2F, 9F, -2F, 4F, 3F, 4F, new CubeDeformation(0.45F, 0.45F, 0.45F)), PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(72, 0).addBox(-2F, 0F, -2F, 4F, 9F, 4F, new CubeDeformation(0.36F, 0.36F, 0.36F)).texOffs(48, 16).addBox(-2F, 9F, -2F, 4F, 3F, 4F, new CubeDeformation(0.45F, 0.45F, 0.45F)), PartPose.offset(1.9F, 12F, 0F));
        return LayerDefinition.create(mesh, 128, 32);
    }
}

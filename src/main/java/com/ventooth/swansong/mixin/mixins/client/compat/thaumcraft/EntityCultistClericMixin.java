package com.ventooth.swansong.mixin.mixins.client.compat.thaumcraft;

import org.spongepowered.asm.mixin.Mixin;
import thaumcraft.common.entities.monster.EntityCultist;
import thaumcraft.common.entities.monster.EntityCultistCleric;

import net.minecraft.world.World;

@Mixin(EntityCultistCleric.class)
public abstract class EntityCultistClericMixin extends EntityCultist {
    public EntityCultistClericMixin(World world) {
        super(world);
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0 || pass == 1;
    }
}

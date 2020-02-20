package org.kilocraft.essentials.extensions.magicalparticles.config;

import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Optional;

@ConfigSerializable
public class ParticleFrameConfigSection {

    @Setting("count")
    public int count = 0;

    @Setting("effect")
    public String effect = "dragon_breath";

    @Setting("blockProperties")
    private BlockStateParticleEffectConfigSection blockStateSection = this.isEqualTo(ParticleTypes.BLOCK) || this.isEqualTo(ParticleTypes.ITEM) ? new BlockStateParticleEffectConfigSection() : null;

    @Setting("dustProperties")
    private DustParticleEffectConfigSection dustParticleSection = this.isEqualTo(ParticleTypes.DUST) || this.isEqualTo(ParticleTypes.FALLING_DUST) ? new DustParticleEffectConfigSection() : null;

    @Setting("longDistance")
    public boolean longDistance = true;

    @Setting("pos")
    public String pos = "0 0 0";

    @Setting("offset")
    public String offset = "10 0.5 0";

    @Setting("speed")
    public double speed = 0.3D;

    public Optional<BlockStateParticleEffectConfigSection> getBlockStateSection() {
        return Optional.ofNullable(blockStateSection);
    }

    public Optional<DustParticleEffectConfigSection> getDustParticleSection() {
        return Optional.ofNullable(dustParticleSection);
    }

    private boolean isEqualTo(ParticleType<?> particleType) {
        Identifier identifier = Registry.PARTICLE_TYPE.getId(particleType);
        return identifier != null && this.effect.equalsIgnoreCase(identifier.getPath());
    }
}

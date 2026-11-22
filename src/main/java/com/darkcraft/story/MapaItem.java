package com.darkcraft.story;

import com.darkcraft.gen.CastlePlan;
import com.darkcraft.gen.MapShapes;
import com.darkcraft.net.Net;
import com.darkcraft.world.CastleSites;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Mapa de pergamino de la ciudadela: al usarlo se despliega y marca la siguiente pagina de la cronica. */
public class MapaItem extends Item {
    private static final Map<String, MapShapes.Data> CACHE = new LinkedHashMap<>(8, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, MapShapes.Data> e) {
            return size() > 4;
        }
    };

    public MapaItem(Properties p) {
        super(p);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack st = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            CompoundTag t = st.getOrCreateTag();
            int cx, cz;
            if (t.contains("castilloX")) {
                cx = t.getInt("castilloX");
                cz = t.getInt("castilloZ");
            } else {
                int[] s = CastleSites.nearest(sp.serverLevel().getSeed(), sp.getBlockX(), sp.getBlockZ(), 3);
                if (s == null) {
                    sp.displayClientMessage(Component.translatable("darkcraft.mapa.sin_castillo").withStyle(ChatFormatting.GRAY), true);
                    return InteractionResultHolder.fail(st);
                }
                cx = s[0];
                cz = s[1];
                t.putInt("castilloX", cx);
                t.putInt("castilloZ", cz);
            }
            MapShapes.Data d;
            synchronized (CACHE) {
                d = CACHE.get(cx + "," + cz);
                if (d == null) {
                    CastlePlan plan = Story.planAt(sp.serverLevel(), cx, cz);
                    d = MapShapes.build(plan);
                    CACHE.put(cx + "," + cz, d);
                }
            }
            Net.to(sp, new Net.MapOpen(cx, cz, Story.read(sp), d.shapes(), d.labels(), d.labelPos(), d.chapters()));
            level.playSound(null, sp.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 0.75f);
        }
        return InteractionResultHolder.sidedSuccess(st, level.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("darkcraft.mapa.lore").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}

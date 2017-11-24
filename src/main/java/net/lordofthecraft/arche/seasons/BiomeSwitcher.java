package net.lordofthecraft.arche.seasons;

import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import net.lordofthecraft.arche.ArcheCore;

public class BiomeSwitcher{
	private final AtomicBoolean winter;
	private final ArcheCore plugin;
	private final LotcianCalendar calendar;

	BiomeSwitcher(final ArcheCore plugin,  final LotcianCalendar calendar) {
		this.plugin = plugin;
		this.calendar = calendar;
		final boolean isWinter = calendar.getMonth().getSeason() == Season.WINTER;
		this.winter = new AtomicBoolean(isWinter);
	}
	
	public void startListening() {
		ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(
			new PacketAdapter(plugin, PacketType.Play.Server.MAP_CHUNK, PacketType.Play.Server.UPDATE_TIME) {
			public void onPacketSending(final PacketEvent event) {
				if(event.getPacketType() == PacketType.Play.Server.MAP_CHUNK) {
					if (winter.get()) {
						PacketContainer packet = event.getPacket();
	
						boolean groundUpContinuous = packet.getBooleans().read(0);
						if(!groundUpContinuous) return; //Don't change this chunk
						ChunkInfo info = new ChunkInfo(event.getPlayer(), packet.getIntegers().readSafely(2), 0, true, packet.getByteArrays().readSafely(0), 0);
						BiomeSwitcher.this.translateChunkInfo(info, Season.WINTER);
					}		
				} else {
					String worldname = event.getPlayer().getWorld().getName();
					if(calendar.getRunnable().getTrackedWorlds().contains(worldname)) {
						PacketContainer packet = event.getPacket();
						StructureModifier<Long> longs = packet.getLongs();
						long time = longs.read(1);
						if(time > 0) time *= -1;
						longs.write(1, time);
					}
				}
			}
		}).start();
	}

	public void setWinter(final boolean winter) {
		this.winter.set(winter);
	}

	protected final boolean translateChunkInfo(final ChunkInfo info, final Season season) {
		if (info.hasContinous) {
			for (int i = info.data.length - 256; i < info.data.length; ++i) {
				final byte biome = info.data[i];
				final byte replacement = BiomeType.getWinterBiome(biome).getId();
				if (replacement >= 0) {
					info.data[i] = replacement;
				}

			}
			return true;
		}
		return false;
	}

	protected final <T> T getOrDefault(final T value, final T defaultIfNull) {
		return (value != null) ? value : defaultIfNull;
	}

	protected static class ChunkInfo
	{
		public Player player;
		public int chunkMask;
		public int extraMask;
		public boolean hasContinous;
		public byte[] data;
		public int startIndex;
		public int chunkSectionNumber;
		public int extraSectionNumber;
		public int size;

		public ChunkInfo(final Player player, final int chunkMask, final int extraMask, final boolean hasContinous, final byte[] data, final int startIndex) {
			super();
			this.player = player;
			this.chunkMask = chunkMask;
			this.extraMask = extraMask;
			this.hasContinous = hasContinous;
			this.data = data;
			this.startIndex = startIndex;
		}
	}

	public class PacketPluginHookInitializationException extends Exception
	{
		private static final long serialVersionUID = 1L;

		public PacketPluginHookInitializationException(final String message) {
			super(message);
		}
	}
}
package net.lordofthecraft.arche.seasons;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.CoreLog;

public class BiomeSwitcher{
	private final AtomicBoolean winter;
	private final ArcheCore plugin;
	private final LotcianCalendar calendar;
	private final boolean switchBiomes;

	boolean t = false;
	
	BiomeSwitcher(final ArcheCore plugin,  final LotcianCalendar calendar, boolean switchBiomes) {
		this.plugin = plugin;
		this.calendar = calendar;
		final boolean isWinter = calendar.getMonth().getSeason() == Season.WINTER;
		this.winter = new AtomicBoolean(isWinter);
		this.switchBiomes = switchBiomes;
	}
	
	public void startListening() {
		CoreLog.info("Starting up into Winter: " + winter.get());
		if(switchBiomes) ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(
				new PacketAdapter(plugin, PacketType.Play.Server.MAP_CHUNK) {
					@Override
					public void onPacketSending(final PacketEvent event) {
						if (winter.get()) {
							PacketContainer packet = event.getPacket();

							boolean groundUpContinuous = packet.getBooleans().read(0);
							if(!groundUpContinuous) return; //Don't change this chunk
							ChunkInfo info = new ChunkInfo(event.getPlayer(), packet.getIntegers().readSafely(2), 0, true, packet.getByteArrays().readSafely(0), 0);
							BiomeSwitcher.this.translateChunkInfo(info);
						}
					}
				}).start();
		
		ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(
				new PacketAdapter(plugin, PacketType.Play.Server.UPDATE_TIME) {
					@Override
					public void onPacketSending(final PacketEvent event) {
						String worldname = event.getPlayer().getWorld().getName();
						if(calendar.getRunnable().getTrackedWorlds().contains(worldname)) {
							PacketContainer packet = event.getPacket();
							StructureModifier<Long> longs = packet.getLongs();
							long time = longs.read(1);
							//System.out.println("Time was: (spam " + time + " but also " + longs.read(0));
							if(time > 0) time *= -1;
							longs.write(1, time);
						}
					}
				}).start();
	}

	public void setWinter(final boolean winter) {
		this.winter.set(winter);
		CoreLog.debug("Setting us to Winter: " + winter);
	}

	protected final boolean translateChunkInfo(final ChunkInfo info) {
		if (info.hasContinous) {
			ByteBuffer buf = ByteBuffer.wrap(info.data, info.data.length - 1024, 1024);
			IntBuffer out = buf.asIntBuffer();
			for(int i = 0; i < 256; i++) {
				int biome = buf.getInt();
				int replacement = BiomeType.getWinterBiome(biome).getId();
				if(replacement >= 0) out.put(replacement);
				else out.get();
			}
			t=true;
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
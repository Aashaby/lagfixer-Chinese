package xyz.lychee.lagfixer.menu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OSFileStore;
import oshi.util.FormatUtil;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.commands.MenuCommand;
import xyz.lychee.lagfixer.objects.AbstractMenu;
import xyz.lychee.lagfixer.utils.ItemBuilder;
import xyz.lychee.lagfixer.utils.MessageUtils;
import xyz.lychee.lagfixer.utils.TimingUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HardwareMenu extends AbstractMenu {
    private final ItemBuilder i1;
    private final ItemBuilder i2;
    private final ItemBuilder i3;
    private final ItemBuilder i4;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final SystemInfo si;
    private final HardwareAbstractionLayer hal;
    private HardwareData hardwareData;
    private long lastNetworkUpdate = 0;
    private long prevBytesSent = 0;
    private long prevBytesRecv = 0;

    public HardwareMenu(LagFixer plugin, int size, String title) {
        super(plugin, size, Language.getLocalized("menu_hardware_title",
                Placeholder.unparsed("version", plugin.getDescription().getVersion())), 3, true);
        this.si = new SystemInfo();
        this.hal = this.si.getHardware();

        this.loadOrCreateHardwareData(plugin);

        this.i1 = createSkullItem("menu_hardware_item_network",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTJiMTcxMmI5MDdjZTZiMTQwMmVhYWMyOGVjMjRhNGQ5NTU2OGY0YWI4N2U1OTc5ODBjMTViMjJiYmJkN2E1In19fQ==");
        this.i2 = createSkullItem("menu_hardware_item_processor",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDU2Yzk0NjE5MDMxMjMxNjhjZTY2N2VhZDdlYTU2YTUxNjEzMDk3MDQ5YmE2NDc4MzJiMzcyMmFmZmJlYjYzNiJ9fX0=");
        this.i3 = createSkullItem("menu_hardware_item_memory",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjYwYjAwNGYzNjBlMjg4NTVjY2YxMjM1YzJiZGVhMGEyOTk3YjBiYzAzMjU4ZTJkYzI0YWI4YTI1NzBhZWE2In19fQ==");
        this.i4 = createSkullItem("menu_hardware_item_storage",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjg5MWFmZDM5ZTJlNjczOGJjNmE4Yzg4YzI0OWZkYmNmNGE0NWM0YTI0MjQ3ZjFkMTBiYWUwYzY0ZDk5OTFlMSJ9fX0=");

        this.surroundInventory();  // ← 父类会处理返回按钮
        this.fillButtons();

        this.getInv().setItem(11,
                ItemBuilder.createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWY0NDZhOGY5Mjg0YzYyY2Y4ZDQ5MWZiZGIzMzhmZDM5ZWJiZWJlMzVlOTU5YzJmYzRmNzg2YzY3NTIyZWZiIn19fQ==")
                        .setName(Language.getLocalized("menu_hardware_warning_name"))
                        .setLore(
                                Language.getLocalized("menu_hardware_warning_lore_1"),
                                Language.getLocalized("menu_hardware_warning_lore_2"),
                                "",
                                Language.getLocalized("menu_hardware_warning_lore_3"),
                                Language.getLocalized("menu_hardware_warning_lore_4")
                        ).build()
        );
        // 删除这一行：this.getInv().setItem(size - 1, ConfigMenu.getBack());
        // 因为父类的 surroundInventory 已经设置了返回按钮

        this.fillInventory();
    }

    private ItemBuilder createSkullItem(String nameKey, String texture) {
        return ItemBuilder.createSkull(texture)
                .setName(Language.getLocalized(nameKey))
                .setLore(" &8{*} " + Language.getLocalized("menu_hardware_lore_loading"));
    }

    private void loadOrCreateHardwareData(LagFixer plugin) {
        File dataFile = new File(plugin.getDataFolder(), "hardware_data.json");
        if (dataFile.exists()) {
            try (Reader reader = new FileReader(dataFile)) {
                this.hardwareData = gson.fromJson(reader, HardwareData.class);
                return;
            } catch (IOException ignored) {}
        }
        collectAndSaveHardwareData(dataFile);
    }

    private void collectAndSaveHardwareData(File dataFile) {
        SystemInfo tempSi = new SystemInfo();
        HardwareAbstractionLayer tempHal = tempSi.getHardware();
        CentralProcessor cpu = tempHal.getProcessor();
        GlobalMemory memory = tempHal.getMemory();
        List<HWDiskStore> disks = tempHal.getDiskStores();

        this.hardwareData = new HardwareData();
        this.hardwareData.setCpuName(cpu.getProcessorIdentifier().getName());
        this.hardwareData.setCpuMicroarchitecture(cpu.getProcessorIdentifier().getMicroarchitecture());
        this.hardwareData.setCpuVendorFreq(cpu.getProcessorIdentifier().getVendorFreq());
        this.hardwareData.setLogicalCores(cpu.getLogicalProcessorCount());
        this.hardwareData.setPhysicalCores(cpu.getPhysicalProcessorCount());
        this.hardwareData.setTotalMemory(memory.getTotal());
        this.hardwareData.setPageSize(memory.getPageSize());
        this.hardwareData.setMemoryType(memory.getPhysicalMemory().isEmpty() ? "Unknown" : memory.getPhysicalMemory().getFirst().getMemoryType());
        this.hardwareData.setDiskCount(disks.size());
        this.hardwareData.setPartitionCount(disks.stream().mapToInt(d -> d.getPartitions().size()).sum());
        this.hardwareData.setNetworkInterfaceCount(tempHal.getNetworkIFs().size());

        try (Writer writer = new FileWriter(dataFile)) {
            gson.toJson(this.hardwareData, writer);
        } catch (IOException ignored) {}
    }

    private void fillButtons() {
        this.getInv().setItem(12, this.i1.build());
        this.getInv().setItem(13, this.i2.build());
        this.getInv().setItem(14, this.i3.build());
        this.getInv().setItem(15, this.i4.build());
    }

    @Override
    public void update() {
        TimingUtil timing = TimingUtil.startNew();

        // Network
        try {
            List<NetworkIF> networks = hal.getNetworkIFs();
            long bytesSent = 0, bytesRecv = 0;
            for (NetworkIF net : networks) {
                net.updateAttributes();
                bytesSent += net.getBytesSent();
                bytesRecv += net.getBytesRecv();
            }
            List<String> lore1 = new ArrayList<>();
            lore1.add(Language.getLocalized("menu_hardware_lore_interfaces",
                    Placeholder.unparsed("count", String.valueOf(hardwareData.getNetworkInterfaceCount()))));
            lore1.add(Language.getLocalized("menu_hardware_lore_sent",
                    Placeholder.unparsed("sent", FormatUtil.formatBytes(bytesSent))));
            lore1.add(Language.getLocalized("menu_hardware_lore_received",
                    Placeholder.unparsed("received", FormatUtil.formatBytes(bytesRecv))));
            lore1.add(Language.getLocalized("menu_hardware_lore_throughput",
                    Placeholder.unparsed("throughput", calculateNetworkSpeed(bytesSent, bytesRecv))));
            lore1.add(Language.getLocalized("menu_hardware_lore_updated_network",
                    Placeholder.unparsed("time", timing.stop().toString())));
            this.i1.setLore(lore1);
        } catch (Throwable t) {
            this.i1.setLore(Language.getLocalized("menu_hardware_lore_error").split("\n"));
        }

        timing.start();
        // CPU
        try {
            List<String> lore2 = new ArrayList<>();
            lore2.add(Language.getLocalized("menu_hardware_lore_cpu_model",
                    Placeholder.unparsed("model", hardwareData.getCpuName())));
            lore2.add(Language.getLocalized("menu_hardware_lore_logical_cores",
                    Placeholder.unparsed("cores", String.valueOf(hardwareData.getLogicalCores()))));
            lore2.add(Language.getLocalized("menu_hardware_lore_physical_cores",
                    Placeholder.unparsed("cores", String.valueOf(hardwareData.getPhysicalCores()))));
            lore2.add(Language.getLocalized("menu_hardware_lore_microarchitecture",
                    Placeholder.unparsed("arch", hardwareData.getCpuMicroarchitecture())));
            lore2.add(Language.getLocalized("menu_hardware_lore_frequency",
                    Placeholder.unparsed("freq", FormatUtil.formatHertz(hardwareData.getCpuVendorFreq()))));
            lore2.add(Language.getLocalized("menu_hardware_lore_updated_cpu",
                    Placeholder.unparsed("time", timing.stop().toString())));
            this.i2.setLore(lore2);
        } catch (Throwable t) {
            this.i2.setLore("&cAn error occurred while retrieving processor information :(");
        }

        timing.start();
        // Memory
        try {
            GlobalMemory memory = hal.getMemory();
            long usedMem = hardwareData.getTotalMemory() - memory.getAvailable();
            double memPercent = (usedMem * 100.0) / hardwareData.getTotalMemory();
            VirtualMemory swap = memory.getVirtualMemory();

            List<String> lore3 = new ArrayList<>();
            lore3.add(Language.getLocalized("menu_hardware_lore_total_ram",
                    Placeholder.unparsed("total", FormatUtil.formatBytesDecimal(hardwareData.getTotalMemory()))));
            lore3.add(Language.getLocalized("menu_hardware_lore_used_ram",
                    Placeholder.unparsed("used", FormatUtil.formatBytesDecimal(usedMem)),
                    Placeholder.unparsed("percent", String.format("%.1f", memPercent))));
            lore3.add(Language.getLocalized("menu_hardware_lore_available_ram",
                    Placeholder.unparsed("avail", FormatUtil.formatBytesDecimal(memory.getAvailable()))));
            lore3.add(Language.getLocalized("menu_hardware_lore_page_size",
                    Placeholder.unparsed("size", FormatUtil.formatBytesDecimal(hardwareData.getPageSize()))));
            lore3.add(Language.getLocalized("menu_hardware_lore_memory_type",
                    Placeholder.unparsed("type", hardwareData.getMemoryType())));
            lore3.add(Language.getLocalized("menu_hardware_lore_swap_total",
                    Placeholder.unparsed("total", FormatUtil.formatBytesDecimal(swap.getSwapTotal()))));
            lore3.add(Language.getLocalized("menu_hardware_lore_swap_used",
                    Placeholder.unparsed("used", FormatUtil.formatBytesDecimal(swap.getSwapUsed()))));
            lore3.add(Language.getLocalized("menu_hardware_lore_updated_memory",
                    Placeholder.unparsed("time", timing.stop().toString())));
            this.i3.setLore(lore3);
        } catch (Throwable t) {
            this.i3.setLore("&cAn error occurred while retrieving memory information :(");
        }

        timing.start();
        // Storage
        try {
            long totalReadBytes = 0, totalWriteBytes = 0, readOps = 0, writeOps = 0, queueLength = 0;
            for (HWDiskStore disk : hal.getDiskStores()) {
                disk.updateAttributes();
                totalReadBytes += disk.getReadBytes();
                totalWriteBytes += disk.getWriteBytes();
                readOps += disk.getReads();
                writeOps += disk.getWrites();
                queueLength += disk.getCurrentQueueLength();
            }
            long totalDiskSpace = 0, usedDiskSpace = 0;
            for (OSFileStore fs : si.getOperatingSystem().getFileSystem().getFileStores()) {
                totalDiskSpace += fs.getTotalSpace();
                usedDiskSpace += fs.getTotalSpace() - fs.getUsableSpace();
            }
            double diskUsagePercent = totalDiskSpace > 0 ? (usedDiskSpace * 100.0) / totalDiskSpace : 0;

            List<String> lore4 = new ArrayList<>();
            lore4.add(Language.getLocalized("menu_hardware_lore_disks",
                    Placeholder.unparsed("count", String.valueOf(hardwareData.getDiskCount()))));
            lore4.add(Language.getLocalized("menu_hardware_lore_partitions",
                    Placeholder.unparsed("count", String.valueOf(hardwareData.getPartitionCount()))));
            lore4.add(Language.getLocalized("menu_hardware_lore_capacity",
                    Placeholder.unparsed("capacity", FormatUtil.formatBytes(totalDiskSpace))));
            lore4.add(Language.getLocalized("menu_hardware_lore_used_space",
                    Placeholder.unparsed("used", FormatUtil.formatBytes(usedDiskSpace)),
                    Placeholder.unparsed("percent", String.format("%.1f", diskUsagePercent))));
            lore4.add(Language.getLocalized("menu_hardware_lore_read_data",
                    Placeholder.unparsed("read", FormatUtil.formatBytes(totalReadBytes)),
                    Placeholder.unparsed("ops", String.valueOf(readOps))));
            lore4.add(Language.getLocalized("menu_hardware_lore_written_data",
                    Placeholder.unparsed("written", FormatUtil.formatBytes(totalWriteBytes)),
                    Placeholder.unparsed("ops", String.valueOf(writeOps))));
            lore4.add(Language.getLocalized("menu_hardware_lore_disk_queues",
                    Placeholder.unparsed("queues", String.valueOf(queueLength))));
            lore4.add(Language.getLocalized("menu_hardware_lore_updated_storage",
                    Placeholder.unparsed("time", timing.stop().toString())));
            this.i4.setLore(lore4);
        } catch (Throwable t) {
            this.i4.setLore("&cAn error occurred while retrieving storage information :(");
        }

        this.fillButtons();
    }

    private String calculateNetworkSpeed(long currentSent, long currentRecv) {
        long timeDiff = System.currentTimeMillis() - lastNetworkUpdate;
        if (lastNetworkUpdate == 0 || timeDiff == 0) {
            lastNetworkUpdate = System.currentTimeMillis();
            prevBytesSent = currentSent;
            prevBytesRecv = currentRecv;
            return "Calculating...";
        }
        double sentSpeed = (currentSent - prevBytesSent) / (timeDiff / 1000.0);
        double recvSpeed = (currentRecv - prevBytesRecv) / (timeDiff / 1000.0);
        lastNetworkUpdate = System.currentTimeMillis();
        prevBytesSent = currentSent;
        prevBytesRecv = currentRecv;
        return String.format("&a▲&f%s/s &c▼&f%s/s", FormatUtil.formatBytesDecimal((long) sentSpeed), FormatUtil.formatBytesDecimal((long) recvSpeed));
    }

    @Override
    public void handleClick(InventoryClickEvent e, ItemStack item) {}

    @Override
    public AbstractMenu previousMenu() {
        return MenuCommand.getInstance().getMainMenu();
    }

    @Getter @Setter
    private static class HardwareData {
        String cpuName, cpuMicroarchitecture, memoryType;
        long cpuVendorFreq, totalMemory, pageSize;
        int logicalCores, physicalCores, diskCount, partitionCount, networkInterfaceCount;
    }
}

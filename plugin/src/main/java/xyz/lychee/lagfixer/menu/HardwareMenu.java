package xyz.lychee.lagfixer.menu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OSFileStore;
import oshi.util.FormatUtil;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.commands.MenuCommand;
import xyz.lychee.lagfixer.objects.AbstractMenu;
import xyz.lychee.lagfixer.utils.ItemBuilder;
import xyz.lychee.lagfixer.utils.TimingUtil;

import java.io.*;
import java.util.List;

public class HardwareMenu extends AbstractMenu {
    // 汉化文本Network:
    private final ItemBuilder i1 = this.skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTJiMTcxMmI5MDdjZTZiMTQwMmVhYWMyOGVjMjRhNGQ5NTU2OGY0YWI4N2U1OTc5ODBjMTViMjJiYmJkN2E1In19fQ==", "&b\uD83C\uDF10 &f&l网络：");
    // 汉化文本Processor:
    private final ItemBuilder i2 = this.skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDU2Yzk0NjE5MDMxMjMxNjhjZTY2N2VhZDdlYTU2YTUxNjEzMDk3MDQ5YmE2NDc4MzJiMzcyMmFmZmJlYjYzNiJ9fX0=", "&9\uD83D\uDCBE &f&l处理器：");
    // 汉化文本Memory:
    private final ItemBuilder i3 = this.skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjYwYjAwNGYzNjBlMjg4NTVjY2YxMjM1YzJiZGVhMGEyOTk3YjBiYzAzMjU4ZTJkYzI0YWI4YTI1NzBhZWE2In19fQ==", "&a\uD83C\uDF9E &f&l内存：");
    // 汉化文本Disk Storage:
    private final ItemBuilder i4 = this.skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjg5MWFmZDM5ZTJlNjczOGJjNmE4Yzg4YzI0OWZkYmNmNGE0NWM0YTI0MjQ3ZjFkMTBiYWUwYzY0ZDk5OTFlMSJ9fX0=", "&e\uD83D\uDCC2 &f&l磁盘存储：");

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final SystemInfo si;
    private final HardwareAbstractionLayer hal;
    private HardwareData hardwareData;
    private long lastNetworkUpdate = 0;
    private long prevBytesSent = 0;
    private long prevBytesRecv = 0;

    public HardwareMenu(LagFixer plugin, int size, String title) {
        super(plugin, size, title, 3, true);
        this.si = new SystemInfo();
        this.hal = this.si.getHardware();

        this.loadOrCreateHardwareData(plugin);

        this.surroundInventory();
        this.fillButtons();
        this.getInv().setItem(11,
                ItemBuilder.createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWY0NDZhOGY5Mjg0YzYyY2Y4ZDQ5MWZiZGIzMzhmZDM5ZWJiZWJlMzVlOTU5YzJmYzRmNzg2YzY3NTIyZWZiIn19fQ==")
                        // 汉化文本PERFORMANCE WARNING!
                        .setName("&4⚠ &c&l性能警告！")
                        .setLore(
                                // 汉化文本Heavy impact on servers with
                                " &8{*} &7对 &c<4 &7CPU 线程的服务器影响较大",
                                // 汉化文本Don't leave this GUI open indefinitely
                                " &8{*} &7请不要长时间一直打开该界面",
                                " ",
                                // 汉化文本Asynchronous updates
                                "&a✔ 异步更新 &8(&7非阻塞&8)",
                                // 汉化文本Auto-closes after inactivity
                                "&a✔ 闲置后自动关闭"
                        ).build()
        );
        this.getInv().setItem(size - 1, ConfigMenu.getBack());
        this.fillInventory();
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
        // 汉化文本Unknown
        this.hardwareData.setMemoryType(memory.getPhysicalMemory().isEmpty() ? "未知" : memory.getPhysicalMemory().get(0).getMemoryType());
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

    private ItemBuilder skull(String textureHash, String name) {
        // 汉化文本Loading lore...
        return ItemBuilder.createSkull(textureHash).setName(name).setLore(" &8{*} &7正在加载...");
    }

    @Override
    public void update() {
        TimingUtil timing = TimingUtil.startNew();

        try {
            List<NetworkIF> networks = hal.getNetworkIFs();
            long bytesSent = 0;
            long bytesRecv = 0;
            for (NetworkIF net : networks) {
                net.updateAttributes();
                bytesSent += net.getBytesSent();
                bytesRecv += net.getBytesRecv();
            }

            this.i1.setLore(
                    // 汉化文本Interfaces:
                    " &8{*} &7网卡数量：&f" + this.hardwareData.getNetworkInterfaceCount(),
                    // 汉化文本Sent:
                    " &8{*} &7发送：&f" + FormatUtil.formatBytes(bytesSent),
                    // 汉化文本Received:
                    " &8{*} &7接收：&f" + FormatUtil.formatBytes(bytesRecv),
                    // 汉化文本Throughput:
                    " &8{*} &7吞吐：&f" + calculateNetworkSpeed(bytesSent, bytesRecv),
                    // 汉化文本Network data updated in
                    "&7网络数据更新耗时 &e" + timing.stop().getExecutingTime() + "&7ms"
            );
        } catch (Throwable t) {
            this.i1.setLore(
                    // 汉化文本An error occurred while
                    " &8{*} &7获取网络信息时发生错误",
                    // 汉化文本retrieving network information :(
                    "&7请检查依赖/权限 : ("
            );
        }

        timing.start();
        try {
            this.i2.setLore(
                    // 汉化文本Model:
                    " &8{*} &7型号：&f" + this.hardwareData.getCpuName(),
                    // 汉化文本Logical cores:
                    " &8{*} &7逻辑核心：&f" + this.hardwareData.getLogicalCores(),
                    // 汉化文本Physical cores:
                    " &8{*} &7物理核心：&f" + this.hardwareData.getPhysicalCores(),
                    // 汉化文本Microarchitecture:
                    " &8{*} &7微架构：&f" + this.hardwareData.getCpuMicroarchitecture(),
                    // 汉化文本Frequency:
                    " &8{*} &7频率：&f" + FormatUtil.formatHertz(this.hardwareData.getCpuVendorFreq()),
                    // 汉化文本Cpu data updated in
                    "&7CPU 数据更新耗时 &e" + timing.stop().getExecutingTime() + "&7ms"
            );
        } catch (Throwable t) {
            this.i2.setLore(
                    // 汉化文本An error occurred while
                    " &8{*} &7获取处理器信息时发生错误",
                    // 汉化文本retrieving processer information :(
                    "&7请检查依赖/权限 : ("
            );
        }

        timing.start();
        try {
            GlobalMemory memory = hal.getMemory();
            long usedMem = this.hardwareData.getTotalMemory() - memory.getAvailable();
            double memPercent = (usedMem * 100.0) / this.hardwareData.getTotalMemory();
            VirtualMemory swap = memory.getVirtualMemory();

            this.i3.setLore(
                    // 汉化文本Total RAM:
                    " &8{*} &7总内存：&f" + FormatUtil.formatBytesDecimal(this.hardwareData.getTotalMemory()),
                    // 汉化文本Used RAM:
                    " &8{*} &7已用内存：&f" + FormatUtil.formatBytesDecimal(usedMem) +
                            String.format(" (&f%.1f%%&7)", memPercent),
                    // 汉化文本Available RAM:
                    " &8{*} &7可用内存：&f" + FormatUtil.formatBytesDecimal(memory.getAvailable()),
                    // 汉化文本Page Size:
                    " &8{*} &7页大小：&f" + FormatUtil.formatBytesDecimal(this.hardwareData.getPageSize()),
                    // 汉化文本Memory Type:
                    " &8{*} &7内存类型：&f" + this.hardwareData.getMemoryType(),
                    // 汉化文本Swap Total:
                    " &8{*} &7交换空间总量：&f" + FormatUtil.formatBytesDecimal(swap.getSwapTotal()),
                    // 汉化文本Swap Used:
                    " &8{*} &7交换空间已用：&f" + FormatUtil.formatBytesDecimal(swap.getSwapUsed()),
                    // 汉化文本Memory data updated in
                    "&7内存数据更新耗时 &e" + timing.stop().getExecutingTime() + "&7ms"
            );
        } catch (Throwable t) {
            this.i3.setLore(
                    // 汉化文本An error occurred while
                    " &8{*} &7获取内存信息时发生错误",
                    // 汉化文本retrieving memory information :(
                    "&7请检查依赖/权限 : ("
            );
        }

        timing.start();
        try {
            long totalReadBytes = 0;
            long totalWriteBytes = 0;
            long readOps = 0;
            long writeOps = 0;
            long queueLength = 0;

            for (HWDiskStore disk : hal.getDiskStores()) {
                disk.updateAttributes();
                totalReadBytes += disk.getReadBytes();
                totalWriteBytes += disk.getWriteBytes();
                readOps += disk.getReads();
                writeOps += disk.getWrites();
                queueLength += disk.getCurrentQueueLength();
            }

            long totalDiskSpace = 0;
            long usedDiskSpace = 0;
            for (OSFileStore fs : si.getOperatingSystem().getFileSystem().getFileStores()) {
                totalDiskSpace += fs.getTotalSpace();
                usedDiskSpace += fs.getTotalSpace() - fs.getUsableSpace();
            }
            double diskUsagePercent = totalDiskSpace > 0 ?
                    (usedDiskSpace * 100.0) / totalDiskSpace : 0;

            this.i4.setLore(
                    // 汉化文本Physical Disks:
                    " &8{*} &7物理磁盘：&f" + this.hardwareData.getDiskCount(),
                    // 汉化文本Partitions:
                    " &8{*} &7分区数：&f" + this.hardwareData.getPartitionCount(),
                    // 汉化文本Total Capacity:
                    " &8{*} &7总容量：&f" + FormatUtil.formatBytes(totalDiskSpace),
                    // 汉化文本Used Space:
                    " &8{*} &7已用空间：&f" + FormatUtil.formatBytes(usedDiskSpace) +
                            String.format(" (&f%.1f%%&7)", diskUsagePercent),
                    // 汉化文本Read Data:
                    " &8{*} &7读取数据：&f" + FormatUtil.formatBytes(totalReadBytes) +
                            " (&f" + readOps + " 次&7)",
                    // 汉化文本Written Data:
                    " &8{*} &7写入数据：&f" + FormatUtil.formatBytes(totalWriteBytes) +
                            " (&f" + writeOps + " 次&7)",
                    // 汉化文本Disk Queues:
                    " &8{*} &7磁盘队列：&f" + queueLength,
                    // 汉化文本Storage data updated in
                    "&7存储数据更新耗时 &e" + timing.stop().getExecutingTime() + "&7ms"
            );
        } catch (Throwable t) {
            this.i4.setLore(
                    // 汉化文本An error occurred while
                    " &8{*} &7获取存储信息时发生错误",
                    // 汉化文本retrieving storage information :(
                    "&7请检查依赖/权限 : ("
            );
        }

        this.fillButtons();
    }

    private String calculateNetworkSpeed(long currentSent, long currentRecv) {

        long timeDiff = System.currentTimeMillis() - lastNetworkUpdate;
        if (lastNetworkUpdate == 0 || timeDiff == 0) {
            lastNetworkUpdate = System.currentTimeMillis();
            prevBytesSent = currentSent;
            prevBytesRecv = currentRecv;
            // 汉化文本Calculating...
            return "计算中...";
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

    @Getter
    @Setter
    private static class HardwareData {
        String cpuName;
        String cpuMicroarchitecture;
        long cpuVendorFreq;
        int logicalCores;
        int physicalCores;
        long totalMemory;
        long pageSize;
        String memoryType;
        int diskCount;
        int partitionCount;
        int networkInterfaceCount;
    }
}
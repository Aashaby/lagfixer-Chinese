package xyz.lychee.lagfixer.commands;

import lombok.Data;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.ErrorsManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractMonitor;
import xyz.lychee.lagfixer.utils.MessageUtils;
import xyz.lychee.lagfixer.utils.TimingUtil;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class BenchmarkCommand extends CommandManager.Subcommand {
    private volatile boolean benchmark = false;

    public BenchmarkCommand(CommandManager commandManager) {
        // 汉化文本run benchmark and compare it with other servers
        super(commandManager, "benchmark", "运行基准测试并与其他服务器对比", "test");
    }

    @Override
    public void load() {}

    @Override
    public void unload() {}

    @Override
    public boolean execute(@NotNull org.bukkit.command.CommandSender sender, @NotNull String[] args) {
        if (this.benchmark) {
            // 汉化文本Benchmark is running, wait for results in console!
            return MessageUtils.sendMessage(true, sender, "&7基准测试正在运行，请在控制台等待结果！");
        }

        AbstractMonitor monitor = SupportManager.getInstance().getMonitor();
        if (monitor.getMspt() > 10.0) {
            // 汉化文本Server MSPT is too
            return MessageUtils.sendMessage(true, sender, "&7服务器 MSPT 过&c高&7，测试结果可能不准确！");
        }

        long availableRam = monitor.getRamFree() + (monitor.getRamMax() - monitor.getRamTotal());
        if (availableRam < 2048) {
            // 汉化文本Server available RAM is too low, you need
            return MessageUtils.sendMessage(true, sender, "&7服务器可用内存过低，你需要 &c" + availableRam + "&8/&c2048MB");
        }

        BukkitTask task = SupportManager.getInstance().getFork().runTimer(true, () -> {
            if (this.benchmark) {
                // 汉化文本Async benchmark in progress, wait for results...
                MessageUtils.sendMessage(true, sender, "&7异步基准测试进行中，请等待结果...");
            }
        }, 1, 2, TimeUnit.SECONDS);

        this.benchmark = true;
        Thread thread = new Thread(() -> {
            try {
                TimingUtil t = TimingUtil.startNew();
                System.gc();

                Benchmark b = this.runBenchmarks(10, 20, 100_000_000, 10);

                task.cancel();

                String result = b.getResult().toString();
                ErrorsManager.getInstance().sendBenchmark(b);

                // 汉化文本Benchmark done in
                MessageUtils.sendMessage(true, sender, "&7基准测试完成，用时 &f" + t.stop().getExecutingTime() + "&7ms，结果：&f" + result);
                this.getCommandManager().getPlugin().getLogger().info(result);
            } catch (Exception e) {
                // 汉化文本Benchmark error:
                MessageUtils.sendMessage(true, sender, "&c基准测试错误：" + e.getMessage());
            }
            this.benchmark = false;
        });

        thread.setName("LagFixer Benchmark");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();

        return true;
    }

    public Benchmark runBenchmarks(int warmup, int cpu, int arrayLength, int memoryPasses) {
        Benchmark benchmark = new Benchmark(cpu);

        // 汉化文本LagFixer Advanced CPU Benchmark
        benchmark.getResult().append("\n \n&8&m    &r&8[ &eLagFixer 高级 CPU 基准测试 &8]&m    &r\n ");
        for (int i = 0; i < warmup; i++) {
            cpuTest(1_000_000);
        }
        double totalScore = 0;
        long bestScore = Long.MAX_VALUE;
        long worstScore = Long.MIN_VALUE;
        double checksum = 0;

        for (int i = 0; i < cpu; i++) {
            long startTime = System.nanoTime();
            double result = cpuTest(10_000_000);
            long duration = System.nanoTime() - startTime;

            double score = 10_000_000_000.0 / duration;
            benchmark.scores[i] = score;
            totalScore += score;
            bestScore = Math.min(bestScore, duration);
            worstScore = Math.max(worstScore, duration);
            checksum += result;
        }

        benchmark.getResult()
                // 汉化文本Average performance:
                .append("\n &8• &f平均性能：&e").append(totalScore / cpu).append(" Gop/s")
                // 汉化文本Best time:
                .append("\n &8• &f最佳耗时：&e").append(bestScore / 1_000_000_000D).append(" 秒")
                // 汉化文本Worst time:
                .append("\n &8• &f最差耗时：&e").append(worstScore / 1_000_000_000D).append(" 秒");

        benchmark.setCpu_checksum(checksum);
        benchmark.setTotalScore(totalScore / cpu);
        benchmark.setBestScore(bestScore);
        benchmark.setWorstScore(worstScore);

        // RAM Benchmark
        // 汉化文本LagFixer Advanced RAM Benchmark
        benchmark.getResult().append("\n \n&8&m    &r&8[ &eLagFixer 高级 内存（RAM）基准测试 &8]&m    &r\n ");

        long[] array = new long[arrayLength];
        int[] randomIndices = new int[arrayLength];
        Random rand = new Random(2137);

        for (int i = 0; i < arrayLength; i++) {
            randomIndices[i] = rand.nextInt(arrayLength);
        }

        // Sequential Write
        long writeTime = 0;
        for (int pass = 0; pass < memoryPasses; pass++) {
            long start = System.nanoTime();
            for (int i = 0; i < arrayLength; i++) {
                array[i] = i + pass;
            }
            writeTime += System.nanoTime() - start;
        }
        double writeSpeed = (arrayLength * 4D * memoryPasses) / (1024D * 1024D) / (writeTime / 1_000_000_000D);
        // 汉化文本Sequential write:
        benchmark.getResult().append(String.format("\n &8• &f顺序写入：&e%.2f MB/s", writeSpeed));
        benchmark.setWriteSpeed(writeSpeed);

        // Sequential Read
        long readTime = 0;
        long readChecksum = 0;
        for (int pass = 0; pass < memoryPasses; pass++) {
            long start = System.nanoTime();
            for (int i = 0; i < arrayLength; i++) {
                readChecksum += array[i];
            }
            readTime += System.nanoTime() - start;
        }
        double readSpeed = (arrayLength * 4D * memoryPasses) / (1024D * 1024D) / (readTime / 1_000_000_000D);
        // 汉化文本Sequential read:
        benchmark.getResult().append(String.format("\n &8• &f顺序读取：&e%.2f MB/s", readSpeed));
        benchmark.setReadSpeed(readSpeed);

        // Random Access
        long randomTime = 0;
        long randomChecksum = 0;
        for (int pass = 0; pass < memoryPasses; pass++) {
            long start = System.nanoTime();
            for (int i = 0; i < arrayLength; i++) {
                randomChecksum += array[randomIndices[i]];
            }
            randomTime += System.nanoTime() - start;
        }
        double randomSpeed = (arrayLength * 4D * memoryPasses) / (1024D * 1024D) / (randomTime / 1_000_000_000D);
        // 汉化文本Random access:
        benchmark.getResult().append(String.format("\n &8• &f随机访问：&e%.2f MB/s\n ", randomSpeed));
        benchmark.setRandomSpeed(randomSpeed);

        return benchmark;
    }

    private double cpuTest(int iterations) {
        double sum = 0;
        for (int i = 1; i <= iterations; i++) {
            sum += Math.sqrt(i);
            sum -= Math.sin(i);
            sum *= Math.cos(i);
            sum /= Math.log(i + 1);
        }
        return sum;
    }

    @Data
    public static class Benchmark {
        //Cpu benchmark
        private final double[] scores;
        private StringBuilder result = new StringBuilder();
        private double cpu_checksum;
        private double bestScore;
        private double worstScore;
        private double totalScore;

        //Memory benchmark
        private double memory_checksum;
        private double writeSpeed;
        private double readSpeed;
        private double randomSpeed;

        //Compression benchmark
        private double compressionSpeed;
        private double decompressionSpeed;

        public Benchmark(int cpu) {
            this.scores = new double[cpu];
        }
    }
}
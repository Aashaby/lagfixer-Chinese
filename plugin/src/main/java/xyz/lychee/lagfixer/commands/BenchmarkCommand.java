package xyz.lychee.lagfixer.commands;

import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.ErrorsManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.ResourceMonitor;
import xyz.lychee.lagfixer.utils.MessageUtils;
import xyz.lychee.lagfixer.utils.TimingUtil;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class BenchmarkCommand extends CommandManager.Subcommand {
    private volatile boolean benchmark = false;

    public BenchmarkCommand(CommandManager commandManager) {
        super(commandManager, "benchmark", "run benchmark and compare it with other servers", "test");
    }

    @Override
    public void load() {
    }

    @Override
    public void unload() {
    }

    @Override
    public boolean execute(@NotNull org.bukkit.command.CommandSender sender, @NotNull String[] args) {
        if (this.benchmark) {
            Component msg = Language.getMainValue("benchmark_running", true);
            if (msg != null) this.getCommandManager().getPlugin().getAudiences().sender(sender).sendMessage(msg);
            return true;
        }

        ResourceMonitor monitor = SupportManager.getInstance().getResourceMonitor();
        if (monitor.getMspt() > 10.0) {
            Component msg = Language.getMainValue("benchmark_high_mspt", true);
            if (msg != null) this.getCommandManager().getPlugin().getAudiences().sender(sender).sendMessage(msg);
            return true;
        }

        long availableRam = monitor.getRamFree() + (monitor.getRamMax() - monitor.getRamTotal());
        if (availableRam < 2048) {
            Component msg = Language.getMainValue("benchmark_low_ram", true,
                    Placeholder.unparsed("available", Long.toString(availableRam)));
            if (msg != null) this.getCommandManager().getPlugin().getAudiences().sender(sender).sendMessage(msg);
            return true;
        }

        BukkitTask task = SupportManager.getInstance().getFork().runTimer(true, () -> {
            if (this.benchmark) {
                Component progress = Language.getMainValue("benchmark_async_progress", true);
                if (progress != null)
                    this.getCommandManager().getPlugin().getAudiences().sender(sender).sendMessage(progress);
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

                Component doneMsg = Language.getMainValue("benchmark_done", true,
                        Placeholder.unparsed("time", t.stop().toString()),
                        Placeholder.unparsed("result", result));
                if (doneMsg != null)
                    this.getCommandManager().getPlugin().getAudiences().sender(sender).sendMessage(doneMsg);
                this.getCommandManager().getPlugin().getLogger().info(result);
            } catch (Exception e) {
                Component errorMsg = Language.getMainValue("benchmark_error", true,
                        Placeholder.unparsed("error", e.getMessage()));
                if (errorMsg != null)
                    this.getCommandManager().getPlugin().getAudiences().sender(sender).sendMessage(errorMsg);
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

        // CPU header
        Component cpuHeader = Language.getMainValue("benchmark_cpu_header", false);
        if (cpuHeader != null)
            benchmark.getResult().append(Language.getSerializer().serialize(cpuHeader));

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

        // 平均性能
        Component avgComp = Language.getMainValue("benchmark_average", false,
                Placeholder.unparsed("score", String.format("%.2f", totalScore / cpu)));
        if (avgComp != null) benchmark.getResult().append("\n &8• ").append(Language.getSerializer().serialize(avgComp));

        Component bestComp = Language.getMainValue("benchmark_best", false,
                Placeholder.unparsed("time", String.format("%.3f", bestScore / 1_000_000_000D)));
        if (bestComp != null) benchmark.getResult().append("\n &8• ").append(Language.getSerializer().serialize(bestComp));

        Component worstComp = Language.getMainValue("benchmark_worst", false,
                Placeholder.unparsed("time", String.format("%.3f", worstScore / 1_000_000_000D)));
        if (worstComp != null) benchmark.getResult().append("\n &8• ").append(Language.getSerializer().serialize(worstComp));

        benchmark.setCpu_checksum(checksum);
        benchmark.setTotalScore(totalScore / cpu);
        benchmark.setBestScore(bestScore);
        benchmark.setWorstScore(worstScore);

        // RAM Benchmark
        Component ramHeader = Language.getMainValue("benchmark_ram_header", false);
        if (ramHeader != null) benchmark.getResult().append(Language.getSerializer().serialize(ramHeader));

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
        double writeSpeed = (arrayLength * 4L * memoryPasses) / (1024D * 1024D) / (writeTime / 1_000_000_000D);
        Component writeComp = Language.getMainValue("benchmark_write_speed", false,
                Placeholder.unparsed("speed", String.format("%.2f", writeSpeed)));
        if (writeComp != null) benchmark.getResult().append("\n &8• ").append(Language.getSerializer().serialize(writeComp));
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
        double readSpeed = (arrayLength * 4L * memoryPasses) / (1024D * 1024D) / (readTime / 1_000_000_000D);
        Component readComp = Language.getMainValue("benchmark_read_speed", false,
                Placeholder.unparsed("speed", String.format("%.2f", readSpeed)));
        if (readComp != null) benchmark.getResult().append("\n &8• ").append(Language.getSerializer().serialize(readComp));
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
        double randomSpeed = (arrayLength * 4L * memoryPasses) / (1024D * 1024D) / (randomTime / 1_000_000_000D);
        Component randomComp = Language.getMainValue("benchmark_random_speed", false,
                Placeholder.unparsed("speed", String.format("%.2f", randomSpeed)));
        if (randomComp != null) benchmark.getResult().append("\n &8• ").append(Language.getSerializer().serialize(randomComp));
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
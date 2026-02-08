![logo](https://i.imgur.com/hElpNHD.png)

**LagFixer** 是一款终极的 Minecraft 性能优化插件，旨在优化服务器并消除不必要的卡顿。通过对服务器各个方面进行精细调优并精简冗余功能，LagFixer 能为所有玩家带来更流畅、更愉悦的游戏体验。

## 运行要求：
- Java 8 或更高版本
- 服务端版本 1.16.5 - 1.21.11

## 支持版本：
- 1.16.5, 1.17.1, 1.18.2, 1.19.4, 1.20-1.21.11
- 大多数模块支持更宽的版本范围 [1.16.5 - 1.21.11]
- 基于 Forge 的 Spigot 分支： [Mohist](https://mohistmc.com/)、[Arclight](https://github.com/IzzelAliz/Arclight) 等

<details>
<summary>支持的插件</summary>
  
- [PlaceholderAPI](https://www.spigotmc.org/resources/6245/)
- [WildStacker](https://bg-software.com/wildstacker/)
- [UltimateStacker](https://songoda.com/product/16)
- [RoseStacker](https://www.spigotmc.org/resources/82729/)
- [LevelledMobs](https://www.spigotmc.org/resources/74304/)
- [Spark](https://spark.lucko.me/download)
  
</details>

<details>
<summary>占位符（Placeholders）</summary>

- %lagfixer_tps% - 当前 TPS（ticks per second）
- %lagfixer_mspt% - 当前 MSPT（milliseconds per tick）
- %lagfixer_cpuprocess% - 当前进程 CPU 占用
- %lagfixer_cpusystem% - 当前系统 CPU 占用
- %lagfixer_worldcleaner% - 世界清理倒计时

</details>

<details>
<summary>命令</summary>

- /lagfixer - 插件主命令
- /abyss - 被清理物品进入的“深渊”仓库

</details>

## 下载：
- [Modrinth](https://modrinth.com/plugin/lagfixer) - （推荐从这里获取最新版本）
- [SpigotMC](https://www.spigotmc.org/resources/lagfixer-1-13-1-21-10-%E2%9A%A1%EF%B8%8Fbest-performance-solution-%EF%B8%8F-2100-servers-%E2%9C%85-lightweight-and-asynchronous.111684/)

## 模块：
### ⭐ MobAiReducer：（性能影响：VERY HIGH）
- 替换生物移动逻辑以优化并减少不必要的行为。
- 解决默认动物行为导致的低效问题，例如无意义的随机移动或频繁转头。
- 通过禁用不必要的 PathFinders 或替换为更高效的实现来进行优化。
- 在生物数量很多的场景中非常关键，因为哪怕细微的移动也会占用服务器资源。

### ⭐ ItemsCleaner（性能影响：MEDIUM）
- 清理地面上的旧物品，以提升服务器性能。
- 物品随时间积累会导致服务器卡顿，尤其是在高人口或活跃服务器中。
- 及时移除多余物品以减轻服务器负担。
- 玩家可以使用 `/abyss` 命令从“深渊”仓库取回被清理的物品。

### ⭐ EntityLimiter（性能影响：HIGH）
- 限制每个区块的实体数量。
- 对拥有大型刷怪/养殖场的生存服非常重要。
- 防止实体过度堆积以及由此带来的性能问题。
- 即使在高实体密度环境下也能保持性能稳定。

### ⭐ LagShield（性能影响：HIGH）
- 监控服务器负载，并在延迟突增时动态调整设置。
- 针对服务器性能波动进行处理，以缓解延迟与卡顿。
- 动态调整参数、禁用不必要功能并优化资源使用。
- 通过降低性能波动带来的影响，保障游戏体验更平滑。

### ⭐ RedstoneLimiter（性能影响：MEDIUM）
- 禁用高负载的红石时钟以防止服务器过载。
- 某些红石结构会导致性能下降甚至崩服。
- 启用 AntiRedstone 可提升服务器稳定性并保持响应速度。
- 即便存在复杂红石装置，也能更好地保证游戏不中断。

### ⭐ ExplosionOptimizer（性能影响：HIGH）
- 限制爆炸强度并防止连锁反应，以减少卡顿与破坏。
- 适用于 TNT/苦力怕/末影水晶使用频繁的服务器。
- 防止过量爆炸导致性能问题。
- 在控制破坏性事件的同时保持服务器性能稳定。

### ⭐ ConsoleFilter（性能影响：VISUAL ONLY）
- 根据预设规则过滤控制台消息。
- 通过选择性显示重要信息提升清晰度。
- 减少刷屏，提高多人服务器的日志可读性。
- 提升服务器管理效率，并改善管理员与玩家的使用体验。

### ⭐ VehicleMotionReducer（性能影响：MEDIUM）
- 优化所有载具，例如船与矿车。
- 移除在废弃矿井中生成的箱子矿车。
- 在矿车使用频繁的服务器上尤其有用。
- 通过优化载具机制并移除不必要实体来提升服务器性能。

### ⭐ AbilityLimiter（性能影响：MEDIUM）
 - 限制三叉戟与鞘翅的高频使用，防止区块加载过量。
 - 频繁的高速移动可能导致服务器卡顿与不稳定。
 - 允许调整减速幅度，以平衡性能与玩家体验。
 - 启用后世界加载更平滑、服务器更稳定，并对移动能力进行合理限制。

## 统计（bStats）：
![bStats:](https://bstats.org/signatures/bukkit/LagFixer.svg)


# 其它插件：
[![gatekeeper](https://i.imgur.com/YHGjHR4.png)](https://modrinth.com/plugin/gatekeeper-mc)

[![dynamicdns](https://i.imgur.com/BikoONq.png)](https://modrinth.com/plugin/dynamicdns)

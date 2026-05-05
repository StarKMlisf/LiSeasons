# LISeasons 1.0.4 更新说明

本文档对应版本：`LISeasons 1.0.4`

本次更新重点修复冬季天气接管、融雪可见效果和性能采样中暴露的热路径问题。

## 1. 天气机制调整

### 不再影响 `/weather`

从 `1.0.4` 开始，插件不再拦截 Bukkit 天气事件。

- 管理员执行 `/weather clear` 不会被插件取消。
- 管理员执行 `/weather rain` 不会被插件改回晴天。
- 管理员执行 `/weather thunder` 不会被插件取消雷暴。
- 冬季不再强制 `setStorm(true)`，不会出现“一直下雪”的情况。

### 冬季下雪说明

Minecraft 原版中，下雪依赖世界处于 `storm` 天气，并且当前位置群系支持降雪。

因此现在的逻辑是：

- 插件不强制天气。
- 如果服务器自然下雨，或管理员执行 `/weather rain`，冬季雪地类群系会按原版表现下雪。
- 如果管理员执行 `/weather clear`，插件不会阻止清天。

## 2. 融雪机制优化

### 融雪速度提高

`1.0.3` 起已提高融雪速度，`1.0.4` 保留该优化。

默认效果：

- 玩家周边可见融雪扫描量从约 `28` 列提升到约 `96` 列。
- 后台区块融雪队列重新扫描冷却从约 `20 秒` 降到约 `6 秒`。
- 默认配置 `spring-melt-chance-percent` 从 `30` 提高到 `60`。

### 融雪范围

融雪只在非冬季执行。

会处理的方块：

- `SNOW`
- `SNOW_BLOCK`
- `POWDER_SNOW`
- `ICE`
- `FROSTED_ICE`
- 带 `snowy=true` 状态的草方块等可覆雪方块

默认不会处理的区域：

- `ocean`
- `river`

这些由配置项控制：

```yml
season-world-rules:
  water-cycle:
    exempt-biomes:
      - ocean
      - river
```

## 3. 性能优化

### 缓存 Folia 检测

性能采样中出现：

```text
PlatformUtil.isFolia()
Class.forName()
ClassLoader.loadClass()
```

原因是旧版本每次任务调度都会重新检测 Folia 环境。

`1.0.4` 已改为首次检测后缓存结果，避免在实时时钟、视觉效果、融雪任务中反复执行类加载检查。

### 实时时钟服务降耗

当配置为：

```yml
time:
  sync-real-time: false
```

旧版本仍会每秒遍历世界并尝试恢复昼夜循环。

`1.0.4` 已改为：

- 关闭实时同步时，只恢复一次 `DO_DAYLIGHT_CYCLE`。
- 恢复完成后不再每秒重复遍历世界。
- 重新开启实时同步时，会正常恢复同步逻辑。

## 4. 配置变更

### 推荐调整旧配置

如果服务器已经生成过旧版 `config.yml`，Bukkit 不会自动覆盖已有配置。

建议手动检查并调整：

```yml
season-world-rules:
  water-cycle:
    spring-melt-chance-percent: 60
```

如果希望更快融雪，可以继续提高，例如：

```yml
spring-melt-chance-percent: 80
```

不建议长期设置过高到 `100`，除非服务器人数较少或你确认 TPS 压力可接受。

## 5. 升级方式

1. 停止服务器。
2. 备份旧插件 Jar 和 `plugins/LISeasons/` 配置目录。
3. 替换为新 Jar：

```text
target/LISeasons-1.0.4.jar
```

4. 如服务器已有旧配置，手动合并 `spring-melt-chance-percent` 配置。
5. 启动服务器。
6. 使用 profiler 或 spark 观察：

```text
PlatformUtil.isFolia()
RealTimeClockService.restoreDaylightCycle()
```

这两项不应再持续出现在高占比热路径中。

## 6. 已知说明

- `DO_DAYLIGHT_CYCLE` 在当前 Paper API 中有过时警告，但仍可编译通过并正常使用。
- 冬季是否显示雪取决于原版天气和群系条件，插件不再强制改变天气。
- 融雪是分批进行，不会瞬间清空全世界积雪，目的是兼顾可见效果和 TPS。

## 7. 版本记录

### 1.0.4

- 缓存 Folia 环境检测，减少 `Class.forName()` 热路径开销。
- 实时时钟关闭时只恢复一次昼夜循环。
- 保留 `/weather` 原生命令控制权。

### 1.0.3

- 提高玩家周边可见融雪速度。
- 提高默认融雪概率到 `60`。
- 缩短后台融雪队列重扫冷却。

### 1.0.2

- 修复冬季一直下雪问题。
- 天气监听不再取消管理员天气指令。
- 加入玩家周边可见融雪批次。


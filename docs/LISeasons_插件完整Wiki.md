# LISeasons 插件 Wiki

适用版本：`1.0`

LISeasons 是一个面向 Paper / Folia 生存服的四季与二十四节气插件。插件目标不是硬核惩罚，而是让玩家能自然感受到季节、天气、温度、草木颜色和世界环境的变化。

## 核心功能

- 四季轮转：春季、夏季、秋季、冬季
- 二十四节气：按现实日期或游戏天数推进
- 季节天气：不同季节有不同雨雪、雷暴倾向
- 季节色调：草地、树叶、水体、天空按季节变化
- 温度系统：空气温度与玩家体温分离计算
- 冬冻春融：冬季结冰，春季分批化雪去冰
- 季节生物：冬季/夏季可替换部分自然生成生物
- 日历菜单：游戏内查看季节、节气、温度和全年节气
- PlaceholderAPI：为 HUD、记分板、菜单提供占位符
- Folia 兼容：涉及玩家和区块的任务使用对应调度方式

## 指令

主指令：

```text
/seasons
/season
```

常用指令：

```text
/seasons help
/seasons info [world]
/seasons calendar
/seasons reload
/seasons set season <world> <spring|summer|autumn|winter>
/seasons set term <world> <solarTermKey>
/seasons next <world>
/seasons auto <world>
```

权限：

```text
liseasons.player  基础查询和菜单
liseasons.admin   管理指令
```

## 配置文件

```text
config.yml        主配置、世界开关、时间同步、视觉特效、世界规则
temperature.yml   温度系统
season-color.yml  季节色调和动态 biome 染色
messages.yml      消息文本
```

已存在的配置文件不会被 Bukkit 自动覆盖。更新插件后，如果新功能无效，优先检查旧配置里是否缺少新字段。

## 时间同步

配置位置：

```yaml
time:
  sync-real-time: false
```

默认关闭。  
开启后，插件会关闭原版昼夜循环，并把游戏时间同步到现实时间。

关闭时，插件会自动恢复：

```text
DO_DAYLIGHT_CYCLE = true
```

这用于避免旧版本开启同步后，再关闭时昼夜仍然停住。

## 温度系统

温度系统分为两层：

```text
空气温度：由季节、节气、昼夜、天气、群系、海拔决定
玩家体温：逐步趋近空气温度，并受水、装备、手持物品、附近方块影响
```

主要机制：

- 每日随机基础气温
- 二十四节气温度修正
- 正午升温、午夜降温的昼夜曲线
- 雨天和雷暴降温
- 高海拔更冷
- 入水增加潮湿值，离水后逐步恢复
- 冬季入水更危险
- 盔甲按材质提供不同保暖值
- 手持物品可配置升温/降温
- 普通火源升温，灵魂火和冰类降温

关键配置：

```yaml
base-temperature:
  mode: daily-random

solar-term-modifier:
  enable: true

condition:
  night-min: -3.0
  day-max: 2.0
  rain: -2.5
  thunder: -4.0
  high-altitude-start-y: 120.0
  high-altitude: -3.0
```

## 温度事件

温度事件支持：

```text
mode: above / below
priority: 数字越大优先级越高
cooldown-seconds: 单个事件冷却
temperature-event-mode: highest-only / all
```

推荐使用：

```yaml
temperature-event-mode: "highest-only"
temperature-event-cooldown-seconds: 5
```

这样可以避免低温或高温时多个事件同时触发。

## 季节视觉

`season-color.yml` 控制玩家周围 biome 动态染色。

默认目标：

```yaml
biome-spoof:
  seasons:
    spring: "flower_forest"
    summer: "plains"
    autumn: "windswept_savanna"
    winter: "snowy_plains"
```

效果：

- 春季更鲜绿
- 夏季更浓绿
- 秋季偏黄/暖色
- 冬季偏白/冷色

色调刷新是分批处理，不会瞬间重绘全世界。

## 冬冻春融

配置位置：

```yaml
season-world-rules:
  water-cycle:
    enable: true
    winter-freeze-chance-percent: 35
    spring-melt-chance-percent: 30
    transition-melt-radius-chunks: 6
    transition-melt-budget-per-tick: 512
```

冬季：

- 露天静水可能结冰

春季：

- 日常随机融化冰雪
- 从冬季切换到春季时，会把在线玩家附近已加载区块加入融雪队列

会处理：

```text
ICE -> WATER
FROSTED_ICE -> WATER
SNOW -> AIR
SNOW_BLOCK -> AIR
POWDER_SNOW -> AIR
```

## PlaceholderAPI

安装 PlaceholderAPI 后自动注册。

占位符：

```text
%liseasons_season%              当前季节中文名
%liseasons_season_key%          当前季节键名
%liseasons_term%                当前节气中文名
%liseasons_term_key%            当前节气键名
%liseasons_world%               当前世界
%liseasons_mode%                自动/手动
%liseasons_temperature%         玩家体感温度
%liseasons_temperature_status%  温度状态
%liseasons_air_temperature%     空气温度
%liseasons_wetness%             潮湿度
```

## Folia 兼容说明

插件针对 Folia 做了以下处理：

- 玩家相关视觉、温度检测使用玩家调度器
- 季节状态按世界区域调度
- 春季融雪队列在 Folia 下投递到对应区块区域执行
- 时间同步在 Folia 下使用世界 spawn 区域执行

建议：

- 不要把区块刷新预算设置过大
- `transition-melt-budget-per-tick` 建议从 `512` 起步
- `biome-spoof.budget-chunks-per-tick` 建议 `4~12`

## 玩家说明

春季适合探索和种植。  
夏季沙漠、恶地、丛林更热。  
秋季更凉爽，树叶会逐步转暖色。  
冬季水域、高山、夜晚更危险。  

低温时靠近火源、穿保暖装备、避免下水。  
高温时远离热源和热带群系，寻找较凉爽区域。

## 常见问题

### 颜色为什么不是瞬间变化？

为了性能稳定，插件只处理玩家附近已加载区块，并按预算分批刷新。

### 关闭时间同步后昼夜还是停住？

`1.0` 已修复。关闭 `sync-real-time` 后插件会恢复原版昼夜循环。

### PlaceholderAPI 没有占位符？

确认：

```text
PlaceholderAPI 已安装
服务器已重启或执行 /seasons reload
占位符前缀是 liseasons
```

### 春天没有化雪？

确认：

```text
season-world-rules.water-cycle.enable: true
世界在 LISeasons 启用列表内
玩家附近区块是已加载区块
```

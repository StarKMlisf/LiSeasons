# LISeasons Wiki（0.1.43）

本文用于服务器服主和玩家，说明 `LISeasons 0.1.43` 的核心玩法、配置和常见问题。

## 1. 插件定位

LISeasons 是一个面向生存服的四季氛围插件，重点是：

- 四季与二十四节气轮转
- 草地/树叶季节色调变化
- 温度体感与环境互动
- 作物、生物、天气的季节联动

设计目标是“有体感、有氛围、不过度惩罚”。

## 2. 本版本重点更新（0.1.43）

### 温度系统升级

- 温度模型从“单次即时计算”升级为：
  - 空气温度（季节、节气、群系、天气决定）
  - 玩家体温（逐步趋近空气温度）
- 新增潮湿残留机制：
  - 入水后会变潮湿
  - 离水后按配置逐步恢复
  - 冬季入水仍危险，但不再一落水就极寒
- 新增每日随机基础气温（可开关）
- 新增二十四节气温度修正（可开关）
- 盔甲改为按材质提供不同保暖值
- 支持手持物品自定义温度修正（可做保暖/降温道具）

### 温度事件升级

- 支持事件触发模式 `mode`（`above` / `below`）
- 支持事件优先级 `priority`
- 支持全局冷却与单事件冷却
- 支持 `highest-only`：同一轮仅触发最严重事件，避免叠加刷屏

### 环境热源/冷源细化

- 普通火、营火、岩浆、岩浆块：升温
- 灵魂火、灵魂营火、灵魂火把：降温
- 冰、浮冰、蓝冰：降温

## 3. 玩家指南

## 3.1 温度怎么看

- 默认在 ActionBar 显示体温
- 危险温度会触发提示和事件效果

## 3.2 四季体感

- 春季：温和，适合探索和种植
- 夏季：偏热，热带群系风险上升
- 秋季：整体舒适，昼夜温差更明显
- 冬季：偏冷，水域风险显著提升

## 3.3 如何取暖

- 靠近火、营火、岩浆、岩浆块
- 选择保暖盔甲
- 冬季减少下水与长时间淋雨
- 进入地下或室内减少环境冷却

## 3.4 如何降温

- 远离沙漠、恶地、丛林等高温区域
- 远离岩浆和火源
- 利用较冷群系或冷源物品调节体温

## 4. 服主配置说明

## 4.1 主要文件

- `config.yml`：总开关、世界白名单、视觉特效等
- `temperature.yml`：温度系统专用配置
- `season-color.yml`：季节色调/群系视觉配置
- `messages.yml`：文本消息配置

## 4.2 重点温度配置（temperature.yml）

- `base-temperature.mode`
  - `daily-random`：每日随机基础温度
  - `fixed`：使用 `season-base-temperature`
- `solar-term-modifier.enable`
  - 是否启用节气温度修正
- `biome-vanilla-scale`
  - 原版群系温度权重，越高波动越大
- `water.wetness-recovery-*`
  - 潮湿恢复速度和间隔
- `armor.*`
  - 各材质盔甲保暖值
- `items.held.*`
  - 手持道具温度修正（支持自定义玩法）
- `temperature-event-mode`
  - `highest-only` / `all`
- `temperature-event-cooldown-seconds`
  - 全局温度事件冷却

## 5. 指令

主指令：`/seasons`（别名 `/season`）

- `/seasons help`
- `/seasons info [world]`
- `/seasons calendar`
- `/seasons reload`
- `/seasons set season <world> <spring|summer|autumn|winter>`
- `/seasons set term <world> <solarTermKey>`
- `/seasons next <world>`
- `/seasons auto <world>`

权限：

- `liseasons.player`：基础查询与菜单
- `liseasons.admin`：管理指令

## 6. 常见问题

## 6.1 改季节后颜色没变化？

- 新版为“分批刷新”，不是瞬间整图重绘
- 请确认：
  - 世界已启用 LISeasons
  - `season-color.yml` 的 `biome-spoof.enable` 为 `true`
  - 目标季节已配置可用目标群系

## 6.2 更新后配置没变？

- Bukkit 不会自动覆盖已存在配置文件
- 建议对照新版 `temperature.yml` 手动合并新字段

## 6.3 温度事件太频繁？

- 调高 `temperature-event-cooldown-seconds`
- 使用 `temperature-event-mode: highest-only`
- 提高危险事件阈值温度

## 7. 性能建议

- `event-interval-ticks` 建议不低于 `40`
- `biome-spoof.budget-chunks-per-tick` 控制在 `4~12`
- 视觉特效和温度事件不要同时设为极高频率
- 先在测试服验证配置，再应用到正式服

## 8. 版本信息

- 当前文档对应版本：`0.1.43-paper1.21.11`
- 核心适配：`Paper 1.21.11`（兼容 Folia 调度模式）


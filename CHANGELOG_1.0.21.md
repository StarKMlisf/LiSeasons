# LISeasons 1.0.21 更新说明

## 调整

- GUI 配置改为更接近 TrMenu 的写法：每个菜单单独放在 `gui/` 文件夹下。
- `gui/overview.yml`、`gui/month-terms.yml`、`gui/year-terms.yml`、`gui/temperature.yml`、`gui/season-events.yml`、`gui/festivals.yml` 均使用 `menu.layout` + `menu.icons` 字符矩阵配置。
- 新增 `type: content` 动态内容占位：节气、事件、节日等列表会自动填入 layout 中对应字符的位置。

## 优化

- `content-slots` 不再作为主要配置方式，避免手写槽位列表不好理解、容易无效的问题。
- `gui.yml` 仅保留全局兜底和说明，默认菜单以 `gui/*.yml` 为准。

## 构建

- 插件版本更新为 `1.0.21`。

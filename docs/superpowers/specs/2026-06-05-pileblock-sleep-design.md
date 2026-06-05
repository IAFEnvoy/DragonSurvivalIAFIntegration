# PileBlock 继承 TreasureBlock 睡眠能力

## 目标

允许龙玩家在 Ice & Fire 的 PileBlock（金/银/铜堆）上睡觉，就像在 Dragon Survival 的 TreasureBlock 上一样。

## 范围

仅继承**睡眠能力**（`isBed` + 休息触发 + 重生点），不包括：
- 粒子效果
- 生命恢复加速（由 `DragonTreasureHandler` 自动生效，因为已有 `instanceof PileBlock` 兼容）
- BeLoong-Core 兼容

## 实现

### Mixin 1: `PileBlockMixin`

目标：`com.iafenvoy.iceandfire.item.block.PileBlock`

| 方法 | 实现 |
|------|------|
| `isBed(state, level, pos, sleeper)` | `return DragonStateProvider.isDragon(sleeper)` |
| `getRespawnPosition(state, type, levelReader, pos, orientation)` | 复用 TreasureBlock 逻辑：调用 `RespawnAnchorBlock.findStandUpPosition` 查找可站立位置 |
| `isPossibleToRespawnInThis(state)` | `return true` |
| `useWithoutItem(state, level, pos, player, hit)` | 注入逻辑：非叠放操作时，龙玩家站在方块上 → 触发 `TreasureRestData.setResting(true)` + 设置重生点 + 同步网络包 |

`useWithoutItem` 注入点：在现有叠放逻辑返回 `InteractionResult.PASS` 之前。

### Mixin 2: `DragonTreasureHandlerMixin`

目标：`by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonTreasureHandler`

修改 `update()` 方法中的条件（第 86 行）：

```java
// 修改前
!(player.getBlockStateOn().getBlock() instanceof TreasureBlock)

// 修改后
!(player.getBlockStateOn().getBlock() instanceof TreasureBlock
    || player.getBlockStateOn().getBlock() instanceof PileBlock)
```

确保龙玩家站在 PileBlock 上不会被强制取消休息状态。

### PileBlock.LAYERS 兼容性

`PileBlock.LAYERS` 与 `TreasureBlock.LAYERS` 是相同的属性：`IntegerProperty.create("layers", 1, 8)`。`DragonTreasureHandler.handleResting()` 原有的层数扫描在扩展 `instanceof` 后会自然生效，因为 `state.getValue(LAYERS_PROPERTY)` 在两个方块状态上都能正确取值。

## 文件清单

```
src/main/java/com/iafenvoy/dsiafi/mixin/
├── PileBlockMixin.java
└── DragonTreasureHandlerMixin.java
```

需要更新 `src/main/resources/dsiafi.mixins.json` 注册两个新 mixin。

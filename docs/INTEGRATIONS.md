# 协议集成说明

## 当前策略

- `BrandCommandAdapter` 负责品牌命令映射
- `ProtocolClient` 负责具体协议客户端执行
- `ControlAdapter` 负责控制通道封装

## 开发/测试 vs 生产

- 默认 profile 使用 `mock`，用于本地开发与测试验证
- 使用 mock：`SPRING_PROFILES_ACTIVE=mock` 或直接执行 `run-local.cmd`
- 使用真实环境：`SPRING_PROFILES_ACTIVE=real`，并提供 `application-real.yml` 配置

## 错误码（ProblemDetail.code）

- `integration.disabled`
- `integration.config.missing`
- `integration.transport.failure`
- `integration.transport.timeout`

## 当前已实现客户端

- `mock-protocol-client`：覆盖所有品牌和路径
- `samsung-real-lan-client`：Samsung 局域网真实接入骨架
- `sony-real-lan-client`：Sony BRAVIA IRCC 真实接入
- `lg-real-lan-client`：LG webOS 局域网真实接入骨架
- `gateway-ir-real-client`：家庭红外网关真实接入
- `gateway-hdmi-cec-real-client`：家庭 HDMI-CEC 网关真实接入

## 配置项

- `remote.integration.default-mode`
- `remote.integration.strict-mode`
- `remote.integration.adapter-modes.samsung-lan`
- `remote.integration.adapter-modes.sony-lan`
- `remote.integration.adapter-modes.lg-lan`
- `remote.integration.adapter-modes.generic-ir`
- `remote.integration.adapter-modes.generic-hdmi-cec`
- `remote.integration.samsung.endpoint`
- `remote.integration.samsung.token`
- `remote.integration.sony.endpoint`
- `remote.integration.sony.pre-shared-key`
- `remote.integration.sony.ircc-endpoint`
- `remote.integration.lg.endpoint`
- `remote.integration.lg.client-key`
- `remote.integration.gateway.endpoint`
- `remote.integration.gateway.infrared-endpoint`
- `remote.integration.gateway.hdmi-cec-endpoint`
- `remote.integration.gateway.hub-id`
- `remote.integration.gateway.auth-token`
- `remote.integration.gateway.ir-codes`（按 profileKey 配置红外码）
- `remote.integration.gateway.cec-commands`（按 actionKey 配置 CEC 负载）

## 网关 payload 约定

### 关键规则

- `profileKey` = `{brand}-{model}-{actionKey}` 全小写并做连字符清洗
- `actionKey` 由系统内置命令映射生成（例如 `HOME` -> `home`，`POWER_ON` -> `power-on`）
- IR 和 CEC payload 都会携带 `actionKey`，便于网关统一做动作映射
- 示例 TCL 命令集：`power-toggle`、`volume-up`、`volume-down`、`home`、`back`、`dpad-*`、`ok`

### Infrared

#### TCL IR 模板（从 application-real.yml 生成）

<!-- IR-CODES-START -->
```yml
ir-codes:
  tcl-q7-home:
    protocol: NEC
    bits: 32
    data: "0x00FF48B7"
    repeat: 1
  tcl-q7-power-toggle:
    protocol: NEC
    bits: 32
    data: "0x00FF48B7"
    repeat: 1
  tcl-q7-volume-up:
    protocol: NEC
    bits: 32
    data: "0x00FF48B7"
    repeat: 1
  tcl-q7-volume-down:
    protocol: NEC
    bits: 32
    data: "0x00FF48B7"
    repeat: 1
  tcl-q7-back:
    protocol: NEC
    bits: 32
    data: "0x00FF48B7"
    repeat: 1
  tcl-q7-dpad-up:
    protocol: NEC
    bits: 32
    data: "0x00FF48B7"
    repeat: 1
  tcl-q7-dpad-down:
    protocol: NEC
    bits: 32
    data: "0x00FF48B7"
    repeat: 1
  tcl-q7-dpad-left:
    protocol: NEC
    bits: 32
    data: "0x00FF48B7"
    repeat: 1
  tcl-q7-dpad-right:
    protocol: NEC
    bits: 32
    data: "0x00FF48B7"
    repeat: 1
  tcl-q7-ok:
    protocol: NEC
    bits: 32
    data: "0x00FF48B7"
    repeat: 1
```
<!-- IR-CODES-END -->

更新命令：`powershell -ExecutionPolicy Bypass -File scripts/update-ir-template.ps1`

```json
{
  "hubId": "hub-one-demo",
  "transport": "infrared",
  "target": {
    "deviceId": "tv-family-room",
    "brand": "TCL",
    "model": "Q7",
    "room": "Family Room"
  },
  "command": {
    "name": "HOME",
    "profileKey": "tcl-q7-home",
    "actionKey": "home",
    "format": "tasmota-irsend",
    "ir": {
      "protocol": "NEC",
      "bits": 32,
      "data": "0x00FF48B7",
      "repeat": 1
    }
  }
}
```

- 如果未配置 `ir` 字段，则使用 `profileKey` 由网关侧进行码库匹配
- `ir` 字段采用常见的 `Protocol/Bits/Data/Repeat` 结构，便于映射到主流 IR 发送接口

### HDMI-CEC

```json
{
  "hubId": "hub-one-demo",
  "transport": "hdmi-cec",
  "target": {
    "deviceId": "tv-cinema-room",
    "brand": "BenQ",
    "model": "W4000i",
    "room": "Cinema Room"
  },
  "command": {
    "name": "HOME",
    "actionKey": "cec-home",
    "format": "cec-hex",
    "cec": {
      "payloadHex": "40:44"
    }
  }
}
```

- 若未配置 `cec`，则仅传递 `actionKey`，由网关侧做动作映射

## 后续扩展建议

- 把 `mock` 改为真实协议时，优先补齐鉴权、超时、重试与错误分类
- 增加真实硬件联调后的兼容性矩阵


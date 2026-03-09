# Adaptive TV Remote

一个独立的电视遥控项目原型，目标是验证“手机 App 作为统一入口，按可用能力自动选择局域网直连、红外网关或 HDMI-CEC 网关”的产品方向。

## 项目定位

- 这是一个独立项目，代码位于 `prototypes/adaptive-tv-remote`
- 它不属于当前工作区根 `pom.xml` 下的两个 OSS 模块
- 它当前是一个 `Spring Boot + Mobile Web/PWA` 原型，而不是原生 Android / iOS 客户端
- 它的职责是验证产品链路、API 设计、适配层扩展点和遥控 UI 体验
- 该目录已经补齐了独立仓库所需的 wrapper、CI、贡献说明和 issue / PR 模板

## 当前能力

- 统一的 `RemoteCommand` 指令模型
- `LAN_DIRECT`、`IR_GATEWAY`、`HDMI_CEC_GATEWAY` 三种控制路径
- 基于品牌和路径的适配器扩展点
- 支持家庭、房间、设备、配对关系的持久化目录
- 支持候选设备扫描、接管和配对建议流程
- 支持 Samsung / LG 候选设备接管后的品牌 onboarding 握手结果持久化
- PWA 遥控界面可展示设备 onboarding 状态、最近会话和凭据是否已协商
- 内置设备发现、设备画像、配对关系和命令执行历史
- 自带手机友好的网页遥控器界面
- 支持 `mock / real skeleton` 按适配器切换

## 项目结构

- `src/main/java/.../api`：REST API
- `src/main/java/.../service`：设备目录、发现、路由与执行编排
- `src/main/java/.../adapter`：控制通道适配器
- `src/main/java/.../brand`：品牌命令映射
- `src/main/java/.../integration`：协议客户端与 skeleton
- `src/main/java/.../persistence`：JPA 持久化模型与仓库
- `src/main/resources/static`：移动端遥控网页
- `docs/`：架构、集成、抽仓与开发计划

## 本地运行

在项目目录执行：

```powershell
mvnw.cmd -q spring-boot:run
```

或在 macOS / Linux 上执行：

```bash
./mvnw -q spring-boot:run
```

启动后访问：`http://localhost:8080`

## 仓库化准备

- 仓库级说明见 `docs/REPOSITORY-READY.md`
- 抽仓说明见 `docs/EXTRACTION.md`
- GitHub 仓库元信息建议见 `docs/GITHUB-METADATA.md`
- 下一阶段开发计划见 `docs/NEXT-DEVELOPMENT-PLAN.md`
- CI / Release 工作流位于 `.github/workflows/ci.yml`、`.github/workflows/release.yml`
- 贡献与安全说明位于 `CONTRIBUTING.md`、`SECURITY.md`

## 主要接口

- `GET /api/remote/households`
- `GET /api/remote/rooms`
- `GET /api/remote/discovery/candidates`
- `POST /api/remote/discovery/candidates/scan`
- `GET /api/remote/discovery/candidates/{candidateId}/pairing-suggestions`
- `POST /api/remote/discovery/candidates/{candidateId}/adopt`
- `POST /api/remote/discovery/candidates/{candidateId}/dismiss`
- `POST /api/remote/discovery/candidates/{candidateId}/reopen`
- `GET /api/remote/devices`
- `GET /api/remote/devices/{deviceId}`
- `POST /api/remote/devices/register`
- `GET /api/remote/devices/{deviceId}/pairings`
- `GET /api/remote/devices/{deviceId}/onboarding/samsung-handshakes`
- `GET /api/remote/devices/{deviceId}/onboarding/sessions`
- `GET /api/remote/devices/{deviceId}/onboarding/status`
- `POST /api/remote/devices/{deviceId}/onboarding/retry`
- `POST /api/remote/pairings`
- `PATCH /api/remote/pairings/{pairingId}`
- `DELETE /api/remote/pairings/{pairingId}`
- `POST /api/remote/discovery/scan`
- `POST /api/remote/devices/{deviceId}/commands`
- `GET /api/remote/executions`
- `GET /api/remote/adapters`
- `GET /api/remote/integrations`
- `GET /api/remote/profile`

## 集成模式切换

- 默认所有协议客户端都走 `mock`
- 可以按适配器单独切到 `real` skeleton 模式
- 当前已提供品牌骨架：`samsung-lan`、`lg-lan`、`sony-lan`
- 当前已提供网关骨架：`generic-ir`、`generic-hdmi-cec`

示例：启用 Samsung LAN real skeleton

```powershell
mvnw.cmd -q spring-boot:run -Dspring-boot.run.arguments="--remote.integration.adapter-modes.samsung-lan=real"
```

示例：启用 LG LAN real skeleton

```powershell
mvnw.cmd -q spring-boot:run -Dspring-boot.run.arguments="--remote.integration.adapter-modes.lg-lan=real"
```

示例：启用 Sony LAN real skeleton

```powershell
mvnw.cmd -q spring-boot:run -Dspring-boot.run.arguments="--remote.integration.adapter-modes.sony-lan=real"
```

示例：启用网关 IR real skeleton

```powershell
mvnw.cmd -q spring-boot:run -Dspring-boot.run.arguments="--remote.integration.adapter-modes.generic-ir=real"
```

示例：启用网关 HDMI-CEC real skeleton

```powershell
mvnw.cmd -q spring-boot:run -Dspring-boot.run.arguments="--remote.integration.adapter-modes.generic-hdmi-cec=real"
```


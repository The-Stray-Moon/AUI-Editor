# AUI Editor

Add an editor for AUI

- mod id：auieditor
- Java 包名 / Gradle group：cc.sighs.auieditor
- 作者：MerrySnow QiHuang02
- 声明的许可证：LGPL-2.1

## 项目状态

已迁入 AUI Ore 可视化编辑器，包含组件拖放、容器布局、属性与状态编辑、主题调整、撤销重做、项目保存和 HTML 导入导出。迁移来源与验证范围见 [Ore 编辑器迁移记录](docs/ORE_EDITOR_MIGRATION.md)。已完成全部 target 的构建与测试，以及主目标 NeoForge 1.21.1 的 AutoJS 客户端检查；其他 target 的游戏行为仍待验证。

以下矩阵在初始化时从实际 target 目录及 `ci.properties` 生成。CI 启用仅表示会参与验证，不等于已验证支持；后续增删版本或改变工具链时同步维护本表。

| Target | Gradle JVM | CI 配置 | 项目验证状态 |
| --- | --- | --- | --- |
| fabric-1.20.1 | JDK 17 | true | 构建及 53 项测试通过；游戏未验证 |
| fabric-1.21.1 | JDK 21 | true | 构建及 53 项测试通过；游戏未验证 |
| fabric-26.1 | JDK 25 | true | 构建及 53 项测试通过；游戏未验证 |
| forge-1.18.2 | JDK 21 | true | 构建及 53 项测试通过；游戏未验证 |
| forge-1.19.2 | JDK 21 | true | 构建及 53 项测试通过；游戏未验证 |
| forge-1.20.1 | JDK 21 | true | 构建及 53 项测试通过；游戏未验证 |
| neoforge-1.21.1 | JDK 21 | true | 构建及 53 项测试通过；AutoJS 客户端检查通过 |
| neoforge-26.1 | JDK 25 | true | 构建及 53 项测试通过；游戏未验证 |

ApricityUI（mod id `apricityui`）是所有 target 的必选运行依赖。各 target 从 Sighs Maven 获取对应 loader 与 Minecraft 版本变体，并在 loader metadata 中声明最低版本；版本由各 target 的 `gradle.properties` 中 `aui_version` 管理，目前使用 1.3.1。Forge 1.18.2 / 1.19.2 另提供 JOML 1.10.5 的编译与开发运行依赖，按维护者要求不随发布 jar 打包。

进入游戏后，客户端命令 `/auieditor` 打开编辑器，`/auieditor open` 使用 AUI 文件选择器打开可编辑 HTML。项目保存到游戏目录的 `apricity/ore-projects/`；打开现有 HTML 后保存会更新原文件，沿用上游临时文件替换逻辑，不另存历史备份。关闭、加载或切换文件时保留未保存修改确认。

Java 调用入口为 `cc.sighs.auieditor.editor.ore.OreEditor`，提供 `open()`、`openHtml(Path)`、`close()` 和 `toggle()`。客户端注册位于 target，编辑器实现、页面和测试位于 common。

## 开发与构建

开发规范见 [AGENTS.md](AGENTS.md)。共享业务与资源位于 `common/`；loader / Minecraft 差异位于 `targets/<loader>-<version>/`。所有 target 直接编译 common 原源码，不引用共享 jar，也不等待复制到 build 目录。

- 单版本：IDEA 打开对应 target 目录，使用该目录的 wrapper 和矩阵声明的 JDK。
- 全版本：IDEA 打开 `idea-workspace/`；当前主版本为 **neoforge-1.21.1**，配置的 Gradle JVM 为 **JDK 21**。该值来自 target 描述文件，仍须核对工作区 wrapper 与所用 JDK 的兼容性。
- 工作区只完整导入主 target。其他版本、文档、脚本及根配置作为仓库浏览内容展示，不承诺非主版本的 API 补全。common 源码只归属主 target，不额外关联根工程、common 或其他完整 target。
- 切换主版本修改 `idea-workspace/gradle.properties` 的 `workspace_main_target` 后重新加载 Gradle。composite build 共用工作区的 Gradle/JVM，不使用被包含 target 的 wrapper；若不兼容，独立打开 target 或调整工作区工具链。
- 普通 common 内容修改无需运行源码同步任务；变更 Gradle 配置或 `commonJava` 排除规则后需重新加载 Gradle。

在所选 target 目录执行：

```powershell
.\gradlew.bat clean build
.\gradlew.bat verifyHotDebug
.\gradlew.bat runClient
# 或 runServer；Minecraft EULA 必须由用户阅读并接受
```

在仓库根可执行 `./gradlew -p common build` 单独检查共享层，但不能替代目标矩阵验证。根 `-Ptarget=<目录名> build` 调用该 target 的独立 wrapper；`-PallTargets=true build` 自动选择已启用且 `ci.java` 不高于当前 Gradle JVM 的目标，并打印跳过项。最低 JDK 筛选不保证旧 wrapper 支持任意新版 JDK，必要时分别构建。根 `build` 默认只验证 common。

## AutoJS 热调试与热执行

运行时调试必须使用 AutoJS，不得仅凭源码推断、临时日志或构建成功宣称游戏行为正确。

每个 target 的 `debug_*` 属性声明来自 Sighs Maven 的 AutoJS、Rhino 及可用的 KubeJS。具体版本和模式以该 target 配置为准；没有 `debug_kubejs` 时仅用 Rhino，不假设存在 KubeJS 事件或 `kubejs_log`。调试依赖仅用于开发游戏运行，不嵌入发布 jar，不成为玩家端必需依赖。

1. 使用相应 JDK 执行 `verifyHotDebug`，再启动 target 的游戏实例。
2. 默认 `GET http://127.0.0.1:25585/health` 探活，通过 MCP 客户端连接 `http://127.0.0.1:25585/mcp`，先调用 `status` 确认 side 和引擎模式。
3. 用 `eval_js` 执行短脚本，例如 server side 的 `AutoJS.snapshot()`；检查 `isError`、`ok`、`diagnostics`，触发行为后回读状态并形成断言。界面和输入用 client，权威世界修改用 server。
4. 修改脚本用 `reload_scripts`；KubeJS 模式结合 `kubejs_log` 检查错误，Rhino 模式重建作用域后重新注入验证脚本。配方、标签等按需另做 `/reload`。
5. Java、Mixin 和启动期注册内容不能靠脚本热重载替换，需构建重启后再用 AutoJS 验证。

配置位于游戏目录 `config/autojs.json`；多实例用 `AUTOJS_PORT` 分配不同端口。端点默认可执行任意 Java，只用于本机开发，不得暴露公网或玩家生产环境。共享机器配置 token，请求带 `Authorization: Bearer <token>`，不提交凭据。完整操作手册可从 AutoJS 发布 jar 提取 `SKILL.md`。仓库或端点不可用时明确记录阻塞，不用本地替代 jar 冒充远端验证。

## 发布与维护

- curseforge: project ID not configured in root gradle.properties; environment overrides and credentials remain unverified.
- modrinth: project ID not configured in root gradle.properties; environment overrides and credentials remain unverified.

发布前验证承诺支持的每个 target：构建、检查 jar 的 metadata/共享类/资源、使用 AutoJS 做最小 client 和 dedicated server 行为验证，并记录实际范围。上表只记录已执行的检查，构建成功不代表所有游戏行为或服务端兼容性均已验证。

- [发布指南](docs/PUBLISHING.md)
- [维护流程](docs/MAINTENANCE_WORKFLOW.md)
- [自动发现与新增 target](docs/CI_TARGET_DISCOVERY.md)
- [版本迁移参考](docs/version-differences/README.md)

工程来源：[开发框架](https://github.com/Tower-of-Sighs/SighsTemple)。本 README 与 AGENTS.md 已转为项目文档，后续由项目维护者更新，不再运行初始化脚本覆盖。

# Ore 编辑器迁移

源码来自 Tower-of-Sighs/AUI 的 `c3f78b67`（2026-08-19）删除前状态，即父提交 `cda3136e251b6e41f2faf056f9c4dcb96afec039`。该提交删除了 Ore 编辑器 Java 源码、HTML/CSS、18 个测试类及 DevTools 中的打开入口；本项目恢复编辑器部分，继续依赖 ApricityUI 1.3.1 的 DOM、布局、渲染、Ore 主题、文件选择器和提示组件。

- 上游：https://github.com/Tower-of-Sighs/AUI
- 移除提交：https://github.com/Tower-of-Sighs/AUI/commit/c3f78b67
- Java 包从 `com.sighs.apricityui.editor.ore` 迁为 `cc.sighs.auieditor.editor.ore`。
- 页面保留 `assets/apricityui/apricity/editor/ore/` 路径，以适配 AUI 的资源加载器；这些文件在 AUI 1.3.1 中已删除，没有同路径覆盖。
- AUI 1.3.1 发布 jar 同样缺少页面引用的 `apricityui/theme/ore/ore-edit.css`，本项目从上述父提交补回。基础 `ore.css`、字体和其他主题资源继续由 AUI 提供，不重复打包。
- 中英文文本在 `assets/auieditor/lang/`，翻译键使用 `ore_editor.auieditor.*` 与 `tooltip.auieditor.ore_editor.*`，不覆盖 AUI 的语言文件。
- 上游 LGPL 2.1 正文随 jar 保留在 `META-INF/licenses/AUI-LGPL-2.1.txt`，来源说明在 `META-INF/NOTICE-AUI-Editor.txt`；根 LICENSE 保持维护者现状。
- `ore-edit.css` 保留 Minecraft-CSS 的 MPL 2.0 来源注释，对应正文位于 `META-INF/licenses/Minecraft-CSS-MPL-2.0.txt`。

## 入口与存档

所有 target 注册客户端命令 `/auieditor`（打开画布）与 `/auieditor open`（选择可编辑 HTML）。Forge/NeoForge 使用客户端命令事件，Fabric 使用客户端 entrypoint 与 command API v2。服务端不注册命令，也不主动加载编辑器。

公共客户端 API：`OreEditor.open()`、`openHtml(Path)`、`close()`、`toggle()`、`isOpen()`、`getDocument()`、`getSession()`、`loadSavedProject()`。

项目和导出文件沿用上游游戏目录 `apricity/ore-projects/`。编辑已有 HTML 时沿用临时文件替换写入和未保存确认，不另外保存原文件备份。迁移未改变项目 JSON 格式、节点 ID 或 CSS 输出格式。

Forge 1.18.2 / 1.19.2 缺少 AUI 共享渲染代码需要的 JOML，target 显式添加 `implementation`（编译/测试）和 `additionalRuntimeClasspath`（ModDev 开发运行）；按维护者要求不将 JOML 嵌入发布 jar。AUI 依赖使用 LegacyForge 的 `modImplementation` 重映射 SRG 名称。

## 回归检查

每个 target 使用 `ci.properties` 声明的 JDK 执行 `clean build`。共享测试覆盖模型、历史、布局插入、序列化、文件保存、HTML 导入导出及编辑器 DOM 交互；测试中的资源路径由 Gradle 传入，支持独立 common 与 target 构建。

游戏验证先执行 `verifyHotDebug`，再启动客户端，用 AutoJS `status` 确认 client side，并通过 `eval_js` 调用上述公共 API，回读 `isOpen()`、文档节点及保存结果。Java 修改后必须重启游戏。自动化单元测试不能替代实际渲染、鼠标拖放和键盘输入验证。

## 2026-11-07 实际验证

- 自动发现当前 8 个 target，分别使用其 `ci.properties` 的 JDK 和 wrapper 执行 `clean build --offline`：全部成功，每个 target 均运行 53 项测试，失败、错误、跳过均为 0。独立 common 同样构建成功、53 项测试通过。
- 检查八个最终发布 jar：含共享编辑器类、HTML/CSS、双语翻译及来源说明；`apricityui` 为最低 1.3.1 的必选依赖，Fabric 客户端入口已展开。未含嵌套 jar、JOML、AUI 实现类或 AutoJS。
- Forge 1.18.2 / 1.19.2 在线执行 `writeClientLegacyClasspath` 成功，两份 `build/moddev/clientLegacyClasspath.txt` 均包含 `joml-1.10.5.jar`。先前离线缺少调试依赖的尝试未计作成功，联网解析后已解除。
- 主目标 `neoforge-1.21.1`：`verifyHotDebug --offline` 成功，AutoJS 1.0.1，KubeJS 2101.7.2-build.379 / Rhino 2101.2.7-build.81，client side 可用；未加载世界，server side 不可用。

本次游戏验证发现模板现有的 `hot-debug.gradle` 将 NeoForge 调试模组放入 legacy library classpath，默认 `runClient` 不把它们识别为模组。为执行检查，本次使用下列临时 Gradle init script 启动；它只调整本次开发运行，不更改仓库配置或发布依赖：

```groovy
allprojects {
    afterEvaluate {
        neoForge.runs.configureEach {
            additionalRuntimeClasspathConfiguration.setExtendsFrom([])
        }
        configurations.runtimeClasspath.extendsFrom(configurations.hotDebugRuntime)
    }
}
```

在主 target 目录用 JDK 21 执行 `gradlew.bat runClient --offline -I <临时脚本路径>`。`status` 确认 client 已注册且 available，模式为 KubeJS。随后依次执行以下三个 `eval_js`（`side=client`）；第一段打开后等待 CSS/字体任务完成，再执行第二段。每次响应均检查 MCP `isError`、内部 `ok` 与 `diagnostics`，最终三段全部 `ok=true`、无 `isError`，diagnostics 仅 INFO。

### 命令事件注册与打开

```javascript
AutoJS.background(true);
var OreEditor = Java.loadClass("cc.sighs.auieditor.editor.ore.OreEditor");
var Dispatcher = Java.loadClass("com.mojang.brigadier.CommandDispatcher");
var RegisterCommands = Java.loadClass("net.neoforged.neoforge.client.event.RegisterClientCommandsEvent");
var NeoForge = Java.loadClass("net.neoforged.neoforge.common.NeoForge");
var editorDispatcher = new Dispatcher();
NeoForge.EVENT_BUS.post(new RegisterCommands(editorDispatcher, null));
if (editorDispatcher.execute("auieditor", null) != 1) throw new Error("client command did not open editor");
var editorDoc = OreEditor.getDocument();
if (!OreEditor.isOpen() || editorDoc.querySelector("#editorCanvas") == null) throw new Error("editor canvas missing");
if (editorDoc.querySelectorAll(".editor-palette-item").size() != 2) throw new Error("container palette missing");
JSON.stringify({command:true,open:true,canvas:true,paletteItems:2});
```

实际返回（303 ms）：

```json
{"command":true,"open":true,"canvas":true,"paletteItems":2}
```

### 资源、控件与未保存确认

```javascript
var StyleAsyncHandler = Java.loadClass("com.sighs.apricityui.resource.async.style.StyleAsyncHandler");
var editorStyles = StyleAsyncHandler.peekHandle(editorDoc);
if (editorStyles == null || editorStyles.pendingCss() != 0 || editorStyles.pendingFonts() != 0 || editorStyles.failures() != 0) throw new Error("editor resources not ready");
editorDoc.querySelector(".editor-palette-switcher").querySelectorAll("button").get(1).click();
if (editorDoc.querySelectorAll(".editor-palette-item").size() != 3) throw new Error("component palette missing");
editorDoc.querySelector("[data-editor-mode=theme]").click();
if (editorDoc.querySelectorAll(".editor-theme-token-reset").size() != 35) throw new Error("theme controls missing");
OreEditor.getSession().setDirty(true);
OreEditor.close();
var confirmation = editorDoc.querySelector("[data-ore-editor-ui=unsaved-changes-dialog]");
if (!OreEditor.isOpen() || confirmation == null) throw new Error("unsaved changes not protected");
confirmation.querySelector(".button-danger").click();
if (OreEditor.isOpen()) throw new Error("confirmed close failed");
JSON.stringify({pendingCss:0,pendingFonts:0,resourceFailures:0,componentPalette:3,themeTokens:35,unsavedProtected:true,confirmedClose:true});
```

实际返回（88 ms）：

```json
{"pendingCss":0,"pendingFonts":0,"resourceFailures":0,"componentPalette":3,"themeTokens":35,"unsavedProtected":true,"confirmedClose":true}
```

### HTML 导入与保存

```javascript
AutoJS.background(true);
var Files = Java.loadClass("java.nio.file.Files");
var editorTestHtml = Files.createTempFile("auieditor-live-check-", ".html");
var htmlCheckResult;
try {
    Files.writeString(editorTestHtml, "<html><body><p>AutoJS Ore import check</p></body></html>");
    if (!OreEditor.openHtml(editorTestHtml)) throw new Error("HTML import failed");
    var importedEditor = OreEditor.getDocument();
    if (String(importedEditor.body.getTextContent()).indexOf("AutoJS Ore import check") < 0) throw new Error("imported content missing");
    importedEditor.querySelector("#saveButton").click();
    var savedHtml = String(Files.readString(editorTestHtml));
    if (savedHtml.indexOf("AutoJS Ore import check") < 0 || savedHtml.indexOf("ore-edit.css") < 0) throw new Error("HTML save failed");
    if (OreEditor.getSession().dirty()) throw new Error("save left editor dirty");
    OreEditor.close();
    if (OreEditor.isOpen()) throw new Error("saved editor close failed");
    htmlCheckResult = {htmlImported:true,originalFileUpdated:true,themeLinked:true,dirty:false,closed:true};
} finally {
    if (OreEditor.isOpen()) { OreEditor.getSession().setDirty(false); OreEditor.close(); }
    Files.deleteIfExists(editorTestHtml);
    AutoJS.background(false);
}

JSON.stringify(htmlCheckResult);
```

实际返回（146 ms）：

```json
{"htmlImported":true,"originalFileUpdated":true,"themeLinked":true,"dirty":false,"closed":true}
```

首次打开的 AUI 日志同时记录 `status=ready cssPending=0 fontsPending=0 resourceFailures=0 paints=1`；无缺失样式或字体错误。检查结束后删除本次创建的临时 HTML，并恢复 `AutoJS.background(false)`。

尚未验证其他 target 的游戏行为、真实鼠标拖放/键盘输入和 dedicated server 启动。默认热调试模组发现配置的问题仍需单独处理；上述临时脚本仅验证了主目标，不可直接套用旧 Forge。旧版发布 jar 不嵌入 JOML，实际发行运行环境仍需提供该库。

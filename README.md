# 极简记账

极简记账是一款面向日常使用的本地记账 Android 应用，目标是让用户尽快完成一笔收支记录，并能清晰查看账单、统计和资产变化。

## 功能特性

- 快速记账：输入金额、选择收入/支出、绑定资产账户、填写备注后一键保存。
- 常用记录：内置早餐、午餐、工资、红包等快捷模板，点击即可填入。
- 智能备注：提供常用备注和历史备注建议，减少重复输入。
- 账单管理：按日期分组展示流水，支持新增、编辑、删除、搜索、收入/支出筛选和月份切换。
- 统计概览：查看月收入、月支出、当前剩余总资产、最近 7 天趋势和最大单笔支出。
- 资产账户：管理现金、银行卡、支付宝、微信、信用卡等账户，自动汇总资产、负债和净资产。
- 本地存储：使用 Room/SQLite 保存数据，默认数据库名为 `minimal-ledger.db`。
- CSV 导出：在统计页导出账单数据，便于备份或二次分析。

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX Navigation Compose
- AndroidX Lifecycle + ViewModel
- Kotlin Coroutines / Flow
- Room + KSP
- Gradle Kotlin DSL

## 环境要求

- Android Studio，建议使用支持 Android Gradle Plugin 8.5.x 的版本
- JDK 17
- Android SDK 35
- 最低运行系统：Android 8.0（API 26）

项目没有提交 Gradle Wrapper。当前仓库内包含本地工具目录 `.tools/gradle-8.7`，也可以使用系统已安装的 Gradle，但推荐优先使用 Gradle 8.7 来匹配 Android Gradle Plugin 8.5.2。

## 快速开始

1. 使用 Android Studio 打开项目根目录。
2. 确认 `local.properties` 中的 SDK 路径指向本机 Android SDK，例如：

   ```properties
   sdk.dir=D\:\\Android\\Sdk
   ```

3. 等待 Gradle Sync 完成。
4. 选择 `app` 运行配置，连接模拟器或真机后点击 Run。

也可以在命令行构建调试包：

```powershell
.\.tools\gradle-8.7\bin\gradle.bat :app:assembleDebug
```

生成的 APK 位于：

```text
app/build/outputs/apk/debug/
```

## 签名配置

项目支持可选的本地签名配置。若 `local.properties` 中存在以下字段，`debug` 和 `release` 构建会使用本地签名：

```properties
signing.storeFile=signing/minimal-ledger-local.jks
signing.storePassword=你的密码
signing.keyAlias=你的别名
signing.keyPassword=你的密码
```

签名文件和 `local.properties` 已在 `.gitignore` 中忽略，不建议提交到仓库。

## 项目结构

```text
app/
├── src/main/java/com/minimalledger/app/
│   ├── data/
│   │   ├── local/       # Room 数据库、Entity、DAO、迁移
│   │   ├── model/       # 业务模型
│   │   └── repository/  # 数据仓库接口与离线实现
│   ├── ui/
│   │   ├── components/  # 通用 Compose 组件
│   │   ├── screens/     # 记账、账单、统计、资产页面
│   │   └── theme/       # 颜色、排版和主题
│   ├── utils/           # 金额、日期、CSV 等工具
│   ├── viewmodel/       # LedgerViewModel 和 UI State
│   └── MainActivity.kt  # 应用入口
├── build.gradle.kts
└── proguard-rules.pro
```

## 数据模型

当前 Room 数据库版本为 4，包含三类核心数据：

- `transactions`：收支流水，记录金额、类型、备注、时间和关联资产账户。
- `asset_accounts`：资产账户，记录账户名称、类型、余额和更新时间。
- `monthly_budgets`：月预算数据，保留预算扩展能力。

金额统一以“分”为单位存储，避免浮点精度问题。

## 常用命令

```powershell
# 构建 Debug APK
.\.tools\gradle-8.7\bin\gradle.bat :app:assembleDebug

# 构建 Release APK
.\.tools\gradle-8.7\bin\gradle.bat :app:assembleRelease

# 运行单元测试
.\.tools\gradle-8.7\bin\gradle.bat :app:test

# 清理构建产物
.\.tools\gradle-8.7\bin\gradle.bat clean
```

## 后续计划

- 完善预算设置与提醒入口。
- 增加数据备份与恢复能力。
- 优化图表和统计维度。
- 补充单元测试与 UI 测试。
- 增加 Gradle Wrapper，降低新环境构建成本。

## 相关文档

- [极简记账APP开发计划.md](./极简记账APP开发计划.md)

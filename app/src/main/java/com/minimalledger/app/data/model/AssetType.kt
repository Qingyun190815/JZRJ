package com.minimalledger.app.data.model

enum class AssetType(
    val label: String,
    val isLiability: Boolean,
) {
    CASH("\u73B0\u91D1", false),
    BANK_CARD("\u94F6\u884C\u5361", false),
    DIGITAL_WALLET("\u7535\u5B50\u94B1\u5305", false),
    INVESTMENT("\u7406\u8D22\u6295\u8D44", false),
    CREDIT_CARD("\u4FE1\u7528\u5361/\u8D1F\u503A", true),
    OTHER("\u5176\u4ED6\u8D44\u4EA7", false),
}

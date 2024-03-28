rootProject.name = "Accounts"
include("application")
include("shared")
include("account_api")
include("account_domain")
include("account_external")
include("transfer_api")
include("transfer_domain")

project(":account_api").projectDir = File(settingsDir, "account/api")
project(":account_domain").projectDir = File(settingsDir, "account/domain")
project(":account_external").projectDir = File(settingsDir, "account/external")
project(":transfer_api").projectDir = File(settingsDir, "transfer/api")
project(":transfer_domain").projectDir = File(settingsDir, "transfer/domain")

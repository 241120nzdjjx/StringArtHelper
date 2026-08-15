# Keep source-level names only where Android invokes code reflectively.
# The current app has no custom reflective entry points.

# Xiaomi Wearable SDK uses Binder interfaces and package-name based service binding.
-keep class com.xiaomi.xms.wearable.** { *; }

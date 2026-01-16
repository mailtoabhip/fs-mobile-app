package com.delhivery.axle.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Root Detection & Emulator Detection Utility
 * 
 * Provides comprehensive security checks to identify:
 * 1. Rooted devices - Devices with elevated privileges
 * 2. Emulator environments - Virtual devices/simulators
 * 
 * This is critical for maintaining security and protecting sensitive business data.
 * 
 * Root Detection Methods:
 * 1. Build Tags Check - Detects test-keys in ROM builds
 * 2. SU Binary Detection - Checks for su binary in common locations
 * 3. Which SU Command - Executes which command to find su
 * 4. BusyBox Detection - Checks for BusyBox binary
 * 5. Root Management Apps - Detects installed root apps (Magisk, SuperSU, etc.)
 * 6. Read-Write Paths - Checks if system partitions are writable
 * 7. Dangerous Properties - Checks for insecure system properties
 * 
 * Emulator Detection Methods:
 * 1. Build Properties - Checks for emulator signatures in device properties
 * 2. Emulator Files - Detects emulator-specific system files
 * 3. Known Emulators - Identifies Genymotion, BlueStacks, Nox, etc.
 * 4. Hardware Features - Checks for missing telephony/sensors
 * 
 * @author Security Team
 * @version 2.0
 */
object RootDetectionUtil {

    private const val TAG = "SecurityDetection"
    
    /**
     * Programmatic flag to disable emulator check.
     * Set to true to skip emulator detection temporarily.
     * 
     * Usage:
     * RootDetectionUtil.disableEmulatorCheck = true  // Disable
     * RootDetectionUtil.disableEmulatorCheck = false // Enable
     */
    @Volatile
    var disableEmulatorCheck: Boolean = false

    /**
     * Quick boolean check for rooted device.
     * Uses all available detection methods.
     * 
     * @return true if device is likely rooted, false otherwise
     */
    fun isDeviceRooted(): Boolean {
        return checkRootMethod1() || 
               checkRootMethod2() || 
               checkRootMethod3() || 
               checkForBusyBoxBinary() || 
               checkForSuBinary() || 
               checkSuExists() || 
               checkForRWPaths()
    }

    /**
     * Comprehensive security check with detailed results.
     * Runs all root and emulator detection methods.
     * 
     * Emulator checks can be disabled via:
     * RootDetectionUtil.disableEmulatorCheck = true  // Disable
     * RootDetectionUtil.disableEmulatorCheck = false // Enable
     * 
     * @param context Application context for checking installed apps and features
     * @return SecurityCheckResult with detection status and individual check results
     */
    fun performFullSecurityCheck(context: Context): SecurityCheckResult {
        val rootChecks = mutableListOf<Pair<String, Boolean>>()
        val emulatorChecks = mutableListOf<Pair<String, Boolean>>()
        
        // Check if emulator check should be disabled
        if (disableEmulatorCheck) {
            Log.w(TAG, "Emulator check disabled programmatically")
        }
        
        Log.d(TAG, "=== Starting Security Check ===")
        Log.d(TAG, "Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        Log.d(TAG, "Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        Log.d(TAG, "Build Type: ${Build.TYPE}, Tags: ${Build.TAGS}")
        
        try {
            // Root detection checks (always run) with individual logging
            rootChecks.add("Test Keys" to runCheckWithLogging("Test Keys") { checkRootMethod1() })
            rootChecks.add("SU Binary Paths" to runCheckWithLogging("SU Binary Paths") { checkRootMethod2() })
            rootChecks.add("Which SU" to runCheckWithLogging("Which SU") { checkRootMethod3() })
            rootChecks.add("BusyBox" to runCheckWithLogging("BusyBox") { checkForBusyBoxBinary() })
            rootChecks.add("SU Binary" to runCheckWithLogging("SU Binary") { checkForSuBinary() })
            rootChecks.add("SU Exists" to runCheckWithLogging("SU Exists") { checkSuExists() })
            rootChecks.add("RW Paths" to runCheckWithLogging("RW Paths") { checkForRWPaths() })
            rootChecks.add("Root Apps" to runCheckWithLogging("Root Apps") { checkForRootApps(context) })
            rootChecks.add("Dangerous Props" to runCheckWithLogging("Dangerous Props") { checkForDangerousProps() })
            rootChecks.add("SELinux Check" to runCheckWithLogging("SELinux Check") { checkSELinuxEnforcement() })
            rootChecks.add("Magisk Detection" to runCheckWithLogging("Magisk Detection") { checkForMagisk(context) })
            rootChecks.add("SU Command Test" to runCheckWithLogging("SU Command Test") { checkSuCommandExecution() })
            
            // Emulator detection checks (skip if disabled)
            if (!disableEmulatorCheck) {
                emulatorChecks.add("Build Properties" to checkEmulatorBuildProperties())
                emulatorChecks.add("Emulator Files" to checkEmulatorFiles())
                emulatorChecks.add("Known Emulators" to checkKnownEmulators())
                emulatorChecks.add("Telephony" to checkTelephonyFeatures(context))
            } else {
                // Add all emulator checks as false when skipped
                emulatorChecks.add("Build Properties" to false)
                emulatorChecks.add("Emulator Files" to false)
                emulatorChecks.add("Known Emulators" to false)
                emulatorChecks.add("Telephony" to false)
            }
            
            val isRooted = rootChecks.any { it.second }
            val isEmulator = if (disableEmulatorCheck) false else emulatorChecks.any { it.second }
            
            Log.d(TAG, "=== Security Check Results ===")
            Log.d(TAG, "Root Checks:")
            rootChecks.forEach { (name, result) ->
                Log.d(TAG, "  $name: ${if (result) "DETECTED ⚠️" else "Pass"}")
            }
            
            if (isRooted) {
                val detectedMethods = rootChecks.filter { it.second }.joinToString { it.first }
                Log.w(TAG, "🚨 ROOT DETECTED by methods: $detectedMethods")
            }
            
            if (isEmulator) {
                val detectedMethods = emulatorChecks.filter { it.second }.joinToString { it.first }
                Log.w(TAG, "Emulator detected by methods: $detectedMethods")
            }
            
            if (!isRooted && !isEmulator) {
                Log.d(TAG, "✅ Device is secure - no root or emulator detected")
            }
            
            Log.d(TAG, "=== Security Check Complete ===")
            
            return SecurityCheckResult(
                isRooted = isRooted,
                isEmulator = isEmulator,
                rootChecks = rootChecks,
                emulatorChecks = emulatorChecks
            )
        } catch (e: Exception) {
            Log.e(TAG, "🔴 CRITICAL ERROR during security check - ASSUMING ROOTED FOR SAFETY", e)
            // CRITICAL: On error, assume rooted for security
            return SecurityCheckResult(
                isRooted = true,
                isEmulator = false,
                rootChecks = rootChecks + listOf("Error Handler" to true),
                emulatorChecks = emulatorChecks
            )
        }
    }
    
    /**
     * Helper function to run a check with detailed logging.
     */
    private fun runCheckWithLogging(checkName: String, check: () -> Boolean): Boolean {
        return try {
            val result = check()
            Log.d(TAG, "[$checkName] Result: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "[$checkName] Exception occurred: ${e.message}", e)
            false
        }
    }
    
    /**
     * Legacy method for backward compatibility.
     * Performs full security check but returns root-focused result.
     */
    @Deprecated("Use performFullSecurityCheck instead", ReplaceWith("performFullSecurityCheck(context)"))
    fun performFullRootCheck(context: Context): RootCheckResult {
        val securityCheck = performFullSecurityCheck(context)
        return RootCheckResult(
            isRooted = securityCheck.isRooted,
            checks = securityCheck.rootChecks
        )
    }
    

    /**
     * Check for common root management apps.
     * Enhanced with more root app packages.
     * 
     * @param context Application context
     * @return true if any root management app is found
     */
    fun checkForRootApps(context: Context): Boolean {
        val rootApps = arrayOf(
            // Magisk variants
            "com.topjohnwu.magisk",
            "com.topjohnwu.magiskmanager",
            "io.github.huskydg.magisk",
            // SuperSU variants
            "eu.chainfire.supersu",
            "eu.chainfire.supersu.pro",
            // Superuser
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "me.phh.superuser",
            // KingRoot variants
            "com.kingroot.kinguser",
            "com.kingroot.RushRoot",
            "com.kingroot.master",
            // Other root tools
            "com.kingo.root",
            "com.smedialink.oneclickroot",
            "com.zhiqupk.root.global",
            "com.alephzain.framaroot",
            "com.ramdroid.appquarantine",
            "com.ramdroid.appquarantinepro",
            "com.devadvance.rootcloak",
            "com.devadvance.rootcloakplus",
            "de.robv.android.xposed.installer",
            "com.saurik.substrate",
            "com.zachspong.temprootremovejb",
            "com.amphoras.hidemyroot",
            "com.amphoras.hidemyrootadfree",
            "com.formyhm.hiderootPremium",
            "com.formyhm.hideroot"
        )

        val packageManager = context.packageManager
        Log.d(TAG, "Checking for ${rootApps.size} known root apps...")
        
        for (packageName in rootApps) {
            try {
                packageManager.getPackageInfo(packageName, 0)
                Log.w(TAG, "🚨 Root app detected: $packageName")
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                // Package not found, continue checking
            }
        }
        
        Log.d(TAG, "No known root apps found")
        return false
    }

    /**
     * Method 1: Check for test-keys in build tags.
     * Official Android builds use "release-keys", custom ROMs often use "test-keys".
     * 
     * @return true if test-keys found
     */
    private fun checkRootMethod1(): Boolean {
        return try {
            val buildTags = Build.TAGS
            val hasTestKeys = buildTags != null && buildTags.contains("test-keys")
            
            if (hasTestKeys) {
                Log.w(TAG, "🚨 Test-keys detected in Build.TAGS: $buildTags")
            } else {
                Log.d(TAG, "Build.TAGS check passed: $buildTags")
            }
            
            hasTestKeys
        } catch (e: Exception) {
            Log.e(TAG, "Error in checkRootMethod1", e)
            false
        }
    }

    /**
     * Method 2: Check for su binary in common paths.
     * Enhanced to handle SELinux restrictions.
     * 
     * @return true if su binary found in any known location
     */
    private fun checkRootMethod2(): Boolean {
        return try {
            val paths = arrayOf(
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su",
                "/su/bin/su",
                "/apex/com.android.runtime/bin/su",
                "/vendor/bin/su",
                "/vendor/xbin/su"
            )
            
            Log.d(TAG, "Checking ${paths.size} paths for SU binary...")
            for (path in paths) {
                if (canReadFile(path)) {
                    Log.w(TAG, "🚨 SU binary found at: $path")
                    return true
                }
            }
            Log.d(TAG, "No SU binary found in common paths")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error in checkRootMethod2", e)
            false
        }
    }

    /**
     * Method 3: Execute 'which su' command.
     * 
     * @return true if su is found in PATH
     */
    private fun checkRootMethod3(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            bufferedReader.readLine() != null
        } catch (t: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }

    /**
     * Method 4: Check for BusyBox binary.
     * BusyBox is commonly installed alongside root.
     * 
     * @return true if BusyBox binary found
     */
    private fun checkForBusyBoxBinary(): Boolean {
        return checkForBinary("busybox")
    }

    /**
     * Method 5: Check for su binary using generic binary check.
     * 
     * @return true if su binary found
     */
    private fun checkForSuBinary(): Boolean {
        return checkForBinary("su")
    }

    /**
     * Generic binary check in PATH locations.
     * Enhanced with better file detection.
     * 
     * @param filename Binary name to search for
     * @return true if binary found
     */
    private fun checkForBinary(filename: String): Boolean {
        return try {
            val pathsArray = System.getenv("PATH")?.split(":")?.toTypedArray() ?: arrayOf(
                "/sbin",
                "/system/bin",
                "/system/xbin",
                "/data/local/xbin",
                "/data/local/bin",
                "/system/sd/xbin",
                "/system/bin/failsafe",
                "/data/local",
                "/apex/com.android.runtime/bin",
                "/vendor/bin",
                "/vendor/xbin"
            )

            Log.d(TAG, "Searching for '$filename' in ${pathsArray.size} paths...")
            for (path in pathsArray) {
                val completePath = "$path/$filename"
                if (canReadFile(completePath)) {
                    Log.w(TAG, "🚨 Binary found: $completePath")
                    return true
                }
            }
            Log.d(TAG, "'$filename' not found in PATH")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for binary: $filename", e)
            false
        }
    }

    /**
     * Check if su exists using which command.
     * 
     * @return true if su can be executed
     */
    private fun checkSuExists(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            bufferedReader.readLine() != null
        } catch (t: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }

    /**
     * Method 6: Check for read-write mounted system paths.
     * System partitions should be read-only on non-rooted devices.
     * 
     * @return true if system is mounted as rw
     */
    private fun checkForRWPaths(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("mount"))
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                val args = line?.split(" ")?.toTypedArray()
                if (args != null && args.size >= 4) {
                    val mountPoint = args[1]
                    val mountOptions = args[3]
                    
                    // Check if system partitions are mounted as rw (read-write)
                    if ((mountPoint == "/system" || mountPoint == "/system/xbin") && 
                        mountOptions.contains("rw")) {
                        Log.d(TAG, "System mounted as RW at: $mountPoint")
                        return true
                    }
                }
            }
            false
        } catch (t: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }

    /**
     * Method 7: Check for dangerous system properties.
     * 
     * @return true if insecure properties detected
     */
    fun checkForDangerousProps(): Boolean {
        return try {
            val dangerousProps = mapOf(
                "ro.debuggable" to "1",
                "ro.secure" to "0"
            )

            for ((key, badValue) in dangerousProps) {
                val value = getSystemProperty(key)
                if (value == badValue) {
                    Log.d(TAG, "Dangerous property detected: $key=$value")
                    return true
                }
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking dangerous props", e)
            false
        }
    }

    /**
     * Get system property value using getprop command.
     * 
     * @param propName Property name to query
     * @return Property value or null
     */
    private fun getSystemProperty(propName: String): String? {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("getprop", propName))
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            bufferedReader.readLine()
        } catch (t: Throwable) {
            null
        } finally {
            process?.destroy()
        }
    }
    
    /**
     * Method 8: Check SELinux enforcement status.
     * Rooted devices often have SELinux in permissive mode.
     * 
     * @return true if SELinux is permissive or disabled
     */
    private fun checkSELinuxEnforcement(): Boolean {
        var process: Process? = null
        return try {
            // Check getenforce command
            process = Runtime.getRuntime().exec("getenforce")
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            val output = bufferedReader.readLine()
            
            val isPermissive = output != null && 
                (output.equals("Permissive", ignoreCase = true) || 
                 output.equals("Disabled", ignoreCase = true))
            
            if (isPermissive) {
                Log.d(TAG, "SELinux is in permissive/disabled mode: $output")
            } else {
                Log.d(TAG, "SELinux status: $output")
            }
            
            isPermissive
        } catch (e: Exception) {
            Log.d(TAG, "Could not check SELinux status: ${e.message}")
            false
        } finally {
            process?.destroy()
        }
    }
    
    /**
     * Method 9: Check for Magisk root solution.
     * Magisk is the most popular modern root solution.
     * 
     * @param context Application context
     * @return true if Magisk is detected
     */
    private fun checkForMagisk(context: Context): Boolean {
        try {
            // Check for Magisk app packages (including renamed versions)
            val magiskPackages = arrayOf(
                "com.topjohnwu.magisk",
                "io.github.huskydg.magisk",
                "com.topjohnwu.magiskmanager"
            )
            
            val packageManager = context.packageManager
            for (packageName in magiskPackages) {
                try {
                    packageManager.getPackageInfo(packageName, 0)
                    Log.d(TAG, "Magisk package detected: $packageName")
                    return true
                } catch (e: PackageManager.NameNotFoundException) {
                    // Continue checking
                }
            }
            
            // Check for Magisk-specific files
            val magiskFiles = arrayOf(
                "/sbin/.magisk",
                "/data/adb/magisk",
                "/data/adb/magisk.db",
                "/data/adb/modules",
                "/cache/.disable_magisk",
                "/dev/.magisk",
                "/dev/magisk"
            )
            
            for (file in magiskFiles) {
                if (canReadFile(file)) {
                    Log.d(TAG, "Magisk file detected: $file")
                    return true
                }
            }
            
            // Check for Magisk mount points
            if (checkMagiskMounts()) {
                Log.d(TAG, "Magisk mounts detected")
                return true
            }
            
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for Magisk", e)
            return false
        }
    }
    
    /**
     * Check for Magisk-specific mount points.
     * 
     * @return true if Magisk mounts detected
     */
    private fun checkMagiskMounts(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec("mount")
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                line?.let {
                    if (it.contains("magisk") || it.contains("/sbin")) {
                        Log.d(TAG, "Suspicious mount: $it")
                        return true
                    }
                }
            }
            false
        } catch (e: Exception) {
            Log.d(TAG, "Could not check mounts: ${e.message}")
            false
        } finally {
            process?.destroy()
        }
    }
    
    /**
     * Method 10: Try to execute su command with id.
     * This is a more aggressive test.
     * 
     * @return true if su command can be executed
     */
    private fun checkSuCommandExecution(): Boolean {
        var process: Process? = null
        return try {
            Log.d(TAG, "Attempting to execute 'su' command...")
            process = Runtime.getRuntime().exec("su")
            
            // Try to write a command
            val outputStream = process.outputStream
            outputStream.write("id\n".toByteArray())
            outputStream.write("exit\n".toByteArray())
            outputStream.flush()
            
            // Read output with timeout
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            var linesRead = 0
            while (reader.readLine().also { line = it } != null && linesRead < 10) {
                output.append(line).append("\n")
                linesRead++
            }
            
            val result = output.toString()
            if (result.contains("uid=0") || result.contains("root")) {
                Log.d(TAG, "SU command executed successfully: $result")
                return true
            }
            
            Log.d(TAG, "SU command output (no root): $result")
            false
        } catch (e: Exception) {
            Log.d(TAG, "SU command execution failed (expected): ${e.message}")
            false
        } finally {
            try {
                process?.destroy()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }
    
    /**
     * Helper to check if a file can be read (works better than File.exists() with SELinux).
     * 
     * @param path File path to check
     * @return true if file is accessible
     */
    private fun canReadFile(path: String): Boolean {
        return try {
            val file = File(path)
            // Try multiple checks
            when {
                file.exists() -> {
                    Log.d(TAG, "File exists (via exists()): $path")
                    true
                }
                file.canRead() -> {
                    Log.d(TAG, "File readable (via canRead()): $path")
                    true
                }
                else -> {
                    // Try to actually open the file
                    try {
                        val reader = file.bufferedReader()
                        reader.close()
                        Log.d(TAG, "File accessible (via read test): $path")
                        true
                    } catch (e: Exception) {
                        false
                    }
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    // ============================================
    // EMULATOR DETECTION METHODS
    // ============================================
    
    /**
     * Check build properties for emulator signatures.
     * 
     * @return true if emulator properties detected
     */
    private fun checkEmulatorBuildProperties(): Boolean {
        return try {
            val fingerprint = Build.FINGERPRINT
            val model = Build.MODEL
            val manufacturer = Build.MANUFACTURER
            val brand = Build.BRAND
            val device = Build.DEVICE
            val product = Build.PRODUCT
            val hardware = Build.HARDWARE
            
            // Check for common emulator signatures
            val isEmulator = fingerprint.startsWith("generic") ||
                    fingerprint.startsWith("unknown") ||
                    model.contains("google_sdk") ||
                    model.contains("Emulator") ||
                    model.contains("Android SDK built for x86") ||
                    manufacturer.contains("Genymotion") ||
                    brand.startsWith("generic") && device.startsWith("generic") ||
                    product == "google_sdk" ||
                    product == "sdk_google" ||
                    product == "sdk" ||
                    product == "sdk_x86" ||
                    product == "vbox86p" ||
                    product == "emulator" ||
                    product == "simulator" ||
                    hardware.contains("goldfish") ||
                    hardware.contains("ranchu") ||
                    hardware.contains("vbox")
            
            if (isEmulator) {
                Log.d(TAG, "Emulator detected via build properties: model=$model, product=$product")
            }
            
            isEmulator
        } catch (e: Exception) {
            Log.e(TAG, "Error checking emulator build properties", e)
            false
        }
    }
    
    /**
     * Check for emulator-specific files.
     * 
     * @return true if emulator files detected
     */
    private fun checkEmulatorFiles(): Boolean {
        return try {
            val emulatorFiles = arrayOf(
                "/dev/socket/qemud",
                "/dev/qemu_pipe",
                "/system/lib/libc_malloc_debug_qemu.so",
                "/sys/qemu_trace",
                "/system/bin/qemu-props",
                "/dev/socket/genyd",
                "/dev/socket/baseband_genyd"
            )
            
            for (file in emulatorFiles) {
                if (File(file).exists()) {
                    Log.d(TAG, "Emulator file detected: $file")
                    return true
                }
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking emulator files", e)
            false
        }
    }
    
    /**
     * Check for known emulator environments.
     * 
     * @return true if known emulator detected
     */
    private fun checkKnownEmulators(): Boolean {
        return try {
            val deviceName = Build.DEVICE.lowercase()
            val model = Build.MODEL.lowercase()
            val product = Build.PRODUCT.lowercase()
            val manufacturer = Build.MANUFACTURER.lowercase()
            val brand = Build.BRAND.lowercase()
            
            val knownEmulators = listOf(
                "genymotion",
                "bluestacks",
                "andy",
                "nox",
                "ttvm",
                "droid4x",
                "windroy",
                "vbox86"
            )
            
            for (emulator in knownEmulators) {
                if (deviceName.contains(emulator) ||
                    model.contains(emulator) ||
                    product.contains(emulator) ||
                    manufacturer.contains(emulator) ||
                    brand.contains(emulator)) {
                    Log.d(TAG, "Known emulator detected: $emulator")
                    return true
                }
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking known emulators", e)
            false
        }
    }
    
    /**
     * Check for telephony features (emulators often lack real telephony).
     * 
     * @param context Application context
     * @return true if telephony features are missing (indicates emulator)
     */
    private fun checkTelephonyFeatures(context: Context): Boolean {
        return try {
            val hasPhone = context.packageManager.hasSystemFeature("android.hardware.telephony")
            val deviceId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )
            
            // Emulators often have null or generic ANDROID_ID
            val hasGenericId = deviceId == null || 
                    deviceId == "9774d56d682e549c" || // Common emulator ID
                    deviceId.matches(Regex("^0+$")) // All zeros
            
            val isLikelyEmulator = !hasPhone || hasGenericId
            
            if (isLikelyEmulator) {
                Log.d(TAG, "Emulator indicators: hasPhone=$hasPhone, deviceId=$deviceId")
            }
            
            isLikelyEmulator
        } catch (e: Exception) {
            Log.e(TAG, "Error checking telephony features", e)
            false
        }
    }
    
    // ============================================
    // DATA CLASSES
    // ============================================
    
    /**
     * Data class containing comprehensive security check results.
     * 
     * @property isRooted True if device is rooted
     * @property isEmulator True if running on emulator
     * @property rootChecks List of root detection check results
     * @property emulatorChecks List of emulator detection check results
     */
    data class SecurityCheckResult(
        val isRooted: Boolean,
        val isEmulator: Boolean,
        val rootChecks: List<Pair<String, Boolean>>,
        val emulatorChecks: List<Pair<String, Boolean>>
    ) {
        /**
         * Check if device is insecure (rooted or emulator).
         */
        fun isInsecure(): Boolean = isRooted || isEmulator
        
        /**
         * Get comma-separated string of root detection methods.
         */
        fun getRootDetectionMethodsString(): String {
            return rootChecks
                .filter { it.second }
                .joinToString(", ") { it.first }
        }
        
        /**
         * Get comma-separated string of emulator detection methods.
         */
        fun getEmulatorDetectionMethodsString(): String {
            return emulatorChecks
                .filter { it.second }
                .joinToString(", ") { it.first }
        }
        
        /**
         * Get human-readable security status.
         */
        fun getSecurityStatus(): String {
            return when {
                isRooted && isEmulator -> "Rooted Device & Emulator"
                isRooted -> "Rooted Device"
                isEmulator -> "Emulator"
                else -> "Secure"
            }
        }
    }
    
    /**
     * Legacy data class for backward compatibility.
     * 
     * @property isRooted Overall result - true if device is rooted
     * @property checks List of individual check results with method name and result
     */
    data class RootCheckResult(
        val isRooted: Boolean,
        val checks: List<Pair<String, Boolean>>
    ) {
        /**
         * Get comma-separated string of methods that detected root.
         */
        fun getDetectedMethodsString(): String {
            return checks
                .filter { it.second }
                .joinToString(", ") { it.first }
        }
    }
}


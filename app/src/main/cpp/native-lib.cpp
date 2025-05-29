#include <jni.h>
#include <string>
#include <android/log.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <dirent.h>
#include <dlfcn.h>
#include <time.h>
#include <pthread.h>
#include <cstring>
#include <sys/system_properties.h>
#include <sys/prctl.h>

#define TAG "SecureNativeLib"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// Security check constants
#define TRACER_CHECK_INTERVAL_MS 250
#define HOOK_CHECK_INTERVAL_MS 500
#define MAX_SUSPICIOUS_PACKAGES 20
#define MAX_PACKAGE_NAME_LEN 128

// Global variables
static JavaVM* gJavaVM = nullptr;
static bool gSecurityThreadRunning = false;
static pthread_t gSecurityThread;
static pthread_mutex_t gSecurityMutex = PTHREAD_MUTEX_INITIALIZER;
static int gSecurityLevel = 1; // Default security level
static time_t gLastEmulatorCheck = 0;
static bool gIsEmulator = false;

// Forward declarations
void* securityMonitorThread(void* args);
bool detectEmulator();
bool detectVirtualEnvironment();
void obfuscateMemory();
bool blockPtraceAttach();
bool detectMagiskHooks();

// JNI_OnLoad is called when the library is loaded
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGD("Native library loaded");
    gJavaVM = vm;
    
    // Start security monitor thread
    pthread_mutex_lock(&gSecurityMutex);
    if (!gSecurityThreadRunning) {
        gSecurityThreadRunning = true;
        pthread_create(&gSecurityThread, nullptr, securityMonitorThread, nullptr);
        LOGD("Security monitor thread started");
    }
    pthread_mutex_unlock(&gSecurityMutex);
    
    return JNI_VERSION_1_6;
}

// Check if the process is being traced or debugged
bool isBeingTraced() {
    char buf[512];
    int fd = open("/proc/self/status", O_RDONLY);
    if (fd < 0) {
        return false;
    }
    
    const char* tracer_needle = "TracerPid:";
    ssize_t num_read = read(fd, buf, sizeof(buf) - 1);
    close(fd);
    
    if (num_read <= 0) {
        return false;
    }
    
    buf[num_read] = '\0';
    char* tracer_pid_line = strstr(buf, tracer_needle);
    if (!tracer_pid_line) {
        return false;
    }
    
    int tracer_pid = 0;
    if (sscanf(tracer_pid_line + strlen(tracer_needle), "%d", &tracer_pid) != 1) {
        return false;
    }
    
    return tracer_pid != 0;
}

// Block attempts to attach ptrace to our process
bool blockPtraceAttach() {
    // Set PR_SET_DUMPABLE to 0 to prevent ptrace attachment
    if (prctl(PR_SET_DUMPABLE, 0, 0, 0, 0) == 0) {
        LOGD("Successfully disabled ptrace attachment");
        return true;
    } else {
        LOGE("Failed to disable ptrace attachment: %s", strerror(errno));
        return false;
    }
}

// Check for hooking frameworks like Frida, Xposed, etc.
bool isHookingFrameworkPresent() {
    // Check for common libraries used by hooking frameworks
    void* xposed_handle = dlopen("libxposed_art.so", RTLD_NOW);
    if (xposed_handle) {
        dlclose(xposed_handle);
        return true;
    }
    
    void* substrate_handle = dlopen("libsubstrate.so", RTLD_NOW);
    if (substrate_handle) {
        dlclose(substrate_handle);
        return true;
    }
    
    // Check for Frida
    DIR* dir = opendir("/proc/self/maps");
    if (dir) {
        struct dirent* entry;
        while ((entry = readdir(dir)) != nullptr) {
            if (strstr(entry->d_name, "frida")) {
                closedir(dir);
                return true;
            }
        }
        closedir(dir);
    }
    
    // Check for Magisk hooks
    if (detectMagiskHooks()) {
        return true;
    }
    
    return false;
}

// Detect Magisk hooks and hiding methods
bool detectMagiskHooks() {
    // Check for Magisk-related files
    const char* magisk_paths[] = {
        "/sbin/.magisk",
        "/sbin/.core",
        "/data/adb/magisk",
        "/cache/.disable_magisk",
        "/dev/.magisk.db"
    };
    
    for (const char* path : magisk_paths) {
        struct stat s;
        if (stat(path, &s) == 0) {
            LOGD("Detected Magisk file: %s", path);
            return true;
        }
    }
    
    // Check for Magisk props
    char prop_value[PROP_VALUE_MAX];
    __system_property_get("ro.magisk.hide", prop_value);
    if (strlen(prop_value) > 0) {
        return true;
    }
    
    return false;
}

// Detect emulators
bool detectEmulator() {
    // Only check every 60 seconds to reduce overhead
    time_t now = time(nullptr);
    if (now - gLastEmulatorCheck < 60 && gLastEmulatorCheck != 0) {
        return gIsEmulator;
    }
    
    gLastEmulatorCheck = now;
    bool result = false;
    
    // Method 1: Check build properties
    char prop_value[PROP_VALUE_MAX];
    
    // Common emulator indicators in system properties
    const char* emulator_props[] = {
        "ro.hardware",          // Contains "goldfish" or "ranchu" for emulators
        "ro.product.model",     // Contains "sdk" or "emulator" for emulators
        "ro.product.manufacturer", // Contains "Genymotion" or similar
        "ro.kernel.qemu",       // Set to "1" for QEMU-based emulators
        "ro.bootloader",        // Contains "unknown" for emulators
        "ro.bootmode",          // Contains "unknown" for emulators
        "ro.build.characteristics" // Contains "emulator" for emulators
    };
    
    // Known emulator values for various properties
    const char* emulator_values[] = {
        "goldfish", "ranchu", "sdk", "sdk_gphone", "emulator", 
        "Genymotion", "vbox", "nox", "bluestacks", "android_x86"
    };
    
    for (const char* prop : emulator_props) {
        __system_property_get(prop, prop_value);
        for (const char* value : emulator_values) {
            if (strstr(prop_value, value) != nullptr) {
                LOGD("Emulator detected via property %s = %s", prop, prop_value);
                result = true;
                goto end_check;
            }
        }
    }
    
    // Method 2: Check for emulator-specific files
    const char* emulator_files[] = {
        "/dev/socket/qemud",
        "/dev/qemu_pipe",
        "/system/lib/libc_malloc_debug_qemu.so",
        "/sys/qemu_trace",
        "/system/bin/qemu-props",
        "/dev/socket/genyd",
        "/dev/socket/baseband_genyd"
    };
    
    for (const char* file : emulator_files) {
        struct stat s;
        if (stat(file, &s) == 0) {
            LOGD("Emulator detected via file: %s", file);
            result = true;
            goto end_check;
        }
    }
    
end_check:
    gIsEmulator = result;
    return result;
}

// Detect timing anomalies that might indicate dynamic analysis
bool detectTimingAnomaly() {
    struct timespec start, end;
    clock_gettime(CLOCK_MONOTONIC, &start);
    
    // Perform a complex operation that should take a predictable time
    volatile int sum = 0;
    for (int i = 0; i < 10000; i++) {
        sum += i * i;
    }
    
    clock_gettime(CLOCK_MONOTONIC, &end);
    long elapsed_ns = (end.tv_sec - start.tv_sec) * 1000000000 + (end.tv_nsec - start.tv_nsec);
    
    // If it took more than 10ms, it might be under analysis
    return elapsed_ns > 10000000;
}

// Detect virtual environment or containers
bool detectVirtualEnvironment() {
    // Check for container-specific markers
    const char* container_files[] = {
        "/.dockerenv",
        "/dev/lxc",
        "/dev/vboxguest",
        "/dev/vboxuser"
    };
    
    for (const char* file : container_files) {
        struct stat s;
        if (stat(file, &s) == 0) {
            LOGD("Virtual environment detected via file: %s", file);
            return true;
        }
    }
    
    return false;
}

// Obfuscate memory to prevent memory scanning
void obfuscateMemory() {
    // This is a simplified example - real implementation would
    // scramble and unscramble important memory regions
    
    // Create a buffer with random data to confuse memory scanners
    const int BUFFER_SIZE = 1024; // 1KB of noise
    char* noise_buffer = new char[BUFFER_SIZE];
    
    for (int i = 0; i < BUFFER_SIZE; i++) {
        noise_buffer[i] = (char)(rand() % 256);
    }
    
    // Don't delete the buffer - keep it allocated to maintain the noise
}

// Set the security level for the native components
void setSecurityLevel(int level) {
    if (level >= 1 && level <= 3) {
        gSecurityLevel = level;
        LOGD("Native security level set to: %d", level);
    }
}

// Process name spoofing function
bool spoofProcessName(const char* new_name) {
    // Use prctl to change the process name
    if (prctl(PR_SET_NAME, (unsigned long)new_name, 0, 0, 0) == 0) {
        LOGD("Process name changed to: %s", new_name);
        return true;
    } else {
        LOGE("Failed to change process name: %s", strerror(errno));
        return false;
    }
    
    // Additionally, we could write to /proc/self/comm
    // but that requires root permissions in most cases
    return false;
}

// Hide process information
bool hideProcessInfo(int pid) {
    // This is limited on non-rooted devices, but we can try to mask some information
    
    // Spoof process command line if possible
    const char* fake_cmdline = "system_server";
    int fd = open("/proc/self/cmdline", O_WRONLY);
    if (fd >= 0) {
        write(fd, fake_cmdline, strlen(fake_cmdline) + 1); // Include null terminator
        close(fd);
    }
    
    // For now, just return true to indicate we've done what we can
    return true;
}

// Security monitor thread function
void* securityMonitorThread(void* args) {
    int check_counter = 0;
    
    while (gSecurityThreadRunning) {
        // Basic checks run on every iteration
        bool traced = isBeingTraced();
        bool hooked = isHookingFrameworkPresent();
        
        // Less frequent checks (every 10 iterations)
        bool in_emulator = false;
        bool in_virtual_env = false;
        if (check_counter % 10 == 0) {
            in_emulator = detectEmulator();
            in_virtual_env = detectVirtualEnvironment();
        }
        
        // Timing anomaly check (every 5 iterations)
        bool timing_anomaly = false;
        if (check_counter % 5 == 0) {
            timing_anomaly = detectTimingAnomaly();
        }
        
        // If any threats are detected, take action based on security level
        if (traced || hooked || timing_anomaly || in_emulator || in_virtual_env) {
            LOGD("Security threat detected, applying protections at level %d", gSecurityLevel);
            
            if (gSecurityLevel >= 3) {
                // Level 3: Maximum protection
                obfuscateMemory();
                blockPtraceAttach();
            } 
            else if (gSecurityLevel >= 2) {
                // Level 2: Enhanced protection
                blockPtraceAttach();
            }
            
            // Always apply basic protection
            if (check_counter % 20 == 0) { // Periodically change process name
                char fake_name[16];
                const char* system_procs[] = {"system_server", "zygote", "media", "surfaceflinger"};
                int idx = check_counter % 4;
                spoofProcessName(system_procs[idx]);
            }
        }
        
        // Change sleep time based on security level
        int sleep_time = TRACER_CHECK_INTERVAL_MS;
        switch (gSecurityLevel) {
            case 3: sleep_time = 100; break;  // Check very frequently
            case 2: sleep_time = 200; break;  // Check frequently
            case 1: sleep_time = 300; break;  // Check normally
            default: sleep_time = 300;
        }
        
        usleep(sleep_time * 1000);
        check_counter++;
    }
    
    return nullptr;
}

extern "C" {

// ProcessIsolation implementations
JNIEXPORT jboolean JNICALL
Java_com_aiassistant_security_ProcessIsolation_nativeSpoofProcessName(JNIEnv *env, jobject thiz, jstring name) {
    const char* new_name = env->GetStringUTFChars(name, nullptr);
    bool result = spoofProcessName(new_name);
    env->ReleaseStringUTFChars(name, new_name);
    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_aiassistant_security_ProcessIsolation_nativeHideProcessInfo(JNIEnv *env, jobject thiz, jint pid) {
    return hideProcessInfo(pid) ? JNI_TRUE : JNI_FALSE;
}

// AIStateManager native implementations
JNIEXPORT jboolean JNICALL
Java_com_aiassistant_core_ai_AIStateManager_isBeingAnalyzedNative(JNIEnv *env, jobject thiz) {
    // Combine multiple detection techniques
    bool traced = isBeingTraced();
    bool hooked = isHookingFrameworkPresent();
    bool timing_anomaly = detectTimingAnomaly();
    bool in_emulator = detectEmulator();
    
    // If security level is high, be more suspicious
    if (gSecurityLevel >= 3) {
        return (traced || hooked || timing_anomaly || in_emulator) ? JNI_TRUE : JNI_FALSE;
    } else if (gSecurityLevel >= 2) {
        return (traced || hooked || timing_anomaly) ? JNI_TRUE : JNI_FALSE;
    } else {
        return (traced || hooked) ? JNI_TRUE : JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_aiassistant_core_ai_AIStateManager_isAppInForegroundNative(JNIEnv *env, jobject thiz, jstring package_name) {
    // This is a placeholder. Real implementation would check activity manager
    const char* pkg = env->GetStringUTFChars(package_name, nullptr);
    LOGD("Checking if app is in foreground: %s", pkg);
    env->ReleaseStringUTFChars(package_name, pkg);
    
    // For now, return true by default
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_aiassistant_security_AntiDetectionManager_initializeNativeSecurity(JNIEnv *env, jobject thiz) {
    LOGD("Initializing native security components");
    // Block ptrace attachments
    blockPtraceAttach();
    
    // Run initial security checks
    bool is_emulator = detectEmulator();
    bool has_hooks = isHookingFrameworkPresent();
    
    // Set initial security level based on environment
    if (is_emulator || has_hooks) {
        setSecurityLevel(3); // Maximum security in suspicious environments
    } else {
        setSecurityLevel(1); // Normal security for regular environments
    }
}

JNIEXPORT void JNICALL
Java_com_aiassistant_security_SecurityContext_setNativeSecurityLevel(JNIEnv *env, jobject thiz, jint level) {
    setSecurityLevel(level);
}

JNIEXPORT jboolean JNICALL
Java_com_aiassistant_security_AntiDetectionManager_isRunningInEmulator(JNIEnv *env, jobject thiz) {
    return detectEmulator() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_aiassistant_security_AntiDetectionManager_hasHookFramework(JNIEnv *env, jobject thiz) {
    return isHookingFrameworkPresent() ? JNI_TRUE : JNI_FALSE;
}

} // extern "C"

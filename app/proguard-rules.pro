# Regras ProGuard / R8 de Segurança e Ofuscação

# Preserva serviços essenciais do Android
-keep public class com.neyvan.desinstalador.accessibility.AutoUninstallAccessibilityService { *; }
-keep public class com.neyvan.desinstalador.MainActivity { *; }

# Remove chamadas de log em builds de release (Prevenção de vazamento de informações)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Otimização agressiva e ofuscação
-repackageclasses 'com.neyvan.desinstalador.obf'
-allowaccessmodification

package cn.chaosmp.camerafix.fabric;

enum FabricRuntimeNamespace {
    INTERMEDIARY,
    NAMED;

    static FabricRuntimeNamespace detect() {
        if (classExists("net.minecraft.client.Minecraft")) {
            return NAMED;
        }
        if (classExists("net.minecraft.class_310")) {
            return INTERMEDIARY;
        }
        throw new IllegalStateException("Unable to detect Fabric runtime namespace");
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name, false, FabricRuntimeNamespace.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }
}

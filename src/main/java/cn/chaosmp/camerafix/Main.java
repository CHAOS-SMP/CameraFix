package cn.chaosmp.camerafix;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class Main {
    public static final String MOD_ID = "camerafix";
    public static final String MOD_NAME = "CameraFix";
    public static volatile boolean ENABLE_HOOK = true;
    private static final Class<?> MINECRAFT_CLASS = findClass("net.minecraft.client.Minecraft", "net.minecraft.class_310");
    private static final Method MINECRAFT_GET_INSTANCE = findStaticMethod(MINECRAFT_CLASS, new Class<?>[0], "getInstance", "method_1551");
    private static final Method MINECRAFT_IS_SINGLEPLAYER = findMethod(MINECRAFT_CLASS, new Class<?>[0], "isSingleplayer", "method_47392");
    private static final Method MINECRAFT_GET_CHAT_LISTENER = findMethod(MINECRAFT_CLASS, new Class<?>[0], "getChatListener", "method_44714");
    private static final Field MINECRAFT_GUI_FIELD = findField(MINECRAFT_CLASS, "gui", "field_1705");
    private static final Field MINECRAFT_PLAYER_FIELD = findField(MINECRAFT_CLASS, "player", "field_1724");
    private static final Class<?> COMPONENT_CLASS = findClass("net.minecraft.network.chat.Component", "net.minecraft.class_2561");
    private static final Method COMPONENT_LITERAL = findStaticMethod(COMPONENT_CLASS, new Class<?>[]{String.class}, "literal", "method_43470");

    private Main() {
    }

    public static boolean shouldUseProtocol() {
        return ENABLE_HOOK;
    }

    public static void logBanner() {
        System.out.println("COPYRIGHT 2026@CHAOS-SMP");
        System.out.println("POWERED BY CHAOS-SMP (chaos-smp.cn)");
    }

    public static void onClientJoin() {
        sendSystemMessage("COPYRIGHT 2026@CHAOS-SMP");
        sendSystemMessage("POWERED BY CHAOS-SMP (chaos-smp.cn)");
    }

    public static String setHookEnabled(boolean enabled) {
        ENABLE_HOOK = enabled;
        return enabled ? "ON" : "OFF";
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void registerClientCommand(CommandDispatcher<?> dispatcher) {
        if (dispatcher.getRoot().getChild("cmr") != null) {
            return;
        }
        var root = (com.mojang.brigadier.builder.LiteralArgumentBuilder) CommandsHelper.literal("cmr");
        var argument = (com.mojang.brigadier.builder.RequiredArgumentBuilder) CommandsHelper.argument("value", BoolArgumentType.bool());
        argument.executes(context -> {
            boolean flag = BoolArgumentType.getBool(context, "value");
            String status = setHookEnabled(flag);
            sendSystemMessage(status);
            return Command.SINGLE_SUCCESS;
        });
        root.then(argument);
        ((CommandDispatcher) dispatcher).register(root);
    }

    private static boolean isSingleplayer() {
        Object minecraft = minecraftInstance();
        return (Boolean) invoke(MINECRAFT_IS_SINGLEPLAYER, minecraft);
    }

    private static Object minecraftInstance() {
        return invoke(MINECRAFT_GET_INSTANCE, null);
    }

    private static void sendSystemMessage(String text) {
        Object minecraft = minecraftInstance();
        Object component = invoke(COMPONENT_LITERAL, null, text);

        Object chatListener = resolveChatListener(minecraft);
        if (chatListener != null) {
            Method method = findMethod(chatListener.getClass(), new Class<?>[]{COMPONENT_CLASS, boolean.class}, "handleSystemMessage", "method_44736");
            if (method != null) {
                invoke(method, chatListener, component, false);
                return;
            }
        }

        Object player = MINECRAFT_PLAYER_FIELD == null ? null : get(MINECRAFT_PLAYER_FIELD, minecraft);
        if (player != null) {
            Method systemMethod = findMethod(player.getClass(), new Class<?>[]{COMPONENT_CLASS}, "sendSystemMessage");
            if (systemMethod != null) {
                invoke(systemMethod, player, component);
                return;
            }
            Method displayMethod = findMethod(player.getClass(), new Class<?>[]{COMPONENT_CLASS, boolean.class}, "displayClientMessage");
            if (displayMethod != null) {
                invoke(displayMethod, player, component, false);
                return;
            }
        }

        throw new IllegalStateException("No compatible client message sink found");
    }

    private static Object invoke(Method method, Object owner, Object... args) {
        if (method == null) {
            throw new IllegalStateException("Attempted to invoke missing method");
        }
        try {
            return method.invoke(owner, args);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to invoke " + method, exception);
        }
    }

    private static Object get(Field field, Object owner) {
        if (field == null) {
            return null;
        }
        try {
            return field.get(owner);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read " + field, exception);
        }
    }

    private static Class<?> findClass(String... names) {
        for (String name : names) {
            try {
                return Class.forName(name, false, Main.class.getClassLoader());
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }

    private static Field findField(Class<?> type, String... names) {
        if (type == null) {
            return null;
        }
        for (String name : names) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
            try {
                Field field = type.getField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    private static Method findStaticMethod(Class<?> type, Class<?>[] parameterTypes, String... names) {
        Method method = findMethod(type, parameterTypes, names);
        if (method == null || !java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
            return null;
        }
        return method;
    }

    private static Method findMethod(Class<?> type, Class<?>[] parameterTypes, String... names) {
        if (type == null) {
            return null;
        }
        for (String name : names) {
            try {
                Method method = type.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
            }
            try {
                Method method = type.getMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }

    private static Object resolveChatListener(Object minecraft) {
        if (MINECRAFT_GET_CHAT_LISTENER != null) {
            return invoke(MINECRAFT_GET_CHAT_LISTENER, minecraft);
        }

        Object gui = MINECRAFT_GUI_FIELD == null ? null : get(MINECRAFT_GUI_FIELD, minecraft);
        if (gui == null) {
            return null;
        }

        Method guiChatListener = findMethod(gui.getClass(), new Class<?>[0], "chatListener");
        if (guiChatListener != null) {
            return invoke(guiChatListener, gui);
        }

        return null;
    }
}

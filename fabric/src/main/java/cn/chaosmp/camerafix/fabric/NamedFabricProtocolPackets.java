package cn.chaosmp.camerafix.fabric;

import cn.chaosmp.camerafix.util.ProtocolPackets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

final class NamedFabricProtocolPackets {
    private static final String NAMESPACE = "neko";
    private static final String PLACE_PATH = "place";
    private static final String INTERACT_PATH = "interact";
    private static final Class<?> IDENTIFIER_CLASS = NamedRuntimeCompat.requireClass("net.minecraft.resources.Identifier");
    private static final Class<?> TYPE_CLASS = NamedRuntimeCompat.requireClass("net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type");
    private static final Class<?> CUSTOM_PAYLOAD_CLASS = NamedRuntimeCompat.requireClass("net.minecraft.network.protocol.common.custom.CustomPacketPayload");
    private static final Class<?> STREAM_MEMBER_ENCODER_CLASS = NamedRuntimeCompat.requireClass("net.minecraft.network.codec.StreamMemberEncoder");
    private static final Class<?> STREAM_DECODER_CLASS = NamedRuntimeCompat.requireClass("net.minecraft.network.codec.StreamDecoder");
    private static final Class<?> CLIENT_PLAY_NETWORKING_CLASS = NamedRuntimeCompat.requireClass("net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking");
    private static final Class<?> PAYLOAD_TYPE_REGISTRY_CLASS = NamedRuntimeCompat.requireClass("net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry");
    private static final Method IDENTIFIER_FROM_NAMESPACE_AND_PATH =
            NamedRuntimeCompat.requireMethod(IDENTIFIER_CLASS, new Class<?>[]{String.class, String.class}, "fromNamespaceAndPath");
    private static final Constructor<?> TYPE_CONSTRUCTOR = requireConstructor(TYPE_CLASS, IDENTIFIER_CLASS);
    private static final Method CUSTOM_PAYLOAD_CODEC_FACTORY =
            NamedRuntimeCompat.requireMethod(CUSTOM_PAYLOAD_CLASS, new Class<?>[]{STREAM_MEMBER_ENCODER_CLASS, STREAM_DECODER_CLASS}, "codec");
    private static final Method SERVERBOUND_PLAY_METHOD =
            NamedRuntimeCompat.requireMethod(PAYLOAD_TYPE_REGISTRY_CLASS, new Class<?>[0], "serverboundPlay");
    private static final Method CLIENT_SEND_METHOD =
            NamedRuntimeCompat.requireMethod(CLIENT_PLAY_NETWORKING_CLASS, new Class<?>[]{CUSTOM_PAYLOAD_CLASS}, "send");
    private static final Object PLACE_TYPE = createType(PLACE_PATH);
    private static final Object INTERACT_TYPE = createType(INTERACT_PATH);
    private static final Object USE_ITEM_ON_PACKET_CODEC =
            NamedRuntimeCompat.get(requireStaticField(NamedRuntimeCompat.USE_ITEM_ON_PACKET_CLASS, "STREAM_CODEC"), null);
    private static final Object USE_ITEM_PACKET_CODEC =
            NamedRuntimeCompat.get(requireStaticField(NamedRuntimeCompat.USE_ITEM_PACKET_CLASS, "STREAM_CODEC"), null);
    private static final Method CODEC_ENCODE_METHOD =
            NamedRuntimeCompat.requireMethod(USE_ITEM_ON_PACKET_CODEC.getClass(), new Class<?>[]{Object.class, Object.class}, "encode");
    private static final Method CODEC_DECODE_METHOD =
            NamedRuntimeCompat.requireMethod(USE_ITEM_ON_PACKET_CODEC.getClass(), new Class<?>[]{Object.class}, "decode");
    private static final Method READ_FLOAT_METHOD = requireMethodByName("net.minecraft.network.FriendlyByteBuf", "readFloat");
    private static final Method READ_BOOLEAN_METHOD = requireMethodByName("net.minecraft.network.FriendlyByteBuf", "readBoolean");
    private static final Method WRITE_FLOAT_METHOD = requireMethodByName("net.minecraft.network.FriendlyByteBuf", "writeFloat", float.class);
    private static final Method WRITE_BOOLEAN_METHOD = requireMethodByName("net.minecraft.network.FriendlyByteBuf", "writeBoolean", boolean.class);

    private NamedFabricProtocolPackets() {
    }

    static void register() {
        Object registry = NamedRuntimeCompat.invoke(SERVERBOUND_PLAY_METHOD, null);
        Method registerMethod = findRegisterMethod(registry.getClass());
        NamedRuntimeCompat.invoke(registerMethod, registry, PLACE_TYPE, createCodec(PLACE_TYPE, USE_ITEM_ON_PACKET_CODEC));
        NamedRuntimeCompat.invoke(registerMethod, registry, INTERACT_TYPE, createCodec(INTERACT_TYPE, USE_ITEM_PACKET_CODEC));
    }

    static void initSender() {
        ProtocolPackets.initSender((useItemOn, useItem, yaw, pitch, sneak) -> {
            if (useItemOn != null) {
                send(createPayload(PLACE_TYPE, useItemOn, yaw, pitch, sneak));
                return true;
            }
            if (useItem != null) {
                send(createPayload(INTERACT_TYPE, useItem, yaw, pitch, sneak));
                return true;
            }
            return false;
        });
    }

    private static void send(Object payload) {
        NamedRuntimeCompat.invoke(CLIENT_SEND_METHOD, null, payload);
    }

    private static Object createType(String path) {
        try {
            Object id = IDENTIFIER_FROM_NAMESPACE_AND_PATH.invoke(null, NAMESPACE, path);
            return TYPE_CONSTRUCTOR.newInstance(id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create named payload type", exception);
        }
    }

    private static Object createCodec(Object type, Object packetCodec) {
        Object encoder = Proxy.newProxyInstance(
                NamedFabricProtocolPackets.class.getClassLoader(),
                new Class<?>[]{STREAM_MEMBER_ENCODER_CLASS},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return handleObjectMethod(proxy, method, args);
                    }
                    PayloadData data = payloadData(args[0]);
                    Object buf = args[1];
                    encodePacket(packetCodec, buf, data.packet());
                    writeFloat(buf, data.yaw());
                    writeFloat(buf, data.pitch());
                    writeBoolean(buf, data.sneak());
                    return null;
                }
        );
        Object decoder = Proxy.newProxyInstance(
                NamedFabricProtocolPackets.class.getClassLoader(),
                new Class<?>[]{STREAM_DECODER_CLASS},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return handleObjectMethod(proxy, method, args);
                    }
                    Object buf = args[0];
                    Object packet = decodePacket(packetCodec, buf);
                    float yaw = readFloat(buf);
                    float pitch = readFloat(buf);
                    boolean sneak = readBoolean(buf);
                    return createPayload(type, packet, yaw, pitch, sneak);
                }
        );
        return NamedRuntimeCompat.invoke(CUSTOM_PAYLOAD_CODEC_FACTORY, null, encoder, decoder);
    }

    private static Object createPayload(Object type, Object packet, float yaw, float pitch, boolean sneak) {
        return Proxy.newProxyInstance(
                NamedFabricProtocolPackets.class.getClassLoader(),
                new Class<?>[]{CUSTOM_PAYLOAD_CLASS},
                new PayloadHandler(new PayloadData(type, packet, yaw, pitch, sneak))
        );
    }

    private static PayloadData payloadData(Object payload) {
        return ((PayloadHandler) Proxy.getInvocationHandler(payload)).data();
    }

    private static void encodePacket(Object codec, Object buf, Object packet) {
        NamedRuntimeCompat.invoke(CODEC_ENCODE_METHOD, codec, buf, packet);
    }

    private static Object decodePacket(Object codec, Object buf) {
        return NamedRuntimeCompat.invoke(CODEC_DECODE_METHOD, codec, buf);
    }

    private static float readFloat(Object buf) {
        return ((Float) NamedRuntimeCompat.invoke(READ_FLOAT_METHOD, buf)).floatValue();
    }

    private static boolean readBoolean(Object buf) {
        return (Boolean) NamedRuntimeCompat.invoke(READ_BOOLEAN_METHOD, buf);
    }

    private static void writeFloat(Object buf, float value) {
        NamedRuntimeCompat.invoke(WRITE_FLOAT_METHOD, buf, value);
    }

    private static void writeBoolean(Object buf, boolean value) {
        NamedRuntimeCompat.invoke(WRITE_BOOLEAN_METHOD, buf, value);
    }

    private static Method findRegisterMethod(Class<?> type) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals("register") && method.getParameterCount() == 2) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new IllegalStateException("Named payload registry register method not found: " + type.getName());
    }

    private static Constructor<?> requireConstructor(Class<?> type, Class<?>... parameterTypes) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException("Missing constructor on " + type.getName(), exception);
        }
    }

    private static Field requireStaticField(Class<?> type, String name) {
        return NamedRuntimeCompat.requireField(type, name);
    }

    private static Method requireMethodByName(String ownerClassName, String name, Class<?>... parameterTypes) {
        Class<?> type = NamedRuntimeCompat.requireClass(ownerClassName);
        return NamedRuntimeCompat.requireMethod(type, parameterTypes, name);
    }

    private static Object handleObjectMethod(Object proxy, Method method, Object[] args) {
        return switch (method.getName()) {
            case "toString" -> "CameraFixNamedPayloadProxy";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> null;
        };
    }

    private record PayloadData(Object type, Object packet, float yaw, float pitch, boolean sneak) {
    }

    private record PayloadHandler(PayloadData data) implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if (method.getDeclaringClass() == Object.class) {
                return handleObjectMethod(proxy, method, args);
            }
            if (method.getName().equals("type") && method.getParameterCount() == 0) {
                return data.type();
            }
            throw new UnsupportedOperationException("Unsupported CustomPacketPayload method: " + method);
        }
    }
}

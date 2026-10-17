/*
 * Dedicated-server check: lists methods in server-loaded classes that reference client-only code
 * (net.minecraft.client.*, blaze3d, NeoForge client, our @OnlyIn(CLIENT) classes and members).
 * Guards like `if (level.isClientSide)` are invisible here, so every hit needs a look at the source.
 *
 * Run after ./gradlew compileJava (Java 21, ASM from the Gradle cache):
 *   A=$(find ~/.gradle/caches/modules-2 -name 'asm-9.8.jar' | head -1)
 *   T=$(find ~/.gradle/caches/modules-2 -name 'asm-tree-9.8.jar' | head -1)
 *   java -cp "$A:$T" tools/DistScan.java build/classes/java/main thaumcraft/Thaumcraft \
 *     thaumcraft/event/common/ThaumcraftLootTableEvents thaumcraft/event/common/ThaumcraftVillagerEvents \
 *     thaumcraft/event/common/CommandThaumcraft thaumcraft/init/registry/ModDimensions thaumcraft/init/registry/ModCreativeTab
 * Arguments after the classes dir are the server entry points (the mod class and common @EventBusSubscriber classes).
 */
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/** Finds references from common (server-loaded) code to client-only classes and members. */
public class DistScan {
    static final String ONLY_IN = "Lnet/neoforged/api/distmarker/OnlyIn;";
    static Map<String, ClassNode> classes = new HashMap<>();
    static Set<String> clientClasses = new HashSet<>();
    static Set<String> clientMembers = new HashSet<>(); // owner.name+desc

    static boolean isClient(List<AnnotationNode> anns) {
        if (anns == null) return false;
        for (AnnotationNode a : anns) {
            if (ONLY_IN.equals(a.desc) && a.values != null) {
                for (int i = 0; i < a.values.size(); i += 2)
                    if ("value".equals(a.values.get(i)) && a.values.get(i + 1) instanceof String[] e && "CLIENT".equals(e[1])) return true;
            }
        }
        return false;
    }

    static boolean anyClient(ClassNode c, MethodNode m) {
        return isClient(c.visibleAnnotations) || isClient(c.invisibleAnnotations) || (m != null && (isClient(m.visibleAnnotations) || isClient(m.invisibleAnnotations)));
    }

    static boolean vanillaClient(String internal) {
        return internal.startsWith("net/minecraft/client/") || internal.startsWith("com/mojang/blaze3d/") || internal.startsWith("net/neoforged/neoforge/client/") || internal.startsWith("com/mojang/realmsclient/");
    }

    static String strip(String t) {
        if (t == null) return null;
        Type ty = t.startsWith("[") || t.startsWith("L") ? Type.getType(t) : Type.getObjectType(t);
        while (ty.getSort() == Type.ARRAY) ty = ty.getElementType();
        return ty.getSort() == Type.OBJECT ? ty.getInternalName() : null;
    }

    static boolean clientType(String internal) {
        if (internal == null) return false;
        if (vanillaClient(internal)) return true;
        // nested in a client class
        String outer = internal;
        while (true) {
            if (clientClasses.contains(outer)) return true;
            int i = outer.lastIndexOf('$');
            if (i < 0) return false;
            outer = outer.substring(0, i);
        }
    }

    static boolean clientMember(String owner, String name, String desc) {
        // walk up our own hierarchy
        String o = owner;
        while (o != null && classes.containsKey(o)) {
            if (clientMembers.contains(o + "." + name + desc)) {
                return true;
            }
            o = classes.get(o).superName;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]);
        try (Stream<Path> s = Files.walk(root)) {
            for (Path p : s.filter(x -> x.toString().endsWith(".class")).toList()) {
                ClassNode cn = new ClassNode();
                new ClassReader(Files.readAllBytes(p)).accept(cn, ClassReader.SKIP_FRAMES);
                classes.put(cn.name, cn);
            }
        }
        for (ClassNode c : classes.values()) {
            if (anyClient(c, null)) clientClasses.add(c.name);
            for (MethodNode m : c.methods) if (isClient(m.visibleAnnotations) || isClient(m.invisibleAnnotations)) clientMembers.add(c.name + "." + m.name + m.desc);
            for (FieldNode f : c.fields) if (isClient(f.visibleAnnotations) || isClient(f.invisibleAnnotations)) clientMembers.add(c.name + "." + f.name + f.desc);
        }
        // Class-level reachability from server entry points, following only common methods.
        Set<String> skip = Set.of("thaumcraft/Thaumcraft.createClientSetup", "thaumcraft/Thaumcraft.clientSetup");
        Deque<String> work = new ArrayDeque<>();
        Set<String> reach = new HashSet<>();
        for (int i = 1; i < args.length; i++) work.add(args[i]);
        while (!work.isEmpty()) {
            String n = work.poll();
            if (!classes.containsKey(n) || clientType(n) || !reach.add(n)) continue;
            ClassNode c = classes.get(n);
            List<String> refs = new ArrayList<>();
            refs.add(c.superName);
            refs.addAll(c.interfaces);
            if (c.nestHostClass != null) refs.add(c.nestHostClass);
            for (MethodNode m : c.methods) {
                if (anyClient(c, m) || skip.contains(c.name + "." + m.name)) continue;
                for (AbstractInsnNode in : m.instructions) {
                    if (in instanceof MethodInsnNode mi) refs.add(strip(mi.owner));
                    else if (in instanceof FieldInsnNode fi) refs.add(strip(fi.owner));
                    else if (in instanceof TypeInsnNode ti) refs.add(strip(ti.desc));
                    else if (in instanceof LdcInsnNode li && li.cst instanceof Type t) refs.add(strip(t.getDescriptor()));
                    else if (in instanceof InvokeDynamicInsnNode id) for (Object a : id.bsmArgs) if (a instanceof Handle h) refs.add(h.getOwner());
                }
            }
            for (String r : refs) if (r != null && !reach.contains(r)) work.add(r);
        }
        System.err.println("reachable: " + reach.size() + " of " + classes.size());
        TreeMap<String, TreeSet<String>> out = new TreeMap<>();
        for (ClassNode c : classes.values()) {
            if (clientType(c.name) || !reach.contains(c.name)) continue;
            for (MethodNode m : c.methods) {
                if (anyClient(c, m)) continue;
                String where = c.name + "." + m.name;
                for (AbstractInsnNode in : m.instructions) {
                    String hit = null;
                    if (in instanceof MethodInsnNode mi) {
                        if (clientType(strip(mi.owner))) hit = "call " + mi.owner + "." + mi.name;
                        else if (clientMember(mi.owner, mi.name, mi.desc)) hit = "call @OnlyIn " + mi.owner + "." + mi.name + mi.desc;
                    } else if (in instanceof FieldInsnNode fi) {
                        if (clientType(strip(fi.owner))) hit = "field " + fi.owner + "." + fi.name;
                        else if (clientMember(fi.owner, fi.name, fi.desc)) hit = "field @OnlyIn " + fi.owner + "." + fi.name;
                    } else if (in instanceof TypeInsnNode ti) {
                        if (clientType(strip(ti.desc))) hit = "type " + ti.desc;
                    } else if (in instanceof LdcInsnNode li && li.cst instanceof Type t) {
                        if (clientType(strip(t.getDescriptor()))) hit = "ldc " + t.getInternalName();
                    } else if (in instanceof InvokeDynamicInsnNode id) {
                        for (Object a : id.bsmArgs) if (a instanceof Handle h && (clientType(h.getOwner()) || clientMember(h.getOwner(), h.getName(), h.getDesc()))) hit = "lambda " + h.getOwner() + "." + h.getName();
                    }
                    if (hit != null) out.computeIfAbsent(where, k -> new TreeSet<>()).add(hit);
                }
            }
        }
        out.forEach((k, v) -> System.out.println(k + "\n    " + String.join("\n    ", v)));
        System.err.println("methods with client refs: " + out.size() + ", client classes: " + clientClasses.size() + ", client members: " + clientMembers.size());
    }
}

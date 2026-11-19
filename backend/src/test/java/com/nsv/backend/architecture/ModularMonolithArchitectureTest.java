package com.nsv.backend.architecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ModularMonolithArchitectureTest {

    private static final String BASE_PACKAGE = "com.nsv.backend";
    private static final String SHARED_KERNEL = "common";
    private static final Set<String> BUSINESS_MODULES = Set.of(
        "user", "product", "inventory", "trace", "order", "dispute"
    );

    // Ma trận phụ thuộc có hướng không chu trình (DAG) được phép giữa các module nghiệp vụ
    private static final Map<String, Set<String>> ALLOWED_MODULE_DEPENDENCIES = Map.of(
        "user", Set.of(),
        "product", Set.of("user"),
        "inventory", Set.of("product"),
        "trace", Set.of("inventory", "product", "user"),
        "order", Set.of("inventory", "product", "user"),
        "dispute", Set.of("order", "inventory", "product", "trace", "user")
    );

    private static final Pattern IMPORT_PATTERN = Pattern.compile(
        "^\\s*import\\s+(?:static\\s+)?(com\\.nsv\\.backend\\.([a-zA-Z0-9_]+)(?:\\.([a-zA-Z0-9_]+))?[^;]*);",
        Pattern.MULTILINE
    );

    private Path resolveMainJavaRoot() {
        Path direct = Paths.get("src/main/java/com/nsv/backend");
        if (Files.exists(direct)) {
            return direct;
        }
        Path subModule = Paths.get("backend/src/main/java/com/nsv/backend");
        if (Files.exists(subModule)) {
            return subModule;
        }
        throw new IllegalStateException("Không tìm thấy thư mục mã nguồn src/main/java/com/nsv/backend");
    }

    @Test
    @DisplayName("TC-ARCH-01 (1): Mã nguồn được phân chia thành các domain module rõ ràng và có package-info khai báo ranh giới")
    void businessModules_ExistWithClearPackageBoundaries() {
        Path root = resolveMainJavaRoot();

        for (String module : BUSINESS_MODULES) {
            Path moduleDir = root.resolve(module);
            assertTrue(Files.isDirectory(moduleDir), "Thiếu thư mục module nghiệp vụ: " + module);
            assertTrue(
                Files.exists(moduleDir.resolve("package-info.java")),
                "Module " + module + " cần có package-info.java mô tả ranh giới"
            );
        }

        assertTrue(Files.isDirectory(root.resolve(SHARED_KERNEL)), "Thiếu thư mục Shared Kernel: common");
        assertTrue(Files.exists(root.resolve(SHARED_KERNEL).resolve("package-info.java")));
    }

    @Test
    @DisplayName("TC-ARCH-01 (2): Kiểm tra đồ thị phụ thuộc giữa các module không có chu trình (Acyclic DAG) và tuân thủ ma trận kiến trúc")
    void businessModules_HaveNoCyclicDependenciesAndFollowAllowedDag() throws IOException {
        Path root = resolveMainJavaRoot();
        Map<String, Set<String>> actualDependencyGraph = new HashMap<>();

        for (String module : BUSINESS_MODULES) {
            actualDependencyGraph.put(module, new HashSet<>());
        }

        try (Stream<Path> files = Files.walk(root)) {
            List<Path> javaFiles = files.filter(p -> p.toString().endsWith(".java")).toList();
            assertFalse(javaFiles.isEmpty(), "Phải có các tệp mã nguồn Java trong dự án");

            for (Path file : javaFiles) {
                Path relative = root.relativize(file);
                if (relative.getNameCount() < 2) {
                    continue;
                }
                String sourceModule = relative.getName(0).toString();
                String content = Files.readString(file, StandardCharsets.UTF_8);

                Matcher matcher = IMPORT_PATTERN.matcher(content);
                while (matcher.find()) {
                    String targetModule = matcher.group(2);
                    if (sourceModule.equals(targetModule)) {
                        continue;
                    }

                    // 1. Shared Kernel (common) không bao giờ được phụ thuộc ngược vào các domain module
                    if (SHARED_KERNEL.equals(sourceModule) && BUSINESS_MODULES.contains(targetModule)) {
                        fail("Vi phạm kiến trúc: Shared Kernel 'common' phụ thuộc ngược vào module '"
                            + targetModule + "' tại tệp " + relative);
                    }

                    // 2. Kiểm tra hướng phụ thuộc giữa các module nghiệp vụ tuân thủ DAG
                    if (BUSINESS_MODULES.contains(sourceModule) && BUSINESS_MODULES.contains(targetModule)) {
                        actualDependencyGraph.get(sourceModule).add(targetModule);
                        Set<String> allowed = ALLOWED_MODULE_DEPENDENCIES.getOrDefault(sourceModule, Set.of());
                        assertTrue(
                            allowed.contains(targetModule),
                            String.format(
                                "Vi phạm hướng phụ thuộc module: '%s' không được phép phụ thuộc vào '%s' (tệp %s)",
                                sourceModule, targetModule, relative
                            )
                        );
                    }
                }
            }
        }

        // 3. Kiểm tra không tồn tại chu trình phụ thuộc (Cycle detection bằng DFS)
        assertNoCycles(actualDependencyGraph);
    }

    @Test
    @DisplayName("TC-ARCH-01 (3): Kiểm tra ranh giới giao tiếp — các Public Module API (*ModuleApi) độc lập với entity/repository nội bộ")
    void publicModuleApis_DoNotExposeInternalEntitiesOrRepositories() throws IOException {
        Path root = resolveMainJavaRoot();
        List<String> expectedApiModules = List.of("user", "product", "inventory", "order");

        for (String module : expectedApiModules) {
            Path apiDir = root.resolve(module).resolve("api");
            assertTrue(Files.isDirectory(apiDir), "Module '" + module + "' phải có package 'api' công khai");

            try (Stream<Path> apiFiles = Files.walk(apiDir)) {
                List<Path> files = apiFiles.filter(p -> p.toString().endsWith(".java")).toList();
                assertFalse(files.isEmpty(), "Package 'api' của module '" + module + "' không được rỗng");

                for (Path file : files) {
                    String content = Files.readString(file, StandardCharsets.UTF_8);
                    Matcher matcher = IMPORT_PATTERN.matcher(content);
                    while (matcher.find()) {
                        String importedFull = matcher.group(1);
                        String targetModule = matcher.group(2);
                        String subLayer = matcher.group(3);

                        // API công khai của module không được làm rò rỉ entity/repository nội bộ
                        if (module.equals(targetModule) && ("entity".equals(subLayer) || "repository".equals(subLayer))) {
                            fail("Rò rỉ đóng gói tại " + root.relativize(file)
                                + ": Public API không được import '" + importedFull + "'");
                        }
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("TC-ARCH-01 (4): Kiểm tra phân tầng nội bộ trong từng module (Controller không gọi trực tiếp Repository)")
    void layeredArchitecture_ControllersDoNotAccessRepositoriesDirectly() throws IOException {
        Path root = resolveMainJavaRoot();

        try (Stream<Path> files = Files.walk(root)) {
            List<Path> controllerFiles = files
                .filter(p -> p.toString().endsWith("Controller.java"))
                .toList();

            for (Path controller : controllerFiles) {
                String content = Files.readString(controller, StandardCharsets.UTF_8);
                Matcher matcher = IMPORT_PATTERN.matcher(content);
                while (matcher.find()) {
                    String subLayer = matcher.group(3);
                    assertNotEquals(
                        "repository",
                        subLayer,
                        "Vi phạm phân tầng tại " + root.relativize(controller)
                            + ": Controller không được truy cập trực tiếp Repository"
                    );
                }
            }
        }
    }

    private void assertNoCycles(Map<String, Set<String>> graph) {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String node : graph.keySet()) {
            if (hasCycleDfs(node, graph, visited, recursionStack)) {
                fail("Phát hiện phụ thuộc vòng tròn (Cyclic Dependency) giữa các module nghiệp vụ!");
            }
        }
    }

    private boolean hasCycleDfs(
        String current,
        Map<String, Set<String>> graph,
        Set<String> visited,
        Set<String> recursionStack
    ) {
        if (recursionStack.contains(current)) {
            return true;
        }
        if (visited.contains(current)) {
            return false;
        }

        visited.add(current);
        recursionStack.add(current);

        for (String neighbor : graph.getOrDefault(current, Set.of())) {
            if (hasCycleDfs(neighbor, graph, visited, recursionStack)) {
                return true;
            }
        }

        recursionStack.remove(current);
        return false;
    }
}

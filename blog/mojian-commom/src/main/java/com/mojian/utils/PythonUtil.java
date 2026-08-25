package com.mojian.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Python 脚本执行工具类
 * <p>负责调用系统 Python 解释器执行脚本文件或代码片段，统一收集退出码、标准输出与错误输出，
 * 并内置超时机制防止脚本卡死。</p>
 *
 * @author: quequnlong
 * @date: 2026/8/24
 * @description:
 */
@Slf4j
public class PythonUtil {

    /** 默认脚本执行超时（秒） */
    private static final long DEFAULT_TIMEOUT_SECONDS = 60;

    /** Python 解释器探测顺序（Windows: py/python；Linux/macOS: python3/python） */
    private static final List<String> PYTHON_COMMANDS = Arrays.asList("python", "python3", "py");

    /** 已探测成功的解释器命令（缓存，避免每次执行都重复探测） */
    private static volatile String cachedPython;

    private PythonUtil() {
    }

    /**
     * 执行 Python 脚本文件
     *
     * @param scriptPath 脚本路径（绝对或相对路径）
     * @param args       传给脚本的参数（可空）
     * @return 执行结果
     */
    public static PythonResult execScript(String scriptPath, String... args) {
        return execScript(scriptPath, args, null);
    }

    /**
     * 执行 Python 脚本文件
     *
     * @param scriptPath  脚本路径（绝对或相对路径）
     * @param args        传给脚本的参数（可空）
     * @param workingDir  工作目录（可空，默认当前目录）
     * @return 执行结果
     */
    public static PythonResult execScript(String scriptPath, String[] args, String workingDir) {
        List<String> command = new ArrayList<>();
        command.add(detectPython());
        command.add(scriptPath);
        if (args != null) {
            command.addAll(Arrays.asList(args));
        }
        return exec(command, workingDir, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * 直接执行 Python 代码片段（python -c "...")
     *
     * @param code Python 代码
     * @return 执行结果
     */
    public static PythonResult execCode(String code) {
        List<String> command = new ArrayList<>();
        command.add(detectPython());
        command.add("-c");
        command.add(code);
        return exec(command, null, DEFAULT_TIMEOUT_SECONDS);
    }

    /**
     * 以完整命令执行 Python（自定义超时）
     *
     * @param command        完整命令列表（首个元素为解释器）
     * @param workingDir     工作目录（可空）
     * @param timeoutSeconds 超时秒数，超过则强制终止并返回 exitCode=-1
     * @return 执行结果
     */
    public static PythonResult exec(List<String> command, String workingDir, long timeoutSeconds) {
        PythonResult result = new PythonResult();
        result.setCommand(command);
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            if (workingDir != null && !workingDir.isEmpty()) {
                pb.directory(new File(workingDir));
            }
            process = pb.start();
            // 并发读取 stdout/stderr，避免输出缓冲区填满导致脚本阻塞
            StreamGobbler outGobbler = new StreamGobbler(process.getInputStream(), result::setStdout);
            StreamGobbler errGobbler = new StreamGobbler(process.getErrorStream(), result::setStderr);
            outGobbler.start();
            errGobbler.start();

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (finished) {
                result.setExitCode(process.exitValue());
            } else {
                process.destroyForcibly();
                result.setExitCode(-1);
                result.setStderr(appendLine(result.getStderr(), "Python 脚本执行超过 " + timeoutSeconds + " 秒，已强制终止"));
                log.warn("Python 脚本执行超时，已强制终止: command={}", command);
            }
            // 等待输出读取线程收尾
            outGobbler.join(5000);
            errGobbler.join(5000);
        } catch (IOException e) {
            result.setExitCode(-1);
            result.setStderr(appendLine(result.getStderr(), e.getMessage()));
            log.error("Python 执行失败: command={}, error={}", command, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.setExitCode(-1);
            result.setStderr(appendLine(result.getStderr(), e.getMessage()));
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
        }
        log.info("Python 脚本执行结果: command={}, exitCode={}, stdout={}, stderr={}", command, result.getExitCode(),
            result.getStdout(), result.getStderr());
        return result;
    }

    /**
     * 探测可用的 Python 解释器命令（成功结果会缓存）
     *
     * @return 解释器命令（python / python3 / py）
     * @throws IllegalStateException 未找到任何可用的解释器
     */
    public static String detectPython() {
        String cached = cachedPython;
        if (cached != null) {
            return cached;
        }
        for (String cmd : PYTHON_COMMANDS) {
            try {
                Process p = new ProcessBuilder(cmd, "--version").redirectErrorStream(true).start();
                if (p.waitFor(10, TimeUnit.SECONDS) && p.exitValue() == 0) {
                    cachedPython = cmd;
                    return cmd;
                }
                p.destroyForcibly();
            } catch (IOException | InterruptedException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new IllegalStateException("未找到可用的 Python 解释器（已尝试: python / python3 / py），请确认环境已安装 Python");
    }

    /**
     * 拼接错误信息（空值保护）
     */
    private static String appendLine(String origin, String line) {
        if (line == null || line.isEmpty()) {
            return origin;
        }
        String base = origin == null ? "" : origin;
        return base.isEmpty() ? line : base + "\n" + line;
    }

    /**
     * 后台读取进程输出流的线程，避免 stdout/stderr 缓冲区填满导致子进程阻塞
     */
    private static class StreamGobbler extends Thread {
        private final InputStream inputStream;
        private final Consumer<String> consumer;

        StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
            this.setDaemon(true);
        }

        @Override
        public void run() {
            try (InputStream in = inputStream; ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                byte[] buf = new byte[4096];
                int n;
                while ((n = in.read(buf)) != -1) {
                    buffer.write(buf, 0, n);
                }
                consumer.accept(new String(buffer.toByteArray()));
            } catch (IOException e) {
                log.warn("读取 Python 输出流失败: {}", e.getMessage());
            }
        }
    }

    /**
     * Python 执行结果：退出码 + 标准输出 + 错误输出
     */
    public static class PythonResult {
        private List<String> command;
        private int exitCode = -1;
        private String stdout = "";
        private String stderr = "";

        /** 是否执行成功（exitCode == 0） */
        public boolean isSuccess() {
            return exitCode == 0;
        }

        public List<String> getCommand() {
            return command;
        }

        public void setCommand(List<String> command) {
            this.command = command;
        }

        public int getExitCode() {
            return exitCode;
        }

        public void setExitCode(int exitCode) {
            this.exitCode = exitCode;
        }

        public String getStdout() {
            return stdout;
        }

        public void setStdout(String stdout) {
            this.stdout = stdout == null ? "" : stdout;
        }

        public String getStderr() {
            return stderr;
        }

        public void setStderr(String stderr) {
            this.stderr = stderr == null ? "" : stderr;
        }

        @Override
        public String toString() {
            return "PythonResult{" +
                    "command=" + command +
                    ", exitCode=" + exitCode +
                    ", stdout='" + stdout + '\'' +
                    ", stderr='" + stderr + '\'' +
                    '}';
        }
    }


    public static void main(String[] args) {
        PythonResult result = PythonUtil.execScript("E:\\python\\test.py"
                ,"综合管理部");
        System.out.println(result.getStdout());
    }
}

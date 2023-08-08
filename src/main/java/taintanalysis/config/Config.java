package taintanalysis.config;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import taintanalysis.entry.EntrySelectorManager;
import taintanalysis.rule.Rule;
import utils.FileUtil;
import utils.PathUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class Config {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private boolean autoAddEntry = true;

    private List<String> epoints = new ArrayList<>();

    //TODO Optimize deprecated. For now keep them to pass tests.
    @Deprecated
    private List<String> sources = new ArrayList<>();
    @Deprecated
    private List<String> sinks = new ArrayList<>();

    private String appPath;

    private List<String> libPaths = new ArrayList<>();

    private String libPath;

    private Set<String> excludes =new HashSet<>();

    private int pathReconstructionTimeout = 180;

    private int maxPathLength = 75;

    private String project;

    private String jdk;

    private boolean autoDetect = true;

    private String tempDir;

    private List<Rule> rules;

    private String callgraphAlgorithm = "CHA";

    private boolean isProjectAJar = false;

    private String entrySelector;

    private String pathCheckers;

    public String getPathCheckers() {
        return pathCheckers;
    }

    public void setPathCheckers(String pathCheckers) {
        this.pathCheckers = pathCheckers;
    }

    public String getEntrySelector() {
        return entrySelector;
    }

    public void setEntrySelector(String entrySelector) {
        this.entrySelector = entrySelector;
    }

    public boolean isProjectAJar() {
        return isProjectAJar;
    }

    public void setProjectAJar(boolean projectAJar) {
        isProjectAJar = projectAJar;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public String getCallgraphAlgorithm() {
        return callgraphAlgorithm;
    }

    public void setCallgraphAlgorithm(String callgraphAlgorithm) {
        this.callgraphAlgorithm = callgraphAlgorithm;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public boolean isAutoAddEntry() {
        return autoAddEntry;
    }

    public void setAutoAddEntry(boolean autoAddEntry) {
        this.autoAddEntry = autoAddEntry;
    }

    public void setAutoAddJspEntry(boolean autoAddJspEntry) {
        this.autoAddEntry = autoAddJspEntry;
    }

    public boolean isAutoDetect() {
        return autoDetect;
    }

    public void setAutoDetect(boolean autoDetect) {
        this.autoDetect = autoDetect;
    }


    public void autoConfig() {
        if (autoDetect) {
            if (project == null || project.isEmpty()) {
                throw new AssertionError("project must not be null or empty!");
            }
            File f = new File(project);
            if (!f.exists()) {
                throw new AssertionError("project not found!");
            }
            String extractTemp = null;
            if (!f.isDirectory()) {
                // TODO support jar file.
                if (!(f.getPath().endsWith(".jar") || f.getPath().endsWith(".zip"))) {
                    throw new AssertionError("project is not a directory and not a jar or zip!");
                }
                this.isProjectAJar = true;
                Path tmpdir = PathUtil.createTempdir();
                extractTemp = tempDir;
                logger.info("create temp dir for extract: {}", tmpdir);
                String projectName = FilenameUtils.removeExtension(Paths.get(project).getFileName().toString());
                Path workDir = Paths.get(tmpdir.toString(), projectName);
                FileUtil.extractJar(project, workDir.toString());
                this.project = workDir.toString();
            }
            // copy all .class to temporary directory.
            Path tmpdir = PathUtil.createTempdir();
            logger.info("create temp dir: {}", tmpdir);
            this.tempDir = tmpdir.toString();
            // filter all .class file
            List<String> classFiles = PathUtil.filterFile(project, new String[]{"**/*.class"});
            List<String> jarFiles = PathUtil.filterFile(project, new String[]{"**/*.jar"});
            // add rt.jar to lib path
            libPath = jarFiles.stream().map(j -> Paths.get(project, j).toString()).collect(Collectors.joining(File.pathSeparator));
            if (libPath.isEmpty()) {
                libPath += jdk;
            } else {
                libPath += File.pathSeparator + jdk;
            }

            Set<String> copied = new HashSet<>();
            List<String> javaClasses = new ArrayList<>();
            try {
                for (String file : classFiles) {
                    String absPath = Paths.get(project, file).toString();
                    String md5 = PathUtil.md5(absPath);
                    if (copied.contains(md5)) {
                        continue;
                    }
                    File o = new File(absPath);
                    String packageName = PathUtil.classPackageName(absPath);
                    String className = PathUtil.className(absPath);
                    if (packageName == null) {
                        continue;
                    }
                    if (className != null) {
                        javaClasses.add(className);
                    }
                    Path d = Paths.get(this.tempDir, PathUtil.packageToDirString(packageName), o.getName());
                    logger.info("copy {} to {}", o.getPath(), d);
                    PathUtil.copy(o, d.toFile());
                    copied.add(md5);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            appPath = tempDir;
            List<String> libClasses = scanLibClasses(libPath);
            libClasses.removeAll(javaClasses);
            excludes.addAll(new ArrayList<>(libClasses));
            addEntry();
            if (extractTemp != null) {
                PathUtil.deteleTempdir(extractTemp);
            }
        }
    }

    private List<String> scanLibClasses(String libPath) {
        String[] jarFiles = libPath.split(File.pathSeparator);
        List<String> result = new ArrayList<>();
        for (String jarName : jarFiles) {
            if (jarName.contains("rt.jar") || jarName.equals("VIRTUAL_FS_FOR_JDK")) {
                continue;
            }
            try {
                JarFile jarFile = new JarFile(jarName);
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    if ((!entryName.contains("$")) && entryName.endsWith(".class")) {
                        String className = entryName.substring(0, entryName.length() - ".class".length()).replace("/", ".");
                        result.add(className);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void scanLib() {
        List<String> realLibPath = new ArrayList<>();
        if (jdk != null && !jdk.isEmpty()) {
            realLibPath.add(jdk);
        }
        for (String path : libPaths) {
            if (path.endsWith(".jar")) {
                realLibPath.add(path);
            } else {
                File file = new File(path);
                if (file.isDirectory()) {
                    realLibPath.addAll(Arrays.stream(Objects.requireNonNull(file.listFiles())).filter(f -> f.getName().endsWith(".jar")).map(File::getPath).collect(toList()));
                }
            }
        }
        libPath = String.join(File.pathSeparator, realLibPath);
    }

    public void addEntry() {
        if (autoAddEntry) {
            List<String> javaClasses = PathUtil.filterFile(tempDir, new String[]{"**/*.class"});
            EntrySelectorManager entrySelectorManager = EntrySelectorManager.buildEntryManager(tempDir);
            entrySelectorManager.selectorList(entrySelector).forEach(s -> {
                // TODO filter selectors based on configuration.
                javaClasses.forEach(c -> {
                    String absPath = Paths.get(tempDir, c).toString();
                    epoints.addAll(s.select(absPath));
                });
            });
        }
    }

    public String getLibPath() {
        return libPath;
    }

    public void setLibPath(String libPath) {
        this.libPath = libPath;
    }

    public int getPathReconstructionTimeout() {
        return pathReconstructionTimeout;
    }

    public void setPathReconstructionTimeout(int pathReconstructionTimeout) {
        this.pathReconstructionTimeout = pathReconstructionTimeout;
    }

    public int getMaxPathLength() {
        return maxPathLength;
    }

    public void setMaxPathLength(int maxPathLength) {
        this.maxPathLength = maxPathLength;
    }

    public List<String> getEpoints() {
        return epoints;
    }

    public void setEpoints(List<String> epoints) {
        this.epoints = epoints;
    }

    @Deprecated
    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    @Deprecated
    public List<String> getSinks() {
        return sinks;
    }

    public void setSinks(List<String> sinks) {
        this.sinks = sinks;
    }

    public String getAppPath() {
        return appPath;
    }

    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }

    public List<String> getLibPaths() {
        return libPaths;
    }

    public void setLibPaths(List<String> libPaths) {
        this.libPaths = libPaths;
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes = excludes;
    }

    public String getJdk() {
        return jdk;
    }

    public void setJdk(String jdk) {
        this.jdk = jdk;
    }

}

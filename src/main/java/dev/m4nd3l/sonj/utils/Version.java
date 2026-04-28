package dev.m4nd3l.sonj.utils;

public class Version {
    private int majorVersion;
    private int minorVersion;
    private int bugFixVersion;

    public Version(int majorVersion, int minorVersion, int bugFixVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.bugFixVersion = bugFixVersion;
    }

    public int getBugFixVersion() { return bugFixVersion; }
    public int getMajorVersion() { return majorVersion; }
    public int getMinorVersion() { return minorVersion; }

    public Version setBugFixVersion(int bugFixVersion) { this.bugFixVersion = bugFixVersion; return this; }
    public Version setMajorVersion(int majorVersion) { this.majorVersion = majorVersion; return this; }
    public Version setMinorVersion(int minorVersion) { this.minorVersion = minorVersion; return this; }

    @Override
    public String toString() { return String.format("%d.%d.%d", getMajorVersion(), getMinorVersion(), getBugFixVersion()); }
}

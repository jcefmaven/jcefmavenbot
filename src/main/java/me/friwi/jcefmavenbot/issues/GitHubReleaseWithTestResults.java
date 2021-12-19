package me.friwi.jcefmavenbot.issues;

import me.friwi.jcefmavenbot.github.api.GitHubRelease;
import org.json.simple.JSONObject;

import java.io.IOException;

public class GitHubReleaseWithTestResults{
    public static final String TABLE_PATTERN = "|---|---|---|---|";

    private GitHubRelease release;

    private TestResult[][] testMatrix;
    private String mavenVersion;
    private String prefix, suffix;

    public GitHubReleaseWithTestResults(GitHubRelease release, TestResult[][] testMatrix, String mavenVersion, String prefix, String suffix) {
        this.release = release;
        this.testMatrix = testMatrix;
        this.mavenVersion = mavenVersion;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public static GitHubReleaseWithTestResults fromGitHubRelease(Object o) {
        return fromGitHubRelease((GitHubRelease) o);
    }

    public static GitHubReleaseWithTestResults fromGitHubRelease(GitHubRelease release){
        String prefix = release.getBody().substring(0, release.getBody().indexOf(TABLE_PATTERN)+TABLE_PATTERN.length())+"\n";
        String suffix = release.getBody().substring(prefix.length()).trim();
        TestResult[][] testMatrix = new TestResult[EnumArch.values().length][];
        for(int i = 0; i < EnumArch.values().length; i++){
            int ind = suffix.indexOf("\n")+1;
            if(ind==0)ind = suffix.length(); //Consume everything if EOF
            String row = suffix.substring(0, ind).trim();
            suffix = suffix.substring(ind);
            String[] parts = row.split("\\|");
            testMatrix[i] = new TestResult[EnumOS.values().length];
            for(int j = 0; j<EnumOS.values().length; j++){
                EnumPlatform platform = EnumPlatform.fromOSAndArch(EnumOS.values()[j], EnumArch.values()[i]);
                testMatrix[i][j] = TestResult.fromString(platform, parts[j+2]); //+2 to skip empty value and arch name
            }
        }
        String mavenVersion = release.getBody().substring(release.getBody().indexOf("<version>")+9);
        mavenVersion = mavenVersion.substring(0, mavenVersion.indexOf("</version>")).trim();
        return new GitHubReleaseWithTestResults(release, testMatrix, mavenVersion, prefix, suffix);
    }

    public TestResult getTestResult(EnumPlatform platform){
        return testMatrix[platform.getArch().ordinal()][platform.getOS().ordinal()];
    }

    public GitHubRelease getRelease() {
        return release;
    }

    public String getMavenVersion() {
        return mavenVersion;
    }

    public void update() throws IOException {
        StringBuilder build = new StringBuilder(prefix);
        for(int i = 0; i < EnumArch.values().length; i++){
            StringBuilder row = new StringBuilder("|" + EnumArch.values()[i].getValue() + "|");
            for(int j = 0; j<EnumOS.values().length; j++){
                row.append(testMatrix[i][j] == null ? " - " : testMatrix[i][j].toString()).append("|");
            }
            row.append("\n");
            build.append(row);
        }
        build.append(suffix);
        this.release.setBody(build.toString());
        this.release.update();
    }
}

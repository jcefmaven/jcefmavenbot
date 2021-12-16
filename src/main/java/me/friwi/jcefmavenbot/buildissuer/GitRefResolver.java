package me.friwi.jcefmavenbot.buildissuer;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitRefResolver {
    private static final File gitDir = new File("jcef");
    private static Git git;

    public static void init(String repoUrl) throws IOException{
        try {
            if(gitDir.exists()){
                System.out.println("Pulling git repo...");
                git = Git.open(gitDir);
                git.pull().call();
            }else {
                System.out.println("Cloning git repo...");
                git = Git.cloneRepository()
                        .setURI(repoUrl)
                        .setDirectory(gitDir)
                        .call();
            }
        } catch (Exception e) {
            throw new IOException("Error while cloning jcef repository!", e);
        }
    }

    public static List<String> fetchAllCommitsSince(String initialRef) throws IOException {
        try {
            System.out.println("Pulling git repo...");
            git.pull().call();
            Iterable<RevCommit> logs = git.log().add(git.getRepository().resolve("master")).call();
            List<String> commitIds = new ArrayList<>();
            for (RevCommit rev : logs) {
                commitIds.add(rev.name());
                if(rev.name().startsWith(initialRef))break;
            }
            return commitIds;
        } catch (GitAPIException e) {
            throw new IOException("Error while fetching jcef repository commits!", e);
        }
    }
}

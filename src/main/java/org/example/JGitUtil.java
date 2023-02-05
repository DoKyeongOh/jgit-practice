package org.example;

import lombok.Builder;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JGitUtil {
    String username;
    String email;
    Git git;

    @Builder
    public JGitUtil(String remoteRepo, String localRepo, String username, String email) {
        this.username = username;
        this.email = email;
        git = loadRepository(remoteRepo, localRepo);
    }

    public Git loadRepository(String remoteRepo, String localRepo) {
        File file = new File(localRepo);
        if (!file.exists()) {
            throw new GitException(ErrorCode.DIRECTORY_IS_NOT_EXIST);
        }

        if (!file.isDirectory()) {
            throw new GitException(ErrorCode.INPUT_IS_NOT_DIRECTORY);
        }

        String[] filenames = file.list();
        if (filenames.length == 0) {
            return cloneRepository(remoteRepo, localRepo);
        }

        for (String filename : filenames) {
            if (filename.equals(".git")) {
                return openRepository(localRepo);
            }
        }
        throw new GitException(ErrorCode.BAD_DIRECTORY);
    }

    public Git cloneRepository(String remoteRepo, String localRepo) {
        try {
            return Git.cloneRepository()
                    .setURI(remoteRepo)
                    .setDirectory(new File(localRepo))
                    .call();
        } catch (GitAPIException e) {
            throw new GitException(ErrorCode.REMOTE_CLONE_FAILURE, e.getMessage());
        }
    }

    public Git openRepository(String localRepo) {
        try {
            return Git.open(new File(localRepo));
        } catch (IOException e) {
            throw new GitException(ErrorCode.LOCAL_OPEN_FAILURE, e.getMessage());
        }
    }

    public void pushIfChanged(Set<String> scanFilenameSet) {
        resetStagingArea();
        List<DiffEntry> diffEntries = getDiffEntries(scanFilenameSet);
        if (diffEntries.size() == 0) {
            return;
        }

        addFilesToStagingArea(diffEntries);
        commit(generateCommitMsg(diffEntries));
        pushWithSsh();
    }

    public void resetStagingArea() {
        try {
            git.reset().call();
        } catch (GitAPIException e) {
            throw new GitException(ErrorCode.GIT_RESET_FAILURE, e.getMessage());
        }
    }

    public List<DiffEntry> getDiffEntries(Set<String> scanFilenameSet) {
        try {
            return git.diff().call().stream().filter(diffEntry -> {
                if (scanFilenameSet == null || scanFilenameSet.isEmpty()) {
                    return true;
                }
                String path = diffEntry.getNewPath();
                if (isDeletedEntry(diffEntry)) {
                    path = diffEntry.getOldPath();
                }
                return scanFilenameSet.contains(path);
            }).collect(Collectors.toList());
        } catch (GitAPIException e) {
            throw new GitException(ErrorCode.DIFF_SEARCH_FAILURE, e.getMessage());
        }
    }

    public void addFilesToStagingArea(List<DiffEntry> diffEntries) {
        int updateCount = 0;
        int removeCount = 0;
        AddCommand addCommand = git.add();
        RmCommand rmCommand = git.rm();

        for (DiffEntry diffEntry : diffEntries) {
            if (isDeletedEntry(diffEntry)) {
                rmCommand.addFilepattern(diffEntry.getOldPath());
                removeCount++;
            } else {
                addCommand.addFilepattern(diffEntry.getNewPath());
                updateCount++;
            }
        }

        try {
            if (updateCount > 0) {
                addCommand.call();
            }
        } catch (GitAPIException e) {
            throw new GitException(ErrorCode.DIFF_FILE_STAGING_FAILURE, e.getMessage());
        }

        try {
            if (removeCount > 0) {
                rmCommand.call();
            }
        } catch (GitAPIException e) {
            throw new GitException(ErrorCode.REMOVED_FILE_STAGING_FAILURE, e.getMessage());
        }
    }

    public void commit(String commitMsg) {
        try {
            git.commit().setAuthor(username, email)
                    .setMessage(commitMsg)
                    .call();
        } catch (GitAPIException e) {
            throw new GitException(ErrorCode.GIT_COMMIT_FAILURE, e.getMessage());
        }
    }

    public String generateCommitMsg(List<DiffEntry> diffEntries) {
        String add = "";
        String modify = "";
        String delete = "";
        for (DiffEntry diffEntry : diffEntries) {
            switch (diffEntry.getChangeType().toString()) {
                case "ADD" : {
                    add += "  - " + diffEntry.getNewPath() + "\n";
                    break;
                }

                case "MODIFY" : {
                    modify += "  - " + diffEntry.getNewPath() + "\n";
                    break;
                }

                case "DELETE" : {
                    delete += "  - " + diffEntry.getNewPath() + "\n";
                    break;
                }
            }
        }

        String commitMsg = "[" + new Date() + "] total " + diffEntries.size() + " changed. \n";
        if (!add.isEmpty()) {
            commitMsg += "ADD \n" + add;
        }

        if (!modify.isEmpty()) {
            commitMsg += "MODIFY \n" + modify;
        }

        if (!delete.isEmpty()) {
            commitMsg += "DELETE \n" + delete;
        }

        return commitMsg;
    }

    public void pushWithSsh() {

    }

    private boolean isDeletedEntry(DiffEntry diffEntry) {
        return diffEntry.getChangeType().toString().equals("DELETE");
    }
}

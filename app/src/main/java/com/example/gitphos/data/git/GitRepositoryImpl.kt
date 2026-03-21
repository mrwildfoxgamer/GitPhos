package com.example.gitphos.data.git

import com.example.gitphos.domain.model.GitErrorCode
import com.example.gitphos.domain.model.GitResult
import com.example.gitphos.domain.repository.GitRepository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.lib.ProgressMonitor
import org.eclipse.jgit.lib.StoredConfig
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class GitRepositoryImpl @Inject constructor() : GitRepository {

    override suspend fun initRepository(localPath: String): GitResult<Unit> = runGit {
        val dir = File(localPath)
        if (!dir.exists()) dir.mkdirs()

        if (File(localPath, ".git").exists()) {
            Timber.d("Git: repo already exists at $localPath")
            return@runGit
        }

        Git.init().setDirectory(dir).call().use { git ->
            Timber.d("Git: initialized repo at $localPath")
            configureRepo(git.repository.config)
        }
    }

    override suspend fun cloneRepository(
        remoteUrl: String,
        localPath: String,
        token: String
    ): GitResult<Unit> = runGit {
        val credentials = credentials(token)
        Git.cloneRepository()
            .setURI(remoteUrl)
            .setDirectory(File(localPath))
            .setCredentialsProvider(credentials)
            .setBranch("main")
            .call()
            .use { Timber.d("Git: cloned $remoteUrl to $localPath") }
    }

    override suspend fun addFiles(
        localPath: String,
        filePatterns: List<String>
    ): GitResult<Unit> = runGit {
        openRepo(localPath).use { git ->
            val addCmd = git.add()
            filePatterns.forEach { addCmd.addFilepattern(it) }
            addCmd.call()
            Timber.d("Git: staged patterns=$filePatterns")
        }
    }

    override suspend fun commit(
        localPath: String,
        message: String,
        authorName: String,
        authorEmail: String
    ): GitResult<Unit> = runGit {
        openRepo(localPath).use { git ->
            val status = git.status().call()
            if (status.isClean) {
                Timber.d("Git: nothing to commit")
                return@runGit
            }
            git.commit()
                .setMessage(message)
                .setAuthor(authorName, authorEmail)
                .setCommitter(authorName, authorEmail)
                .call()
            Timber.d("Git: committed — $message")
        }
    }

    override suspend fun push(
        localPath: String,
        remoteUrl: String,
        token: String,
        branch: String,
        onProgress: ((String) -> Unit)?
    ): GitResult<Unit> = runGit {
        val credentials = credentials(token)
        openRepo(localPath).use { git ->
            configureRemote(git, remoteUrl)
            git.push()
                .setRemote("origin")
                .setCredentialsProvider(credentials)
                .setProgressMonitor(progressMonitor(onProgress))
                .call()
                .forEach { result ->
                    result.remoteUpdates.forEach { update ->
                        Timber.d("Git: push update — ${update.remoteName} status=${update.status}")
                    }
                }
        }
    }

    override suspend fun getRepoSize(localPath: String): GitResult<Long> = runGit {
        val gitDir = File(localPath, ".git")
        gitDir.walkTopDown().sumOf { if (it.isFile) it.length() else 0L }
    }

    override suspend fun ensureBranch(
        localPath: String,
        branch: String
    ): GitResult<Unit> = runGit {
        openRepo(localPath).use { git ->
            val branches = git.branchList().call().map { it.name }
            val fullRef = "refs/heads/$branch"
            if (branches.contains(fullRef)) {
                git.checkout().setName(branch).call()
            } else {
                git.checkout()
                    .setCreateBranch(true)
                    .setName(branch)
                    .call()
            }
            Timber.d("Git: on branch $branch")
        }
    }

    override suspend fun isValidRepo(localPath: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                FileRepositoryBuilder()
                    .setGitDir(File(localPath, ".git"))
                    .readEnvironment()
                    .findGitDir()
                    .build()
                    .use { it.objectDatabase.exists() }
            } catch (e: Exception) {
                false
            }
        }
    }

    // --- Helpers ---

    private fun openRepo(localPath: String): Git {
        return Git.open(File(localPath))
    }

    private fun credentials(token: String): UsernamePasswordCredentialsProvider {
        return UsernamePasswordCredentialsProvider(token, "")
    }

    private fun configureRemote(git: Git, remoteUrl: String) {
        val config: StoredConfig = git.repository.config
        config.setString("remote", "origin", "url", remoteUrl)
        config.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*")
        config.save()
    }

    private fun configureRepo(config: StoredConfig) {
        config.setString("user", null, "name", "GitPhos")
        config.setString("user", null, "email", "gitphos@local")
        config.save()
    }

    private fun progressMonitor(onProgress: ((String) -> Unit)?): ProgressMonitor {
        return object : ProgressMonitor {
            override fun start(totalTasks: Int) {}
            override fun beginTask(title: String?, totalWork: Int) {
                title?.let { onProgress?.invoke(it) }
            }
            override fun update(completed: Int) {}
            override fun endTask() {}
            override fun isCancelled(): Boolean = false
            override fun showDuration(enabled: Boolean) {}
        }
    }
}

private suspend fun <T> runGit(block: suspend () -> T): GitResult<T> {
    return withContext(Dispatchers.IO) {
        try {
            GitResult.Success(block())
        } catch (e: TransportException) {
            val isAuth = e.message?.contains("401") == true || e.message?.contains("not authorized", ignoreCase = true) == true
            GitResult.Error(
                code = if (isAuth) GitErrorCode.AUTH_FAILED else GitErrorCode.NETWORK_ERROR,
                message = e.message ?: "Transport error",
                cause = e
            )
        } catch (e: org.eclipse.jgit.api.errors.GitAPIException) {
            GitResult.Error(GitErrorCode.UNKNOWN, e.message ?: "Git API error", e)
        } catch (e: Exception) {
            GitResult.Error(GitErrorCode.UNKNOWN, e.message ?: "Unknown error", e)
        }
    }
}
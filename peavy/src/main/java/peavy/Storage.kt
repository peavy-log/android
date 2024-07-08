package peavy

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileWriter
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.time.Duration.Companion.seconds


internal class Storage(context: Context) {
    companion object {
        const val MIN_AVAILABLE_SPACE = 768 * 1024 * 1024 // 768 MB
        const val MAX_FILE_SIZE = 1024 * 1024 // 1 MB
        const val MAX_COMPACTED_SIZE = 1024 * 100 // 100 kB
    }

    private val currentMutex = Mutex()
    private val bufferMutex = Mutex()
    private val endedMutex = Mutex()

    private val directory = File(context.filesDir, ".peavy").also {
        it.mkdirs()
    }

    private var currentFile = File(directory, "current")
    private val buffer = mutableListOf<LogEntry>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private var saver = CoroutineScope(Dispatchers.IO.limitedParallelism(1))
    private var compactor = CoroutineScope(Dispatchers.IO)

    init {
        flusher()
        compacter()
    }

    fun storeEntry(logEntry: LogEntry) = saver.launch {
        bufferMutex.withLock {
            buffer.add(logEntry)
            Debug.log("Stored $logEntry to buffer")
        }
    }

    fun flushImmediately() {
        Debug.log("Flushing immediately")
        unsafeFlush()
    }

    suspend fun flush() {
        val entries = bufferMutex.withLock {
            val entries = buffer.toList()
            buffer.clear()
            entries
        }
        if (entries.isEmpty()) return

        Debug.log("Flushing ${entries.size} log entries")

        if (directory.usableSpace < MIN_AVAILABLE_SPACE) {
            val usable = Debug.formatSize(directory.usableSpace)
            val free = Debug.formatSize(directory.freeSpace)
            val total = Debug.formatSize(directory.totalSpace)
            Debug.warnSome("Not enough available space (usable=$usable, free=$free, total=$total), dropping ${entries.size} entries")
            return
        }

        currentMutex.withLock {
            try {
                FileWriter(requireCurrentFile(), true).use {
                    for (entry in entries) {
                        it.write(entry.toJson().toString())
                        it.write("\n")
                    }
                    Debug.log("Flushed ${entries.size} entries")
                }
            } catch (e: Exception) {
                Debug.warnSome("Error flushing", e)
            }
        }
    }

    suspend fun hasCurrentEntries(): Boolean {
        currentMutex.withLock {
            return currentFile.exists() && currentFile.length() > 50
        }
    }

    suspend fun endCurrentFile() {
        currentMutex.withLock {
            if (!currentFile.exists()) {
                return
            }

            val rolled = File(directory, System.currentTimeMillis().toString())

            currentFile.inputStream().use { inFile ->
                GZIPOutputStream(rolled.outputStream()).use { outFile ->
                    inFile.copyTo(outFile)
                    outFile.flush()
                }
            }

            currentFile.delete()
        }
    }

    suspend fun iterEndedFiles(block: (File) -> Boolean) {
        endedMutex.withLock {
            for (file in listEndedFiles()) {
                if (!block(file)) break
            }
        }
    }

    private fun listEndedFiles(): List<File> {
        return directory.listFiles { _, filename -> filename != "current" }?.toList() ?: emptyList()
    }

    private fun flusher() = compactor.launch {
        while (true) {
            delay(5.seconds)
            if (buffer.size != 0) {
                flush()
            }
        }
    }.invokeOnCompletion {
        unsafeFlush()
    }

    private fun unsafeFlush() {
        val entries = buffer.toList()
        Debug.log("Flushing unsafe size=${entries.size}")
        if (entries.isEmpty()) return
        buffer.clear()
        FileWriter(requireCurrentFile(), true).use {
            for (entry in entries) {
                it.write(entry.toJson().toString())
                it.write("\n")
            }
            Debug.log("Flushed ${entries.size} entries")
        }
    }

    private fun compacter() = compactor.launch {
        while (true) {
            delay(30.seconds)

            Debug.log("Current file size: ${currentFile.length()}")
            if (currentFile.length() > MAX_FILE_SIZE) {
                Debug.log("Above max size, rolling")
                try {
                    endCurrentFile()
                } catch (e: Exception) {
                    Debug.warn("Error rolling current", e)
                }
            }

            if (listEndedFiles().size > 20) {
                Debug.log("More than 20 ended files, compacting")
                try {
                    compactEndedFiles()
                } catch (e: Exception) {
                    Debug.warn("Error compacting", e)
                }
            }
        }
    }

    private fun requireCurrentFile(): File {
        if (!currentFile.exists()) {
            currentFile.createNewFile()
        }
        return currentFile
    }

    private suspend fun compactEndedFiles() {
        endedMutex.withLock {
            val compacted = File(directory, System.currentTimeMillis().toString())
            GZIPOutputStream(compacted.outputStream()).use { outFile ->
                listEndedFiles().forEach { file ->
                    // Don't compact the new compacted file
                    if (file.name == compacted.name) {
                        return@forEach
                    }
                    // Don't compact files above max compacted size
                    if (file.length() > MAX_COMPACTED_SIZE) {
                        return@forEach
                    }

                    if (file.length() > 50) {
                        try {
                            GZIPInputStream(file.inputStream()).use { inFile ->
                                inFile.copyTo(outFile)
                            }
                        } catch (e: Exception) {
                            Debug.warn("Error in ended file $file", e)
                        }
                    }
                    file.delete()
                }
            }
            Debug.log("Compacted to ${compacted.length()} size")
        }
    }
}
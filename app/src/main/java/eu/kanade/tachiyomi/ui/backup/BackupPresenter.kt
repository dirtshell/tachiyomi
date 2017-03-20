package eu.kanade.tachiyomi.ui.backup

import android.os.Bundle
import eu.kanade.tachiyomi.data.backup.BackupManager
import eu.kanade.tachiyomi.data.database.DatabaseHelper
import eu.kanade.tachiyomi.ui.base.presenter.BasePresenter
import eu.kanade.tachiyomi.util.isNullOrUnsubscribed
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import uy.kohesive.injekt.injectLazy
import java.io.File
import java.io.InputStream

/**
 * Presenter of [BackupFragment].
 */
class BackupPresenter : BasePresenter<BackupFragment>() {

    /**
     * Database.
     */
    val db: DatabaseHelper by injectLazy()

    /**
     * Backup manager.
     */
    private lateinit var backupManager: BackupManager

    /**
     * Subscription where the backup is restored.
     */
    private var restoreSubscription: Subscription? = null

    /**
     * Subscription where the backup is created.
     */
    private var backupSubscription: Subscription? = null

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        backupManager = BackupManager(db)
    }

    /**
     * Creates a backup and saves it to a file.
     *
     * @param file the path where the file will be saved.
     */
    fun createBackup(file: File) {
        if (backupSubscription.isNullOrUnsubscribed()) {
            backupSubscription = getBackupObservable(file)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeFirst(
                            { view, result -> view.onBackupCompleted(file) },
                            BackupFragment::onBackupError)
        }
    }

    /**
     * Restores a backup from a stream.
     *
     * @param stream the input stream of the backup file.
     */
    fun restoreBackup(stream: InputStream) {
        if (restoreSubscription.isNullOrUnsubscribed()) {
            restoreSubscription = getRestoreObservable(stream)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeFirst(
                            { view, result -> view.onRestoreCompleted() },
                            BackupFragment::onRestoreError)
        }
    }

    /**
     * Returns the observable to save a backup.
     */
    private fun getBackupObservable(file: File) = Observable.fromCallable {
        backupManager.backupToFile(file)
        true
    }

    /**
     * Returns the observable to restore a backup.
     */
    private fun getRestoreObservable(stream: InputStream) = Observable.fromCallable {
        backupManager.restoreFromStream(stream)
        true
    }

}

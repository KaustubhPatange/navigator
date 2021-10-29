package com.kpstv.home_internal.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.LiveData
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

@HiltWorker
class HomeInternalWorker @AssistedInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

  override suspend fun doWork(): Result {
    delay(2000)
    return Result.success()
  }

  companion object {
    private const val ID = "worker:home-internal"
    fun schedule(context: Context) {
      val request = OneTimeWorkRequestBuilder<HomeInternalWorker>()
        .addTag(ID)
        .build()

      WorkManager.getInstance(context).enqueueUniqueWork(ID, ExistingWorkPolicy.KEEP, request)
    }

    fun observe(context: Context) : LiveData<List<WorkInfo>> {
      return WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(ID)
    }
  }
}